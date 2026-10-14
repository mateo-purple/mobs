package dev.friendmonsters;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import java.util.Random;
public final class Trades {
    private Trades(){}
    public static void open(ServerPlayer player, Mob mob) {
        if(!mob.isAlive() || !Relations.ownerMatches(mob,player) ||
           !Rules.ILLAGERS.contains(Data.kind(mob)))return;
        Merchant merchant=new FriendMerchant(mob,player);
        merchant.openTradingScreen(player,mob.getName(),1);
    }
    private static void sell(MerchantOffers offers,Item item,int price,int amount) {
        offers.add(new MerchantOffer(new ItemCost(Items.EMERALD_BLOCK,price),
            new ItemStack(item,amount),12,1,0));
    }
    private static void sellEmeralds(MerchantOffers offers,Item item,int price,int amount) {
        offers.add(new MerchantOffer(new ItemCost(Items.EMERALD,price),
            new ItemStack(item,amount),12,1,0));
    }
    private static void smith(MerchantOffers offers,Item item,int price){
        String kind=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getPath();
        if(kind.startsWith("diamond_") || kind.startsWith("netherite_"))sell(offers,item,price,1);
        else sellEmeralds(offers,item,price,1);
    }
    private static void buy(MerchantOffers offers,Item item,int price,int emeralds) {
        offers.add(new MerchantOffer(new ItemCost(item,price),
            new ItemStack(Items.EMERALD,emeralds),20,1,0));
    }
    private static Item choice(Random random,Item... options){
        return options[random.nextInt(options.length)];
    }
    private static MerchantOffers offers(Mob mob) {
        long day=Rules.merchantDay(mob.level().getDayTime());
        MerchantOffers saved=Data.load(mob,"trade_offers",MerchantOffers.CODEC,new MerchantOffers());
        if(Data.flag(mob,"trade_generated") && Data.time(mob,"trade_day")==day && !saved.isEmpty())return saved;
        MerchantOffers generated=generateOffers(mob,day);
        Data.set(mob,"trade_day",day);
        Data.set(mob,"trade_generated",true);
        Data.save(mob,"trade_offers",MerchantOffers.CODEC,generated);
        return generated;
    }
    private static MerchantOffers generateOffers(Mob mob,long day) {
        MerchantOffers offers=new MerchantOffers();
        Random random=new Random(mob.getUUID().getLeastSignificantBits()
            ^ mob.getUUID().getMostSignificantBits() ^
            day*1013904223L);
        String kind=Data.kind(mob);
        if(kind.equals("pillager")){
            sellEmeralds(offers,choice(random,Items.COBBLESTONE,Items.SAND,Items.OAK_LOG,
                Items.REDSTONE,Items.COAL,Items.ENDER_PEARL),2+random.nextInt(4),2);
            sellEmeralds(offers,choice(random,Items.GOLD_INGOT,Items.BLAZE_ROD,Items.GHAST_TEAR,
                Items.OBSIDIAN,Items.LAPIS_LAZULI),5+random.nextInt(6),1);
            sell(offers,choice(random,Items.NETHERITE_SCRAP,Items.TRIDENT,Items.SHULKER_SHELL,
                Items.ANCIENT_DEBRIS,Items.ELYTRA),9+random.nextInt(14),1);
            if(random.nextInt(4)==0)
                sell(offers,choice(random,Items.NETHER_STAR,Items.ENCHANTED_GOLDEN_APPLE,
                    Items.NETHERITE_BLOCK),24+random.nextInt(30),1);
            buy(offers,choice(random,Items.BREAD,Items.CARROT,Items.COOKED_BEEF,
                Items.APPLE,Items.POTATO,Items.COOKED_CHICKEN),16,3);
            buy(offers,Items.ARROW,32,5);
            buy(offers,Items.CROSSBOW,1,9);
        } else if(kind.equals("vindicator")){
            smith(offers,choice(random,Items.WOODEN_SWORD,Items.STONE_SWORD,Items.COPPER_SWORD,
                Items.IRON_SWORD,Items.GOLDEN_SWORD,Items.DIAMOND_SWORD,Items.NETHERITE_SWORD),8+random.nextInt(12));
            smith(offers,choice(random,Items.WOODEN_AXE,Items.STONE_AXE,Items.COPPER_AXE,
                Items.IRON_AXE,Items.GOLDEN_AXE,Items.DIAMOND_AXE,Items.NETHERITE_AXE),8+random.nextInt(12));
            smith(offers,choice(random,Items.LEATHER_HELMET,Items.LEATHER_CHESTPLATE,Items.LEATHER_LEGGINGS,Items.LEATHER_BOOTS,
                Items.CHAINMAIL_HELMET,Items.CHAINMAIL_CHESTPLATE,Items.CHAINMAIL_LEGGINGS,Items.CHAINMAIL_BOOTS,
                Items.COPPER_HELMET,Items.COPPER_CHESTPLATE,Items.COPPER_LEGGINGS,Items.COPPER_BOOTS,
                Items.IRON_HELMET,Items.IRON_CHESTPLATE,Items.IRON_LEGGINGS,Items.IRON_BOOTS,
                Items.GOLDEN_HELMET,Items.GOLDEN_CHESTPLATE,Items.GOLDEN_LEGGINGS,Items.GOLDEN_BOOTS,
                Items.DIAMOND_HELMET,Items.DIAMOND_CHESTPLATE,Items.DIAMOND_LEGGINGS,Items.DIAMOND_BOOTS,
                Items.NETHERITE_HELMET,Items.NETHERITE_CHESTPLATE,Items.NETHERITE_LEGGINGS,Items.NETHERITE_BOOTS),6+random.nextInt(19));
            buy(offers,Items.IRON_INGOT,12,3);
        }
        else if(kind.equals("witch")){
            var potions=new net.minecraft.core.Holder[]{
                 net.minecraft.world.item.alchemy.Potions.HEALING,
                 net.minecraft.world.item.alchemy.Potions.STRENGTH,
                 net.minecraft.world.item.alchemy.Potions.SWIFTNESS,
                 net.minecraft.world.item.alchemy.Potions.POISON,
                 net.minecraft.world.item.alchemy.Potions.REGENERATION,
                 net.minecraft.world.item.alchemy.Potions.HARMING
            };
            for(int i=0;i<3;i++) {
                @SuppressWarnings("unchecked")
                var potion=(net.minecraft.core.Holder<net.minecraft.world.item.alchemy.Potion>)
                    potions[random.nextInt(potions.length)];
                offers.add(new MerchantOffer(new ItemCost(Items.EMERALD_BLOCK,3+random.nextInt(9)),
                    net.minecraft.world.item.alchemy.PotionContents.createItemStack(Items.POTION,potion),12,1,0));
            }
            sell(offers,Items.DRAGON_BREATH,16+random.nextInt(20),1);
            buy(offers,Items.NETHER_WART,12,4);
        }else if(kind.equals("evoker")){
            offers.add(new MerchantOffer(new ItemCost(Items.DIAMOND,24+random.nextInt(24)),
                new ItemStack(Items.TOTEM_OF_UNDYING),4,1,0));
            sell(offers,Items.OMINOUS_BOTTLE,18+random.nextInt(22),1);
            ItemStack enchanted=net.minecraft.world.item.enchantment.EnchantmentHelper.enchantItem(
                mob.getRandom(),new ItemStack(Items.BOOK),30,mob.registryAccess(),java.util.Optional.empty());
            offers.add(new MerchantOffer(new ItemCost(Items.EMERALD_BLOCK,14+random.nextInt(12)),
                enchanted,8,1,0));
            sell(offers,Items.EXPERIENCE_BOTTLE,9,8);
        }
        return offers;
    }
    private static final class FriendMerchant implements Merchant {
        final Mob mob;
        final long day;
        Player trading;
        MerchantOffers active;
        FriendMerchant(Mob mob,Player player){
            this.mob=mob;this.trading=player;this.active=offers(mob);
            this.day=Rules.merchantDay(mob.level().getDayTime());
        }
        private void save(){
            if(Data.time(mob,"trade_day")==day)Data.save(mob,"trade_offers",MerchantOffers.CODEC,active);
        }
        public void setTradingPlayer(Player player){
            trading=player;
            if(player==null){save();Data.remove(mob,"trading_until");}
        }
        @Override
        public void openTradingScreen(Player player,Component title,int level){
            var menu=player.openMenu(new SimpleMenuProvider(
                (id,inventory,viewer)->new FriendMerchantMenu(id,inventory,this),title));
            if(menu.isPresent()){
                trading=player;
                Data.set(mob,"trading_until",mob.level().getGameTime()+20);
                mob.setTarget(null);mob.getNavigation().stop();
                player.sendMerchantOffers(menu.getAsInt(),active,level,0,false,false);
            }else setTradingPlayer(null);
        }
        public Player getTradingPlayer(){return trading;}
        public MerchantOffers getOffers(){return active;}
        public void overrideOffers(MerchantOffers offers){active=offers;save();}
        public void notifyTrade(MerchantOffer offer){
            offer.increaseUses();save();mob.playSound(SoundEvents.VILLAGER_YES,1,1);
        }
        public void notifyTradeUpdated(ItemStack stack){}
        public int getVillagerXp(){return 0;}
        public void overrideXp(int xp){}
        public boolean showProgressBar(){return false;}
        public SoundEvent getNotifyTradeSound(){return SoundEvents.VILLAGER_YES;}
        public boolean isClientSide(){return mob.level().isClientSide();}
        public boolean stillValid(Player player){
            boolean valid=trading==player && mob.isAlive() && player.level()==mob.level() &&
                player.distanceToSqr(mob)<144 && player instanceof ServerPlayer owner && Relations.ownerMatches(mob,owner) &&
                Rules.merchantDay(mob.level().getDayTime())==day;
            if(valid)Data.set(mob,"trading_until",mob.level().getGameTime()+20);
            return valid;
        }
    }
    /** The vanilla screen and slots are retained. Vanilla shift-trade assumes its Merchant is an Entity. */
    private static final class FriendMerchantMenu extends MerchantMenu {
        FriendMerchantMenu(int id,Inventory inventory,FriendMerchant merchant){
            super(id,inventory,merchant);
        }
        @Override
        public ItemStack quickMoveStack(Player player,int index){
            if(index<0 || index>=slots.size() || !stillValid(player))return ItemStack.EMPTY;
            Slot slot=slots.get(index);
            if(!slot.hasItem())return ItemStack.EMPTY;
            ItemStack stack=slot.getItem(),original=stack.copy();
            if(index==2){
                if(!moveItemStackTo(stack,3,39,true))return ItemStack.EMPTY;
                slot.onQuickCraft(stack,original);
            }else if(index==0 || index==1){
                if(!moveItemStackTo(stack,3,39,false))return ItemStack.EMPTY;
            }else if(index>=3 && index<30){
                if(!moveItemStackTo(stack,30,39,false))return ItemStack.EMPTY;
            }else if(!moveItemStackTo(stack,3,30,false))return ItemStack.EMPTY;
            if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();
            if(stack.getCount()==original.getCount())return ItemStack.EMPTY;
            slot.onTake(player,stack);
            return original;
        }
    }
}
