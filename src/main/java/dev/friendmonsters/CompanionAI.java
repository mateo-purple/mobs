package dev.friendmonsters;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.phys.Vec3;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class CompanionAI {
    private CompanionAI(){}
    private static final Map<Mob,Control> CONTROLS=new WeakHashMap<>();
    private static final class Control {
        boolean installed,paused,previousNoAi;
        Vec3 destination,pausePosition;
        double speed;
        long lastJump;
    }

    /** Called before vanilla AI so native target goals cannot overwrite owner commands. */
    public static void beginTick(Mob mob) {
        if(!(mob.level() instanceof ServerLevel world)||!Data.owned(mob))return;
        Control control=CONTROLS.computeIfAbsent(mob,m->new Control());
        if(!control.installed){
            for(WrappedGoal wrapped:List.copyOf(mob.targetSelector.getAvailableGoals())){
                Goal original=wrapped.getGoal();
                mob.targetSelector.removeGoal(original);
                mob.targetSelector.addGoal(wrapped.getPriority(),new NativeTargetGoal(mob,original));
            }
            mob.goalSelector.addGoal(0,new CommandMovementGoal(mob));
            control.installed=true;
        }
        boolean stationary=Data.mode(mob)==Rules.Mode.STAY||Data.time(mob,"trading_until")>mob.level().getGameTime();
        if(stationary){
            setCombatTarget(mob,null);mob.getNavigation().stop();
            if(!control.paused){
                control.previousNoAi=mob.isNoAi()&&!Combat.isCursePaused(mob);
                control.pausePosition=mob.position();control.paused=true;
            }
            mob.setNoAi(true);mob.setDeltaMovement(Vec3.ZERO);
        }else if(world.getGameTime()%5==0||Data.text(mob,"combat_target").isEmpty())tick(mob);
        clearProtectedBrainTarget(mob);
        if(mob instanceof WitherBoss wither){
            LivingEntity chosen=combatTarget(mob);
            for(int head=0;head<3;head++)wither.setAlternativeTarget(head,chosen==null?0:chosen.getId());
        }
    }

    /** STAY stops residual flight, jumps, knockback and collision displacement too. */
    public static void endTick(Mob mob) {
        Control control=CONTROLS.get(mob);if(control==null)return;
        if(control.paused){
            mob.setNoAi(control.previousNoAi);control.paused=false;
            if(Data.owned(mob)&&mob.isAlive()){
                Vec3 at=Data.mode(mob)==Rules.Mode.STAY?anchor(mob):control.pausePosition;
                mob.setPos(at);mob.setDeltaMovement(Vec3.ZERO);
            }
        }
        if(!Data.owned(mob)||!mob.isAlive()||Data.mode(mob)==Rules.Mode.STAY||Data.time(mob,"trading_until")>mob.level().getGameTime())return;
        clearProtectedBrainTarget(mob);
        if(mob instanceof Piglin&&combatTarget(mob)==null){
            commandMovement(mob);
            if(control.destination!=null)mob.getBrain().setMemory(MemoryModuleType.WALK_TARGET,
                new net.minecraft.world.entity.ai.memory.WalkTarget(control.destination,(float)control.speed,3));
            else mob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        }
        if(mob instanceof Slime&&mob.getTarget()!=null)move(mob,mob.getTarget().position(),1);
        if(control.destination!=null&&(mob instanceof Slime||Data.kind(mob).equals("phantom")||
            Data.kind(mob).equals("wither")&&mob.getTarget()==null)){
            if(!(mob instanceof Slime)&&mob.getTarget()!=null)return;
            Vec3 difference=control.destination.subtract(mob.position());
            if(difference.lengthSqr()<1){mob.setDeltaMovement(mob.getDeltaMovement().scale(.6));return;}
            if(mob instanceof Slime){
                Vec3 direction=new Vec3(difference.x,0,difference.z).normalize();
                double y=mob.getDeltaMovement().y;
                if(mob.onGround()&&mob.level().getGameTime()-control.lastJump>=20){
                    y=.42;control.lastJump=mob.level().getGameTime();
                }
                mob.setDeltaMovement(direction.scale(.2*control.speed).add(0,y,0));
                mob.setYRot((float)(Math.atan2(direction.z,direction.x)*180/Math.PI)-90);
            }else mob.setDeltaMovement(mob.getDeltaMovement().scale(.65).add(difference.normalize().scale(.12*control.speed)));
        }
    }

    public static void tick(Mob mob) {
        if(!(mob.level() instanceof ServerLevel world)||!Data.owned(mob))return;
        String kind=Data.kind(mob);Rules.Mode mode=Data.mode(mob);
        Control control=CONTROLS.computeIfAbsent(mob,m->new Control());
        if(Rules.NON_COMBAT.contains(kind)||Rules.ILLAGERS.contains(kind)||Data.time(mob,"trading_until")>mob.level().getGameTime()){
            setCombatTarget(mob,null);
            if(!kind.equals("pillager")&&!kind.equals("enderman")||Data.time(mob,"trading_until")>mob.level().getGameTime())return;
        }
        if(mode==Rules.Mode.STAY){setCombatTarget(mob,null);mob.getNavigation().stop();return;}
        ServerPlayer owner=Data.owner(mob);
        if(mode==Rules.Mode.DISMISSED){
            setCombatTarget(mob,null);mob.getNavigation().stop();
            if(owner==null||owner.level()!=world||owner.distanceToSqr(mob)>48*48){Relations.release(mob);mob.discard();}
            return;
        }
        if(owner==null||owner.level()!=world){setCombatTarget(mob,null);return;}
        if(kind.equals("ender_dragon")&&mob.isVehicle()){setCombatTarget(mob,null);return;}
        boolean returning=kind.equals("wither")&&mob.distanceToSqr(owner)>
            Tuning.INSTANCE.witherReturnDistance*Tuning.INSTANCE.witherReturnDistance;
        LivingEntity selected=null;
        if(!Rules.ILLAGERS.contains(kind)&&!Rules.NON_COMBAT.contains(kind)&&!returning){
            if(kind.equals("warden")){
                double best=32*32;
                for(Mob candidate:Relations.nearby(mob,32))if(candidate!=mob){
                    double d=candidate.distanceToSqr(mob);
                    if(d<best){selected=candidate;best=d;}
                }
            }else{
                if(owner.tickCount-owner.getLastHurtMobTimestamp()<200)
                    selected=validAssistance(mob,owner.getLastHurtMob());
                if(selected==null&&owner.tickCount-owner.getLastHurtByMobTimestamp()<200)
                    selected=validAssistance(mob,owner.getLastHurtByMob());
                if(selected==null&&mob.tickCount-mob.getLastHurtByMobTimestamp()<200)
                    selected=validAssistance(mob,mob.getLastHurtByMob());
                if(selected==null)selected=validAssistance(mob,combatTarget(mob));
                if(selected==null&&(mode==Rules.Mode.PATROL||kind.equals("wither"))){
                    double radius=kind.equals("blaze")&&mode==Rules.Mode.PATROL?60:
                        kind.equals("wither")?42:Tuning.INSTANCE.patrolRadius;
                    double closest=radius*radius;
                    for(Mob candidate:Relations.nearby(mob,radius))
                        if(candidate!=mob&&Relations.targetable(candidate,owner)&&mob.distanceToSqr(candidate)<closest){
                            selected=candidate;closest=mob.distanceToSqr(candidate);
                        }
                }
            }
        }
        if(mode==Rules.Mode.PATROL&&selected!=null&&!kind.equals("blaze")&&
            selected.position().distanceToSqr(anchor(mob))>Math.pow(Tuning.INSTANCE.patrolRadius+16,2))selected=null;
        setCombatTarget(mob,selected);
        if(mode==Rules.Mode.FOLLOW&&mob.distanceToSqr(owner)>
            Tuning.INSTANCE.followTeleportDistance*Tuning.INSTANCE.followTeleportDistance&&!kind.equals("ender_dragon")){
            teleportNearOwner(mob,owner);
            if(returning)setCombatTarget(mob,null);
        }
        if(selected==null||returning)commandMovement(mob);
    }

    private static LivingEntity validAssistance(Mob mob,LivingEntity target){
        return target!=null&&target.isAlive()&&target.level()==mob.level()&&!Relations.protects(mob,target)&&
            target.distanceToSqr(mob)<96*96?target:null;
    }
    public static LivingEntity combatTarget(Mob mob){
        if(!(mob.level() instanceof ServerLevel level))return null;
        try{var entity=level.getEntity(java.util.UUID.fromString(Data.text(mob,"combat_target")));
            return entity instanceof LivingEntity living&&living.isAlive()?living:null;
        }catch(IllegalArgumentException ignored){return null;}
    }
    private static void setCombatTarget(Mob mob,LivingEntity selected){
        Data.set(mob,"combat_target",selected==null?"":selected.getStringUUID());
        if(mob instanceof Warden warden){
            if(selected!=null){
                if(warden.getTarget()!=selected)warden.setAttackTarget(selected);
                warden.increaseAngerAt(selected,80,false);
            }else warden.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        }
        if(mob instanceof Piglin piglin){
            piglin.getBrain().eraseMemory(MemoryModuleType.UNIVERSAL_ANGER);
            if(selected==null){
                piglin.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
                piglin.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
            }else{
                piglin.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET,selected);
                piglin.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT,selected.getUUID(),200L);
            }
        }
        if(Data.kind(mob).equals("creeper")&&Data.number(mob,"tnt")>0)mob.setTarget(null);
        else mob.setTarget(selected);
    }
    public static void clearProtectedBrainTarget(Mob mob){
        if(mob.getBrain().checkMemory(MemoryModuleType.ATTACK_TARGET,MemoryStatus.VALUE_PRESENT)){
            LivingEntity target=mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
            if(target!=null&&Relations.rejectTarget(mob,target)){
                mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
                mob.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
                mob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            }
        }
    }
    private static Vec3 anchor(Mob mob){return new Vec3(Data.decimal(mob,"anchor_x"),Data.decimal(mob,"anchor_y"),Data.decimal(mob,"anchor_z"));}
    private static void teleportNearOwner(Mob mob,ServerPlayer owner){
        for(int x:new int[]{2,-2,3,-3})for(int z:new int[]{2,-2}){
            Vec3 destination=owner.position().add(x,Rules.FLYING.contains(Data.kind(mob))?2:0,z);
            if(mob.level().noCollision(mob,mob.getBoundingBox().move(destination.subtract(mob.position())))){
                mob.teleportTo(destination.x,destination.y,destination.z);mob.getNavigation().stop();return;
            }
        }
    }
    private static boolean catNearby(Mob mob){
        if(!Data.kind(mob).equals("creeper"))return false;
        for(Mob nearby:Relations.nearby(mob,6))if(Data.kind(nearby).equals("cat")||Data.kind(nearby).equals("ocelot"))return true;
        return false;
    }
    private static void commandMovement(Mob mob){
        ServerPlayer owner=Data.owner(mob);
        if(owner==null||owner.level()!=mob.level()||catNearby(mob))return;
        if(Data.mode(mob)==Rules.Mode.FOLLOW){
            if(mob.distanceToSqr(owner)>4*4)move(mob,owner.position(),1.1);
            else{mob.getNavigation().stop();Control control=CONTROLS.get(mob);if(control!=null)control.destination=null;}
        }else if(Data.mode(mob)==Rules.Mode.PATROL){
            Vec3 center=anchor(mob);Control control=CONTROLS.get(mob);
            if(mob.position().distanceToSqr(center)>Tuning.INSTANCE.patrolRadius*Tuning.INSTANCE.patrolRadius)move(mob,center,1);
            else if(mob.level().getGameTime()%80==0||control.destination==null&&mob.getNavigation().isDone()){
                double dx=mob.getRandom().nextInt(15)-7,dz=mob.getRandom().nextInt(15)-7;
                move(mob,center.add(dx,0,dz),.8);
            }
        }
    }
    public static void move(Mob mob,Vec3 destination,double speed){
        if(Data.kind(mob).equals("ender_dragon"))return;
        Control control=CONTROLS.computeIfAbsent(mob,m->new Control());
        control.destination=Rules.FLYING.contains(Data.kind(mob))?destination.add(0,Math.max(2,mob.getBbHeight()),0):destination;
        control.speed=speed;
        if(Rules.FLYING.contains(Data.kind(mob)))
            mob.getMoveControl().setWantedPosition(control.destination.x,control.destination.y,control.destination.z,speed);
        else mob.getNavigation().moveTo(destination.x,destination.y,destination.z,speed);
    }
    private static final class CommandMovementGoal extends Goal {
        private final Mob mob;
        CommandMovementGoal(Mob mob){this.mob=mob;setFlags(mob instanceof Slime?EnumSet.of(Flag.MOVE,Flag.LOOK):EnumSet.of(Flag.MOVE));}
        public boolean canUse(){return Data.owned(mob)&&(!Rules.NON_COMBAT.contains(Data.kind(mob))||Data.kind(mob).equals("enderman"))&&
            Data.time(mob,"trading_until")<=mob.level().getGameTime()&&Data.mode(mob)!=Rules.Mode.STAY&&Data.mode(mob)!=Rules.Mode.DISMISSED&&
            mob.getTarget()==null&&!catNearby(mob);}
        public boolean requiresUpdateEveryTick(){return true;}
        public void tick(){commandMovement(mob);}
    }
    private static final class NativeTargetGoal extends Goal {
        private final Mob mob;private final Goal original;
        NativeTargetGoal(Mob mob,Goal original){this.mob=mob;this.original=original;setFlags(original.getFlags());}
        public boolean canUse(){return !Data.owned(mob)&&original.canUse();}
        public boolean canContinueToUse(){return !Data.owned(mob)&&original.canContinueToUse();}
        public boolean isInterruptable(){return original.isInterruptable();}
        public boolean requiresUpdateEveryTick(){return original.requiresUpdateEveryTick();}
        public void start(){original.start();}
        public void stop(){original.stop();}
        public void tick(){original.tick();}
    }
}
