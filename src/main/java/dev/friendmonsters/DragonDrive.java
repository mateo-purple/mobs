package dev.friendmonsters;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
public final class DragonDrive {
    private DragonDrive(){}
    public static void bootstrap(){ CompanionDragonPhase.TYPE.getId(); }
    public static boolean eligible(ServerPlayer rider,EnderDragon dragon){
        return dragon.level().dimension()==Level.END &&
           rider.getVehicle()==dragon && Relations.ownerMatches(dragon,rider);
    }
    public static void input(ServerPlayer rider,float forward,float sideways,float rise,int action){
        if(!(rider.getVehicle() instanceof EnderDragon dragon) || !eligible(rider,dragon))return;
        if(!Float.isFinite(forward)||!Float.isFinite(sideways)||!Float.isFinite(rise))return;
        long now=dragon.level().getGameTime();
        if(action==1 || action==2){shoot(rider,dragon,action);return;}
        Data.set(dragon,"input_at",now);
        Data.set(dragon,"forward",(double)Math.clamp(forward,-1,1));
        Data.set(dragon,"sideways",(double)Math.clamp(sideways,-1,1));
        Data.set(dragon,"rise",(double)Math.clamp(rise,-1,1));
    }
    public static void tick(EnderDragon dragon){
        if(!Data.owned(dragon)||!dragon.isAlive())return;
        if(!Data.flag(dragon,"boss_released") && dragon.level() instanceof ServerLevel level){
            var fight=level.getDragonFight();
            if(fight!=null && fight.hasPreviouslyKilledDragon() && dragon.getUUID().equals(fight.getDragonUUID()))
                fight.setDragonKilled(dragon);
            Data.set(dragon,"boss_released",true);
        }
        dragon.setDragonFight(null);
        if(dragon.getPhaseManager().getCurrentPhase().getPhase()!=CompanionDragonPhase.TYPE)
            dragon.getPhaseManager().setPhase(CompanionDragonPhase.TYPE);
    }
    public static void advance(EnderDragon dragon){
        if(!Data.owned(dragon)||!dragon.isAlive())return;
        dragon.setDragonFight(null);
        if(dragon.getFirstPassenger() instanceof ServerPlayer player && eligible(player,dragon)){
            flyRidden(player,dragon);return;
        }
        if(dragon.isVehicle())dragon.ejectPassengers();
        if(Data.mode(dragon)==Rules.Mode.STAY){dragon.setDeltaMovement(Vec3.ZERO);return;}
        ServerPlayer owner=Data.owner(dragon);
        if(owner==null||owner.level()!=dragon.level()){dragon.setDeltaMovement(Vec3.ZERO);return;}
        LivingEntity target=dragon.getTarget();
        Vec3 destination=null;
        if(target!=null && target.isAlive() && !Relations.protects(dragon,target)){
            destination=target.position().add(0,8,0);
            long now=dragon.level().getGameTime();
            if(now-Data.time(dragon,"support_shot")>=40 && dragon.distanceToSqr(target)<60*60){
                Vec3 origin=dragon.head.getEyePosition();
                Vec3 aim=target.getEyePosition().subtract(origin).normalize();
                DragonFireball ball=new DragonFireball(dragon.level(),dragon,aim);
                ball.setPos(origin.add(aim.scale(2)));
                dragon.level().addFreshEntity(ball);Data.set(dragon,"support_shot",now);
            }
        }else if(Data.mode(dragon)==Rules.Mode.FOLLOW && dragon.distanceToSqr(owner)>12*12){
            destination=owner.position().add(0,6,0);
        }else if(Data.mode(dragon)==Rules.Mode.PATROL){
            long now=dragon.level().getGameTime();
            if(now%80==0||Data.time(dragon,"patrol_at")==0){
                Data.set(dragon,"patrol_x",Data.decimal(dragon,"anchor_x")+dragon.getRandom().nextInt(17)-8.0);
                Data.set(dragon,"patrol_z",Data.decimal(dragon,"anchor_z")+dragon.getRandom().nextInt(17)-8.0);
                Data.set(dragon,"patrol_at",now);
            }
            destination=new Vec3(Data.decimal(dragon,"patrol_x"),Data.decimal(dragon,"anchor_y")+6,Data.decimal(dragon,"patrol_z"));
        }
        Vec3 movement=destination==null?Vec3.ZERO:destination.subtract(dragon.position());
        if(movement.lengthSqr()>1)movement=movement.normalize().scale(.6);
        if(movement.horizontalDistanceSqr()>0.01)
            dragon.setYRot((float)Math.toDegrees(Math.atan2(-movement.x,movement.z))+180);
        dragon.setDeltaMovement(movement);dragon.move(MoverType.SELF,movement);
    }
    private static void flyRidden(ServerPlayer player,EnderDragon dragon){
        dragon.setYRot(player.getYRot());
        dragon.setXRot(player.getXRot());
        if(dragon.level().getGameTime()-Data.time(dragon,"input_at")>8){dragon.setDeltaMovement(Vec3.ZERO);return;}
        float yaw=player.getYRot();
        Vec3 ahead=player.getLookAngle().normalize();
        Vec3 right=new Vec3(Math.cos(Math.toRadians(yaw)),0,Math.sin(Math.toRadians(yaw)));
        Vec3 move=ahead.scale(Data.decimal(dragon,"forward"))
            .add(right.scale(Data.decimal(dragon,"sideways")))
            .add(0,Data.decimal(dragon,"rise"),0);
        if(move.lengthSqr()>1)move=move.normalize();
        dragon.setDeltaMovement(move.scale(Tuning.INSTANCE.dragonFlightSpeed));
        dragon.move(MoverType.SELF,dragon.getDeltaMovement());
    }
    private static void shoot(ServerPlayer rider,EnderDragon dragon,int kind){
        if(!(dragon.level() instanceof ServerLevel level))return;
        long now=level.getGameTime();
        String key=kind==1?"fireball_at":"breath_at";
        if(now-Data.time(dragon,key)<(kind==1?18:35))return;
        Data.set(dragon,key,now);
        Vec3 aim=rider.getLookAngle().normalize();
        Vec3 origin=dragon.getEyePosition().add(aim.scale(3));
        if(kind==1){
            DragonFireball projectile=new DragonFireball(level,dragon,aim);
            projectile.setPos(origin);
            level.addFreshEntity(projectile);
        }else{
            Vec3 spot=origin.add(aim.scale(7));
            AreaEffectCloud cloud=new AreaEffectCloud(level,spot.x,spot.y,spot.z);
            cloud.setOwner(dragon);
            cloud.setRadius(3);
            cloud.setDuration(130);
            cloud.setParticle(net.minecraft.core.particles.ParticleTypes.DRAGON_BREATH);
            cloud.addEffect(new MobEffectInstance(MobEffects.INSTANT_DAMAGE,1,1));
            level.addFreshEntity(cloud);
        }
    }
}
