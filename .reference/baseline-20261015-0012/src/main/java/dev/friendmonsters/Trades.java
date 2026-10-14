package dev.friendmonsters;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
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
        if(!Relations.ownerMatches(mob,player))return;
        Merchant merchant=new FriendMerchant(mob,player);
        merchant.openTradingScreen(player,mob.getName(),1);
    }
    private static void sell(MerchantOffers offers,Item item,int price,int amount) {
        offers.add(new MerchantOffer(new ItemCost(Items.EMERALD_BLOCK,price),
            new ItemStack(item,amount),12,1,0));
    }
    private static void buy(MerchantOffers offers,Item item,int price,int emeralds) {
        offers.add(new MerchantOffer(new ItemCost(item,price),
            new ItemStack(Items.EMERALD,emeralds),20,1,0));
    }
    private static Item choice(Random random,Item... options){
        return options[random.nextInt(options.length)];
    }
    private static MerchantOffers offers(Mob mob) {
        MerchantOffers offers=new MerchantOffers();
        Random random=new Random(mob.getUUID().getLeastSignificantBits()
            ^ mob.getUUID().getMostSignificantBits() ^
            Rules.merchantDay(mob.level().getDayTime())*1013904223L);
        String kind=Data.kind(mob);
        if(kind.equals("pillager")){
            sell(offers,choice(random,Items.COBBLESTONE,Items.SAND,Items.OAK_LOG,
                Items.REDSTONE,Items.COAL,Items.DIAMOND,Items.ENDER_PEARL),2+random.nextInt(4),2);
            sell(offers,choice(random,Items.GOLD_INGOT,Items.BLAZE_ROD,Items.GHAST_TEAR,
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
            sell(offers,choice(random,Items.IRON_SWORD,Items.GOLDEN_SWORD,
                Items.DIAMOND_SWORD,Items.NETHERITE_SWORD),8+random.nextInt(12),1);
            sell(offers,choice(random,Items.IRON_AXE,Items.GOLDEN_AXE,
                Items.DIAMOND_AXE,Items.NETHERITE_AXE),8+random.nextInt(12),1);
            sell(offers,choice(random,Items.IRON_CHESTPLATE,Items.DIAMOND_HELMET,
                Items.NETHERITE_CHESTPLATE,Items.DIAMOND_BOOTS),6+random.nextInt(19),1);
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
        Player trading;
        MerchantOffers active;
        FriendMerchant(Mob mob,Player player){
            this.mob=mob;this.trading=player;this.active=offers(mob);
        }
        public void setTradingPlayer(Player player){trading=player;}
        public Player getTradingPlayer(){return trading;}
        public MerchantOffers getOffers(){return active;}
        public void overrideOffers(MerchantOffers offers){active=offers;}
        public void notifyTrade(MerchantOffer offer){mob.playSound(SoundEvents.VILLAGER_YES,1,1);}
        public void notifyTradeUpdated(ItemStack stack){}
        public int getVillagerXp(){return 0;}
        public void overrideXp(int xp){}
        public boolean showProgressBar(){return false;}
        public SoundEvent getNotifyTradeSound(){return SoundEvents.VILLAGER_YES;}
        public boolean isClientSide(){return mob.level().isClientSide();}
        public boolean stillValid(Player player){
            return mob.isAlive() && player.distanceToSqr(mob)<144 &&
                Data.text(mob,"owner").equals(player.getStringUUID());
        }
    }
}
