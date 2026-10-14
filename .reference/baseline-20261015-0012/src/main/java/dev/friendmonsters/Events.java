package dev.friendmonsters;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.MobSplitEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
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
        if(direct!=null && Combat.friendly(direct,victim)) {
            event.setCanceled(true);return;
        }
        if(direct instanceof ServerPlayer player && victim instanceof Mob mob &&
           Rules.ILLAGERS.contains(Data.kind(mob)) && Relations.ownerMatches(mob,player)) {
            Relations.betray(player);
        }
    }
    @SubscribeEvent
    public static void hurtPost(LivingDamageEvent.Post event){
        LivingEntity victim=event.getEntity();
        if(victim instanceof Mob mob && Data.owned(mob))Combat.wearZombie(mob,event.getNewDamage());
        Entity source=event.getSource().getEntity();
        if(source==null)source=event.getSource().getDirectEntity();
        if(source instanceof LivingEntity attacker && victim.isAlive())Combat.onAttack(attacker,victim);
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
    @SubscribeEvent
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
        double distance=128*128;
        for(Entity candidate:level.getAllEntities()){
            if(candidate instanceof Shulker shulker && Relations.ownerMatches(shulker,dead)
               && shulker.isAlive()){
                double d=shulker.distanceToSqr(dead);
                if(d<distance){closest=shulker;distance=d;}
            }
        }
        if(closest==null)return;
        List<ItemStack> recovered=Data.items(closest,"recovered");
        for(ItemEntity item:List.copyOf(event.getDrops())){
            recovered.add(item.getItem().copy());
        }
        Data.items(closest,"recovered",recovered);
        event.getDrops().clear();
    }
    private static void returnInventory(Shulker shulker){
        ServerPlayer player=Data.owner(shulker);
        if(player==null || player.level()!=shulker.level() || shulker.distanceToSqr(player)>16)return;
        List<ItemStack> items=Data.items(shulker,"recovered");
        if(items.isEmpty())return;
        Data.items(shulker,"recovered",List.of());
        for(ItemStack stack:items){
            ItemEntity drop=new ItemEntity((ServerLevel)shulker.level(),player.getX(),
                player.getY()+0.7,player.getZ(),stack.copy());
            drop.setPickUpDelay(0);shulker.level().addFreshEntity(drop);
        }
        Interactions.tell(player,"シュルカーがアイテムを返してくれました");
    }
    @SubscribeEvent
    public static void entityTick(EntityTickEvent.Post event){
        if(!(event.getEntity() instanceof Mob mob) ||
             !(mob.level() instanceof ServerLevel world))return;
        long now=world.getGameTime();
        if(now%10==0)Relations.tick(mob);
        if(Data.time(mob,"retreat")>0 && !Data.owned(mob)){
            if(now>=Data.time(mob,"retreat"))mob.discard();
            else mob.setDeltaMovement(mob.getDeltaMovement().add(0,.05,0));
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
        if(mob instanceof Slime s)Combat.digestion(s);
        if(mob instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon dragon)DragonDrive.tick(dragon);
        if(mob.hasEffect(FriendMonsters.CURSE) && now%50>=20){
            mob.setTarget(null);mob.getNavigation().stop();
        }
        if(Data.kind(mob).equals("elder_guardian") && now%600==0){
            ServerPlayer owner=Data.owner(mob);
            if(owner!=null){
                boolean cursed=false;
                for(Mob other:Relations.nearby(mob,Tuning.INSTANCE.elderCurseRadius))
                    if(Relations.targetable(other,owner)){
                        other.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            FriendMonsters.CURSE,Tuning.INSTANCE.elderCurseDurationTicks));
                        cursed=true;
                    }
                if(cursed)world.playSound(null,mob.blockPosition(),
                    net.minecraft.sounds.SoundEvents.ELDER_GUARDIAN_CURSE,
                    net.minecraft.sounds.SoundSource.HOSTILE,1,1);
            }
        }
    }
}
