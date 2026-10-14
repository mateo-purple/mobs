package dev.friendmonsters;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
public final class DragonDrive {
    private DragonDrive(){}
    public static boolean eligible(ServerPlayer rider,EnderDragon dragon){
        return dragon.level().dimension()==Level.END &&
           rider.getVehicle()==dragon && Relations.ownerMatches(dragon,rider);
    }
    public static void input(ServerPlayer rider,float forward,float sideways,float rise,int action){
        if(!(rider.getVehicle() instanceof EnderDragon dragon) || !eligible(rider,dragon))return;
        long now=dragon.level().getGameTime();
        if(action==1 || action==2){shoot(rider,dragon,action);return;}
        Data.set(dragon,"input_at",now);
        Data.set(dragon,"forward",(double)Math.clamp(forward,-1,1));
        Data.set(dragon,"sideways",(double)Math.clamp(sideways,-1,1));
        Data.set(dragon,"rise",(double)Math.clamp(rise,-1,1));
    }
    public static void tick(EnderDragon dragon){
        if(!(dragon.getFirstPassenger() instanceof ServerPlayer player) ||
           !eligible(player,dragon))return;
        dragon.getPhaseManager().setPhase(EnderDragonPhase.HOVERING);
        dragon.setYRot(player.getYRot());
        dragon.setXRot(player.getXRot());
        if(dragon.level().getGameTime()-Data.time(dragon,"input_at")>8)return;
        float yaw=player.getYRot();
        Vec3 ahead=player.getLookAngle().normalize();
        Vec3 right=new Vec3(Math.cos(Math.toRadians(yaw)),0,Math.sin(Math.toRadians(yaw)));
        Vec3 move=ahead.scale(Data.decimal(dragon,"forward"))
            .add(right.scale(Data.decimal(dragon,"sideways")))
            .add(0,Data.decimal(dragon,"rise"),0);
        if(move.lengthSqr()>1)move=move.normalize();
        dragon.setDeltaMovement(move.scale(Tuning.INSTANCE.dragonFlightSpeed));
        dragon.setPos(dragon.position().add(dragon.getDeltaMovement()));
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
            cloud.addEffect(new MobEffectInstance(MobEffects.INSTANT_DAMAGE,1,1));
            level.addFreshEntity(cloud);
        }
    }
}
