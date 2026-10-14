package dev.friendmonsters;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;
import java.util.List;
public final class CompanionAI {
    private CompanionAI(){}
    public static void tick(Mob mob) {
        if(!(mob.level() instanceof ServerLevel world) || !Data.owned(mob))return;
        String kind=Data.kind(mob);
        if(kind.equals("hoglin") || kind.equals("enderman")) {
            mob.setTarget(null);return;
        }
        ServerPlayer owner=Data.owner(mob);
        if(owner==null)return;
        Rules.Mode mode=Data.mode(mob);
        if(mode==Rules.Mode.DISMISSED){
            mob.getNavigation().stop();mob.setTarget(null);
            if(owner.distanceToSqr(mob)>48*48)mob.discard();
            return;
        }
        if(Rules.ILLAGERS.contains(kind)){
            mob.setTarget(null);
            if(!kind.equals("pillager")){mob.getNavigation().stop();return;}
        }
        if(mode==Rules.Mode.STAY){
            mob.setTarget(null);mob.getNavigation().stop();
            mob.setDeltaMovement(new Vec3(0,mob.getDeltaMovement().y,0));
            return;
        }
        if(mob.level()!=owner.level())return;
        if(kind.equals("ender_dragon")&&mob.isVehicle()){
            mob.setTarget(null);return;
        }
        if(kind.equals("warden")){
            LivingEntity target=null;double best=32*32;
            for(Mob candidate:Relations.nearby(mob,32)){
                if(candidate==mob || !candidate.isAlive())continue;
                double d=candidate.distanceToSqr(mob);
                if(d<best){target=candidate;best=d;}
            }
            mob.setTarget(target);
        }else if(!Rules.ILLAGERS.contains(kind)) {
            LivingEntity selected=null;
            if(owner.getLastHurtMob() instanceof LivingEntity hit && hit.isAlive() &&
                 !Relations.protects(mob,hit))selected=hit;
            if(selected==null && owner.getLastHurtByMob() instanceof LivingEntity attacker &&
                 attacker.isAlive()&&!Relations.protects(mob,attacker))selected=attacker;
            if(selected==null && mob.getLastHurtByMob() instanceof LivingEntity attacker &&
                 attacker.isAlive()&&!Relations.protects(mob,attacker))selected=attacker;
            if(selected==null && (mode==Rules.Mode.PATROL || kind.equals("wither"))) {
                double radius=kind.equals("blaze")&&mode==Rules.Mode.PATROL?60:
                              kind.equals("wither")?42:Tuning.INSTANCE.patrolRadius;
                double closest=radius*radius;
                for(Mob candidate:Relations.nearby(mob,radius))
                    if(candidate!=mob && Relations.targetable(candidate,owner) &&
                       mob.distanceToSqr(candidate)<closest){
                        selected=candidate;closest=mob.distanceToSqr(candidate);
                    }
            }
            if(selected!=null)mob.setTarget(selected);
            else if(mob.getTarget()!=null &&
                     (!mob.getTarget().isAlive() || Relations.protects(mob,mob.getTarget())))
                mob.setTarget(null);
        }
        if(kind.equals("wither") && mob.distanceToSqr(owner)>
             Tuning.INSTANCE.witherReturnDistance*Tuning.INSTANCE.witherReturnDistance)mob.setTarget(null);
        if(kind.equals("creeper")&&Data.number(mob,"tnt")>0)mob.setTarget(null);
        if(mode==Rules.Mode.FOLLOW && mob.distanceToSqr(owner)>
             Tuning.INSTANCE.followTeleportDistance*Tuning.INSTANCE.followTeleportDistance &&
             !kind.equals("ender_dragon")){
            mob.teleportTo(owner.getX()+1,owner.getY(),owner.getZ()+1);
        }
        if(mode==Rules.Mode.FOLLOW){
            if(mob.distanceToSqr(owner)>8*8 && mob.getTarget()==null)
                move(mob,owner.position(),1.1);
            else if(mob.distanceToSqr(owner)<4*4 && mob.getTarget()==null)
                mob.getNavigation().stop();
        } else if(mode==Rules.Mode.PATROL && mob.getTarget()==null) {
            double x=Data.decimal(mob,"anchor_x"),z=Data.decimal(mob,"anchor_z");
            Vec3 anchor=new Vec3(x,Data.decimal(mob,"anchor_y"),z);
            if(mob.position().distanceToSqr(anchor)>Tuning.INSTANCE.patrolRadius*Tuning.INSTANCE.patrolRadius)
                move(mob,anchor,1);
            else if(world.getGameTime()%80==0){
                double dx=world.random.nextInt(15)-7,dz=world.random.nextInt(15)-7;
                move(mob,anchor.add(dx,0,dz),0.8);
            }
        }
    }
    public static void move(Mob mob,Vec3 destination,double speed) {
        if(Rules.FLYING.contains(Data.kind(mob))){
            mob.getMoveControl().setWantedPosition(destination.x,
                destination.y+Math.max(1,mob.getBbHeight()),destination.z,speed);
        }else mob.getNavigation().moveTo(destination.x,destination.y,destination.z,speed);
    }
}

