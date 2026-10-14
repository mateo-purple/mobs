package dev.friendmonsters;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.MobSplitEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Container;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;
public final class Events {
    private Events(){}
    @SubscribeEvent
    public static void interact(PlayerInteractEvent.EntityInteract event) {
        if(!(event.getEntity() instanceof ServerPlayer p))return;
        Mob mob=event.getTarget() instanceof Mob m?m:event.getTarget() instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragonPart part?part.parentMob:null;
        if(mob==null)return;
        InteractionResult result=Interactions.handle(p,mob,event.getHand());
        if(result!=InteractionResult.PASS) {
            event.setCancellationResult(result);event.setCanceled(true);
        }
    }
    @SubscribeEvent
    public static void interactSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if(!(event.getEntity() instanceof ServerPlayer p))return;
        Mob mob=event.getTarget() instanceof Mob m?m:event.getTarget() instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragonPart part?part.parentMob:null;
        if(mob==null)return;
        InteractionResult result=Interactions.handle(p,mob,event.getHand());
        if(result!=InteractionResult.PASS) {
            event.setCancellationResult(result);event.setCanceled(true);
        }
    }
    @SubscribeEvent
    public static void damage(LivingIncomingDamageEvent event) {
        LivingEntity victim=event.getEntity();
        Entity direct=event.getSource().getEntity();
        if(direct==null)direct=event.getSource().getDirectEntity();
        // Deliberate aggression ends a truce before friendly-fire protection is evaluated.
        if(direct!=null && victim instanceof Mob mob) {
            if(Data.kind(mob).equals("piglin_brute")&&!Data.text(mob,"bribed").isEmpty()) {
                Data.remove(mob,"bribed");
                if(direct instanceof LivingEntity attacker)angerBrute(mob,attacker);
            }
            if(Rules.ILLAGERS.contains(Data.kind(mob))&&Data.owned(mob)&&
               !(direct instanceof LivingEntity ally&&Relations.sameOwner(ally,mob)&&!Data.kind(ally).equals("warden"))) {
                ServerPlayer owner=Data.owner(mob);
                if(owner!=null)Relations.betray(owner);
            }
        }
        if(direct!=null && Combat.friendly(direct,victim)) {
            event.setCanceled(true);return;
        }
    }
    @SubscribeEvent
    public static void heal(LivingHealEvent event) {
        if(event.getEntity() instanceof Mob mob&&Data.owned(mob)&&Rules.ZOMBIES.contains(Data.kind(mob)))
            event.setCanceled(true);
    }
    @SubscribeEvent
    public static void target(LivingChangeTargetEvent event) {
        if(event.getEntity() instanceof Mob mob&&event.getNewAboutToBeSetTarget()!=null&&
            Relations.rejectTarget(mob,event.getNewAboutToBeSetTarget()))event.setNewAboutToBeSetTarget(null);
    }
    @SubscribeEvent
    public static void companionGriefing(EntityMobGriefingEvent event) {
        if(Data.owned(event.getEntity())&&List.of("enderman","silverfish").contains(Data.kind(event.getEntity())))
            event.setCanGrief(false);
    }
    @SubscribeEvent
    public static void friendlyEffect(MobEffectEvent.Applicable event) {
        Entity source=event.getEffectSource();
        if(source!=null&&event.getEffectInstance().getEffect().value().getCategory()==MobEffectCategory.HARMFUL&&
           Combat.friendly(source,event.getEntity()))event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
    }
    private static void angerBrute(Mob brute,LivingEntity attacker) {
        brute.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT,attacker.getUUID(),600);
        brute.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET,attacker);
        brute.setTarget(attacker);
    }
    private static void revokeTreasureTruce(ServerPlayer player) {
        for(Mob brute:Relations.nearby(player,16))
            if(Data.kind(brute).equals("piglin_brute")&&!Data.text(brute,"bribed").isEmpty()) {
                Data.remove(brute,"bribed");angerBrute(brute,player);
            }
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void treasureBreak(BlockEvent.BreakEvent event) {
        if(!event.isCanceled()&&event.getPlayer() instanceof ServerPlayer player&&event.getState().is(BlockTags.GUARDED_BY_PIGLINS))
            revokeTreasureTruce(player);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void treasureUse(PlayerInteractEvent.RightClickBlock event) {
        if(!event.isCanceled()&&event.getEntity() instanceof ServerPlayer player&&
           event.getLevel().getBlockState(event.getPos()).is(BlockTags.GUARDED_BY_PIGLINS)&&
           event.getLevel().getBlockEntity(event.getPos()) instanceof Container)
            Data.set(player,"treasure_use",event.getLevel().getGameTime()+1);
    }
    @SubscribeEvent
    public static void treasureOpen(PlayerContainerEvent.Open event) {
        if(event.getEntity() instanceof ServerPlayer player&&Data.time(player,"treasure_use")>0&&
           Data.time(player,"treasure_use")>=player.level().getGameTime()) {
            Data.remove(player,"treasure_use");revokeTreasureTruce(player);
        }
    }
    @SubscribeEvent
    public static void hurtPost(LivingDamageEvent.Post event){
        LivingEntity victim=event.getEntity();
        if(victim instanceof Mob mob && Data.owned(mob))Species.wearZombie(mob,event.getNewDamage());
        Entity source=event.getSource().getEntity();
        if(source==null)source=event.getSource().getDirectEntity();
        if(event.getNewDamage()>0&&source instanceof LivingEntity attacker && victim.isAlive())Combat.onAttack(attacker,victim);
    }
    @SubscribeEvent
    public static void splits(MobSplitEvent event){
        if(Data.owned(event.getParent()) && (Data.kind(event.getParent()).equals("slime") ||
             Data.kind(event.getParent()).equals("magma_cube")))event.setCanceled(true);
    }
    @SubscribeEvent
    public static void death(LivingDeathEvent event){
        if(event.getEntity() instanceof Slime slime && Data.owned(slime))Combat.releasePrey(slime);
    }
    @SubscribeEvent
    public static void projectileSpawn(EntityJoinLevelEvent event){
        if(!event.getLevel().isClientSide())Combat.projectileJoin(event.getEntity());
    }
    @SubscribeEvent
    public static void projectileHit(ProjectileImpactEvent event){
        Projectile projectile=event.getProjectile();
        if(Data.flag(projectile,"tnt_projectile")){
            Combat.blast(projectile);event.setCanceled(true);
        }else if(Data.flag(projectile,"boost"))Combat.boostedBlast(projectile);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void drops(LivingDropsEvent event){
        if(!(event.getEntity().level() instanceof ServerLevel level))return;
        if(event.getEntity() instanceof Shulker shulker && Data.owned(shulker)){
            for(ItemStack stack:Data.items(shulker,"recovered"))
                event.getDrops().add(new ItemEntity(level,shulker.getX(),shulker.getY(),
                    shulker.getZ(),stack.copy()));
            Data.items(shulker,"recovered",List.of());
        }
        if(!(event.getEntity() instanceof ServerPlayer dead))return;
        Shulker closest=null;
        double distance=Double.POSITIVE_INFINITY;
        for(Entity candidate:level.getAllEntities()){
            if(candidate instanceof Shulker shulker && Relations.ownerMatches(shulker,dead)
               && shulker.isAlive()){
                double d=shulker.distanceToSqr(dead);
                if(d<distance){closest=shulker;distance=d;}
            }
        }
        if(closest==null)return;
        List<ItemStack> recovered=Data.items(closest,"recovered");
        for(ItemEntity item:List.copyOf(event.getDrops()))if(!item.getItem().isEmpty())recovered.add(item.getItem().copy());
        Data.items(closest,"recovered",recovered);
        event.getDrops().clear();
    }
    private static void returnInventory(Shulker shulker){
        ServerPlayer player=Data.owner(shulker);
        if(player==null || !player.isAlive() || player.isSpectator() || player.level()!=shulker.level() || shulker.distanceToSqr(player)>16)return;
        List<ItemStack> items=Data.items(shulker,"recovered");
        if(items.isEmpty())return;
        List<ItemStack> remaining=new ArrayList<>();
        for(ItemStack stack:items){
            ItemEntity drop=new ItemEntity((ServerLevel)shulker.level(),player.getX(),
                player.getY()+0.7,player.getZ(),stack.copy());
            drop.setPickUpDelay(0);drop.setTarget(player.getUUID());
            if(!shulker.level().addFreshEntity(drop))remaining.add(stack);
        }
        Data.items(shulker,"recovered",remaining);
        if(remaining.size()<items.size())Interactions.tell(player,"シュルカーがアイテムを返してくれました");
    }
    @SubscribeEvent
    public static void beforeEntityTick(EntityTickEvent.Pre event) {
        if(event.getEntity() instanceof Mob mob&&mob.level() instanceof ServerLevel) {
            Species.enforceZombieHealth(mob);
            Combat.curseTick(mob);
            CompanionAI.beginTick(mob);
        }
    }
    @SubscribeEvent
    public static void entityTick(EntityTickEvent.Post event){
        if(!(event.getEntity() instanceof Mob mob) ||
             !(mob.level() instanceof ServerLevel world))return;
        Combat.endCurseTick(mob);
        CompanionAI.endTick(mob);
        long now=world.getGameTime();
        if(now%10==0)Relations.tick(mob);
        if(Data.time(mob,"retreat")>0 && !Data.owned(mob)){
            if(now>=Data.time(mob,"retreat"))mob.discard();
            else {
                if(!Data.flag(mob,"retreat_active")) {
                    Data.set(mob,"retreat_was_no_ai",mob.isNoAi());Data.set(mob,"retreat_active",true);
                }
                mob.setNoAi(true);mob.setTarget(null);mob.getNavigation().stop();
                var nearby=world.getNearestPlayer(mob,64);
                Vec3 away=nearby==null?new Vec3(1,0,0):mob.position().subtract(nearby.position()).normalize();
                mob.setDeltaMovement(away.x*.6,.25,away.z*.6);
            }
            return;
        }
        if(!Data.owned(mob))return;
        if(now%5==0){
            CompanionAI.tick(mob);
            Species.tick(mob);
            Rewards.tick(mob);
            if(mob instanceof Creeper c)Combat.creeperShoot(c);
            if(mob instanceof Shulker s)returnInventory(s);
        }
        if(mob instanceof Creeper c && Data.number(c,"tnt")>0)c.setSwellDir(-1);
        if(mob instanceof Slime s) {
            Combat.slimeAttack(s);
            if(Data.kind(s).equals("slime"))Combat.digestion(s);
        }
        if(mob instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon dragon)DragonDrive.tick(dragon);
    }
}
