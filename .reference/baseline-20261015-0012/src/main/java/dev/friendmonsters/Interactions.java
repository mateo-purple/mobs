package dev.friendmonsters;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.effect.MobEffectInstance;
import java.util.ArrayList;
import java.util.List;

public final class Interactions {
    private Interactions() {}
    public static String itemName(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
    }
    public static boolean rawMeat(ItemStack stack) { return Rules.RAW_MEAT.contains(itemName(stack)); }
    public static boolean food(ItemStack stack) { return stack.has(DataComponents.FOOD); }
    public static boolean weapon(ItemStack stack) {
        String id = itemName(stack);
        return id.endsWith("_sword") || id.endsWith("_axe") || id.endsWith("_spear")
            || id.equals("bow") || id.equals("crossbow") || id.equals("trident");
    }
    public static boolean goldArmor(ServerPlayer player) {
        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            if (itemName(player.getItemBySlot(slot)).startsWith("golden_")) return true;
        }
        return false;
    }
    public static void tell(ServerPlayer player, String message) {
        player.displayClientMessage(Component.literal(message), true);
    }
    public static void give(ServerPlayer player, ItemStack stack) {
        if (!stack.isEmpty() && !player.getInventory().add(stack)) player.drop(stack, false);
    }
    public static void consume(ServerPlayer player, ItemStack held, boolean bucket) {
        if (player.getAbilities().instabuild) return;
        held.shrink(1);
        if (bucket) give(player, new ItemStack(Items.BUCKET));
    }
    public static InteractionResult handle(ServerPlayer player, Mob mob, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        String kind = Data.kind(mob), item = itemName(held);
        long now = mob.level().getGameTime();
        if (Data.time(mob,"interaction_tick") == now && Data.text(mob,"interaction_player").equals(player.getStringUUID())) {
            return InteractionResult.SUCCESS;
        }
        if (kind.equals("piglin_brute") && item.equals("golden_axe")) {
            Data.set(mob,"bribed",player.getStringUUID()); mob.setTarget(null);
            consume(player,held,false); hearts(mob,true); tell(player,"ブルートはあなたと仲間のピグリンを見逃すようになった");
            return mark(player,mob,now);
        }
        if (Relations.ownerMatches(mob,player)) {
            if (Rules.ILLAGERS.contains(kind) && !player.isShiftKeyDown()) {
                Trades.open(player,mob); return mark(player,mob,now);
            }
            if (kind.equals("ender_dragon") && held.isEmpty() && !player.isShiftKeyDown()) {
                if (mob.level().dimension() == net.minecraft.world.level.Level.END) {
                    player.startRiding(mob,true,false); Network.sync(mob);
                    tell(player,"WASD：飛行 ／ 視線：方向 ／ 空白：上昇 ／ ダッシュ：下降 ／ 左：弾 ／ 右：ブレス ／ スニーク：降りる");
                } else tell(player,"ドラゴンに乗れるのはエンドだけです");
                return mark(player,mob,now);
            }
            if (held.isEmpty()) {
                if (kind.equals("hoglin")) { tell(player,"飼い慣らされています。ホグリンへの行動指示はありません"); return mark(player,mob,now); }
                boolean special = Rules.ILLAGERS.contains(kind) || kind.equals("enderman") || kind.equals("ender_dragon");
                if (!Rules.isCommand(true,player.isShiftKeyDown(),special)) return InteractionResult.PASS;
                if (Rules.ILLAGERS.contains(kind) && !kind.equals("pillager")) {
                    tell(player,"一時停戦中です。行動指示は連れているピリジャーに出してください");
                    return mark(player,mob,now);
                }
                Rules.Mode next = Rules.nextMode(Data.mode(mob),kind.equals("pillager"));
                Data.mode(mob,next); Data.anchor(mob); mob.setTarget(null); mob.getNavigation().stop();
                if (next == Rules.Mode.DISMISSED) Relations.dismiss(mob);
                Network.sync(mob);
                tell(player,mob.getName().getString()+"："+switch(next){case FOLLOW->"追尾";case STAY->"待機";case PATROL->"巡回";case DISMISSED->"解散";});
                return mark(player,mob,now);
            }
            if (kind.equals("creeper") && held.is(Items.TNT)) {
                int count = Data.number(mob,"tnt");
                if(count < 128){consume(player,held,false);Data.set(mob,"tnt",count+1);}
                tell(player,"TNT："+Data.number(mob,"tnt")+" / 128"); return mark(player,mob,now);
            }
            if (kind.equals("ghast") && held.is(Items.FIRE_CHARGE)) {
                consume(player,held,false); Data.set(mob,"charges",Math.min(1000000,Data.number(mob,"charges")+1));
                tell(player,"強化火球の残り："+Data.number(mob,"charges")+"発"); return mark(player,mob,now);
            }
            if (kind.equals("magma_cube") && held.is(Items.LAVA_BUCKET)) {
                Species.feedMagma(mob); consume(player,held,true); return mark(player,mob,now);
            }
            if (kind.equals("enderman") && held.is(Items.ENDER_EYE)) {
                consume(player,held,false); Rewards.order(mob,player); return mark(player,mob,now);
            }
            if ((kind.equals("spider") || kind.equals("cave_spider")) && held.has(DataComponents.POTION_CONTENTS)) {
                PotionContents contents=held.get(DataComponents.POTION_CONTENTS);
                List<MobEffectInstance> all=new ArrayList<>(Data.load(mob,"potions",MobEffectInstance.CODEC.listOf(),List.of()));
                for (MobEffectInstance effect: contents.getAllEffects()) {
                    all.removeIf(old -> old.getEffect().equals(effect.getEffect()));
                    all.add(new MobEffectInstance(effect));
                    if(kind.equals("spider")) Species.applyPermanentPotion(mob,effect);
                }
                Data.save(mob,"potions",MobEffectInstance.CODEC.listOf(),all);
                boolean bottle=held.is(Items.POTION); consume(player,held,false);
                if(bottle && !player.getAbilities().instabuild) give(player,new ItemStack(Items.GLASS_BOTTLE));
                tell(player,kind.equals("spider")?"ポーションの効果を蜘蛛自身に保存しました":"噛みつきに付与するポーション効果を保存しました");
                return mark(player,mob,now);
            }
            if (Rules.SKELETONS.contains(kind) && held.is(Items.TIPPED_ARROW)) {
                List<ItemStack> arrows=Data.items(mob,"arrows");
                ItemStack old=arrows.isEmpty()?ItemStack.EMPTY:arrows.getFirst();
                if(!old.isEmpty() && !ItemStack.isSameItemSameComponents(old,held)){give(player,old);old=ItemStack.EMPTY;}
                int existing=old.isEmpty()?0:old.getCount();
                int add=Math.min(held.getCount(),64-existing);
                if(add>0){ItemStack replacement=held.copyWithCount(existing+add);held.shrink(add);Data.items(mob,"arrows",List.of(replacement));}
                tell(player,"効果付きの矢："+(existing+add)+"本");return mark(player,mob,now);
            }
            if (canEquip(kind) && equip(player,mob,held)) return mark(player,mob,now);
            return InteractionResult.PASS;
        }
        if (Data.owned(mob)) return InteractionResult.PASS;
        double chance = chance(player,mob,held);
        if (chance < 0) return InteractionResult.PASS;
        if (chance == 0) {
            tell(player,switch(kind){
                case "warden"->"仲間化にはHP50以下が必要です";
                case "wither"->"仲間化にはHP10以下が必要です";
                case "ender_dragon"->"最初のドラゴンは仲間にできません。まず一度討伐してください";
                case "piglin"->"金の防具を1部位以上装備してください";
                case "enderman"->"エンダーマンと目を合わせながらエンダーアイを渡してください";
                default->"この条件では仲間にできません";
            });
            return mark(player,mob,now);
        }
        consume(player,held,held.is(Items.LAVA_BUCKET));
        boolean success=mob.getRandom().nextDouble()<chance;
        hearts(mob,success);
        if(success) { Relations.recruit(mob,player); tell(player,mob.getName().getString()+"が仲間になりました"); }
        return mark(player,mob,now);
    }
    private static InteractionResult mark(ServerPlayer player,Mob mob,long tick) {
        Data.set(mob,"interaction_tick",tick);Data.set(mob,"interaction_player",player.getStringUUID());
        return InteractionResult.SUCCESS;
    }
    public static boolean canEquip(String kind) {
        return Rules.ZOMBIES.contains(kind)||Rules.SKELETONS.contains(kind)||kind.equals("piglin");
    }
    public static boolean equip(ServerPlayer player,Mob mob,ItemStack held) {
        var equippable=held.get(DataComponents.EQUIPPABLE);
        EquipmentSlot slot=equippable!=null?equippable.slot():weapon(held)?EquipmentSlot.MAINHAND:null;
        if(slot==null) return false;
        ItemStack old=mob.getItemBySlot(slot).copy();
        mob.setItemSlot(slot,held.copyWithCount(1)); held.shrink(1);
        mob.setDropChance(slot,1.0f); give(player,old);
        return true;
    }
    public static double chance(ServerPlayer player,Mob mob,ItemStack held) {
        String kind=Data.kind(mob), item=itemName(held);
        boolean accepted=switch(kind){
            case "creeper" -> held.is(Items.TNT);
            case "zombie","husk","zombie_villager","drowned","zombified_piglin","phantom" -> rawMeat(held);
            case "spider","cave_spider" -> rawMeat(held) || held.is(Items.COOKED_BEEF) || held.is(Items.COOKED_PORKCHOP) || held.is(Items.COOKED_CHICKEN) || held.is(Items.COOKED_MUTTON) || held.is(Items.COOKED_RABBIT);
            case "skeleton","stray","bogged","parched" -> held.is(Items.ARROW);
            case "wither_skeleton" -> held.is(Items.WITHER_ROSE);
            case "slime" -> held.is(Items.SLIME_BLOCK);
            case "magma_cube" -> held.is(Items.LAVA_BUCKET);
            case "blaze" -> held.is(Items.BLAZE_POWDER);
            case "ghast" -> held.is(Items.SNOWBALL);
            case "enderman" -> held.is(Items.ENDER_EYE);
            case "pillager" -> held.is(Items.BOW)||food(held);
            case "piglin" -> held.is(Items.PORKCHOP) || held.is(Items.COOKED_PORKCHOP) || held.is(Items.GOLDEN_HELMET) || held.is(Items.GOLDEN_CHESTPLATE) || held.is(Items.GOLDEN_LEGGINGS) || held.is(Items.GOLDEN_BOOTS);
            case "hoglin" -> held.is(Items.RED_MUSHROOM_BLOCK)||held.is(Items.BROWN_MUSHROOM_BLOCK)||held.is(Items.MUSHROOM_STEM);
            case "elder_guardian" -> held.is(Items.GOLD_BLOCK);
            case "shulker" -> held.is(Items.END_ROD);
            case "silverfish" -> food(held);
            case "endermite" -> held.is(Items.ENDER_PEARL);
            case "warden" -> held.is(Items.SCULK_CATALYST);
            case "wither" -> held.is(Items.WITHER_SKELETON_SKULL);
            case "ender_dragon" -> held.is(Items.DRAGON_EGG)||held.is(Items.END_CRYSTAL);
            default -> false;
        };
        if(!accepted)return -1;
        if(kind.equals("piglin")&&!goldArmor(player))return 0;
        if(kind.equals("warden"))return Rules.wardenChance(mob.getHealth());
        if(kind.equals("wither")&&!Rules.witherEligible(mob.getHealth()))return 0;
        if(kind.equals("ender_dragon"))return Rules.dragonChance(Species.firstDragonDefeated(mob),held.is(Items.DRAGON_EGG),held.is(Items.END_CRYSTAL));
        if(kind.equals("enderman")) {
            var direction=mob.getEyePosition().subtract(player.getEyePosition()).normalize();
            if(player.getViewVector(1.0f).dot(direction)<0.97||!player.hasLineOfSight(mob))return 0;
        }
        return Tuning.INSTANCE.chance(kind);
    }
    public static void hearts(Mob mob,boolean success) {
        if(mob.level() instanceof ServerLevel level)level.sendParticles(success?ParticleTypes.HEART:ParticleTypes.SMOKE,
            mob.getX(),mob.getY()+mob.getBbHeight()*0.7,mob.getZ(),success?7:4,0.35,0.35,0.35,0.02);
    }
}
