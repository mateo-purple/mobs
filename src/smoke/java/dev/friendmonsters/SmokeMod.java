package dev.friendmonsters;

import java.util.function.Consumer;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Development-only: never included in the installable mod JAR. */
@Mod(SmokeMod.ID)
public final class SmokeMod {
    public static final String ID="friendmonsters_smoke";
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS=DeferredRegister.create(Registries.TEST_FUNCTION,ID);
    public SmokeMod(IEventBus bus){
        FUNCTIONS.register("command_combat",()->SmokeMod::commandCombat);
        FUNCTIONS.register("species_trade",()->SmokeMod::speciesTrade);
        FUNCTIONS.register(bus);
        bus.addListener(SmokeMod::registerTests);
    }
    private static Identifier id(String path){return Identifier.fromNamespaceAndPath(ID,path);}
    private static void registerTests(RegisterGameTestsEvent event){
        var environment=event.registerEnvironment(id("night"),new TestEnvironmentDefinition.TimeOfDay(18000));
        for(String name:new String[]{"command_combat","species_trade"})
            event.registerTest(id(name),new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION,id(name)),
                new TestData<>(environment,id("arena"),200,1,true)));
    }
    private static ServerPlayer player(GameTestHelper helper){
        ServerPlayer player=helper.makeMockServerPlayerInLevel();
        Vec3 spot=Vec3.atCenterOf(helper.absolutePos(new net.minecraft.core.BlockPos(4,2,4)));
        player.snapTo(spot.x,spot.y,spot.z,0,0);
        player.getAbilities().instabuild=false;
        return player;
    }
    private static void click(ServerPlayer player,Mob mob){
        var event=new PlayerInteractEvent.EntityInteract(player,InteractionHand.MAIN_HAND,mob);
        NeoForge.EVENT_BUS.post(event);
    }
    private static void finish(GameTestHelper helper,ServerPlayer player){
        player.closeContainer();
        player.getServer().getPlayerList().remove(player);
        helper.succeed();
    }
    private static void commandCombat(GameTestHelper helper){
        ServerPlayer player=player(helper);
        var ally=helper.spawn(EntityType.SILVERFISH,5,2,4);
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.BREAD,3));
        double old=Tuning.INSTANCE.recruitmentChance.get("silverfish");
        Tuning.INSTANCE.recruitmentChance.put("silverfish",1.0);
        try{click(player,ally);click(player,ally);}finally{Tuning.INSTANCE.recruitmentChance.put("silverfish",old);}
        helper.assertTrue(Relations.ownerMatches(ally,player),"Recruitment event must set owner");
        helper.assertValueEqual(player.getMainHandItem().getCount(),2,"Duplicate right-click must consume only one item");
        player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        Vec3[] anchor={ally.position()};
        helper.runAfterDelay(2,()->{
            click(player,ally);anchor[0]=ally.position();
            helper.assertValueEqual(Data.mode(ally),Rules.Mode.STAY,"First empty click enters STAY");
            ally.setDeltaMovement(new Vec3(1,.5,1));
        });
        helper.runAfterDelay(22,()->{
            helper.assertTrue(ally.position().distanceToSqr(anchor[0])<.01,"STAY must remain stationary through live ticks");
            click(player,ally);
            helper.assertValueEqual(Data.mode(ally),Rules.Mode.PATROL,"Second empty click enters PATROL");
            var enemy=helper.spawn(EntityType.ZOMBIE,6,2,4);
            enemy.setNoAi(true);
            helper.succeedWhen(()->{
                helper.assertTrue(enemy.getHealth()<enemy.getMaxHealth(),"PATROL ally must actually hurt a nearby enemy");
                player.getServer().getPlayerList().remove(player);
            });
        });
    }
    private static void speciesTrade(GameTestHelper helper){
        ServerPlayer player=player(helper);
        var zombie=helper.spawn(EntityType.ZOMBIE,5,2,4);
        Relations.recruit(zombie,player);
        float max=zombie.getMaxHealth(),before=zombie.getHealth();
        zombie.hurtServer(helper.getLevel(),zombie.damageSources().generic(),3);
        helper.assertTrue(Math.abs(zombie.getMaxHealth()-(max-(before-zombie.getHealth())))<.01 && zombie.getMaxHealth()<max,
            "Zombie maximum-health loss must match damage after armor");
        float hurtHealth=zombie.getHealth();zombie.heal(10);
        helper.assertValueEqual(zombie.getHealth(),hurtHealth,"Zombie must refuse healing");
        var magma=helper.spawn(EntityType.MAGMA_CUBE,9,2,9);
        Relations.recruit(magma,player);
        var prey=helper.spawn(EntityType.ZOMBIE,10,2,9);
        Combat.onAttack(magma,prey);
        helper.assertTrue(prey.getVehicle()!=magma,"Magma cube must not engulf prey");
        var pillager=helper.spawn(EntityType.PILLAGER,4,2,5);
        Relations.recruit(pillager,player);
        player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        click(player,pillager);
        helper.assertTrue(player.containerMenu instanceof net.minecraft.world.inventory.MerchantMenu,"Normal right click must open vanilla merchant menu");
        // This used to crash even when shift-clicking an empty result slot.
        player.containerMenu.quickMoveStack(player,2);
        player.closeContainer();
        helper.runAfterDelay(5,()->finish(helper,player));
    }
}
