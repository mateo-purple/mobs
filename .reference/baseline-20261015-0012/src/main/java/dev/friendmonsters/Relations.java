package dev.friendmonsters;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import java.util.List;
public final class Relations {
    private Relations() {}
    public static boolean ownerMatches(Entity m,ServerPlayer p) {
        return p!=null && Data.text(m,"owner").equals(p.getStringUUID());
    }
    public static boolean sameOwner(Entity a,Entity b) {
        return Data.owned(a) && Data.text(a,"owner").equals(Data.text(b,"owner"));
    }
    public static List<Mob> nearby(Entity m,double radius) {
        return m.level().getEntitiesOfClass(Mob.class,m.getBoundingBox().inflate(radius),Mob::isAlive);
    }
    public static boolean protects(LivingEntity a,LivingEntity b) {
        if(a==b)return true;
        String ak=Data.kind(a),bk=Data.kind(b);
        if(Data.owned(a)) {
            if(Data.text(a,"owner").equals(b.getStringUUID()))return true;
            if(ak.equals("warden"))return false;
            if(sameOwner(a,b)||Rules.ILLAGERS.contains(ak))return true;
        }
        if(Data.owned(b)) {
            if((ak.equals("iron_golem")||ak.equals("snow_golem"))&&!bk.equals("pillager"))return true;
            if(Rules.ILLAGERS.contains(bk)&&!ak.equals("iron_golem"))return true;
        }
        if(ak.equals("piglin_brute")) {
            String bribe=Data.text(a,"bribed");
            if(!bribe.isEmpty() && (bribe.equals(b.getStringUUID())||
              (bk.equals("piglin")&&bribe.equals(Data.text(b,"owner")))))return true;
        }
        return false;
    }
    public static boolean targetable(LivingEntity t,ServerPlayer owner) {
        if(!t.isAlive()||t==owner||Data.owned(t)||t instanceof ServerPlayer)return false;
        if(!(t instanceof Mob m))return false;
        if(owner!=null && (m.getTarget()==owner||(m.getTarget()!=null&&
           ownerMatches(m.getTarget(),owner))))return true;
        return t instanceof Enemy&&!List.of("enderman","zombified_piglin","piglin","hoglin").contains(Data.kind(t));
    }
    public static void recruit(Mob m,ServerPlayer p) {
        adopt(m,p,false);
        String type=Data.kind(m);
        if(type.equals("piglin")||type.equals("elder_guardian")) {
            String group=type.equals("piglin")?"piglin":"guardian";
            for(Mob other:nearby(m,Tuning.INSTANCE.groupRadius))
                if(other!=m&&!Data.owned(other)&&Data.kind(other).equals(group))adopt(other,p,false);
        }
        if(type.equals("phantom")) {
            p.resetStat(net.minecraft.stats.Stats.CUSTOM.get(net.minecraft.stats.Stats.TIME_SINCE_REST));
            for(Mob other:nearby(m,64))
                if(other!=m&&Data.kind(other).equals("phantom")&&!Data.owned(other)){
                    other.setTarget(null);Data.set(other,"retreat",m.level().getGameTime()+100);
                }
        }
    }
    static void adopt(Mob m,ServerPlayer p,boolean temporary){
        Data.set(m,"owner",p.getStringUUID());
        Data.set(m,"temporary",temporary);Data.mode(m,Rules.Mode.FOLLOW);Data.anchor(m);
        m.setPersistenceRequired();m.setTarget(null);m.getNavigation().stop();
        Species.onRecruit(m);Interactions.hearts(m,true);
    }
    public static void dismiss(Mob m){
        Data.set(m,"dismiss_at",m.level().getGameTime());m.setTarget(null);m.getNavigation().stop();
    }
    public static void release(Mob m){
        Data.remove(m,"owner");Data.remove(m,"temporary");Data.remove(m,"mode");m.setTarget(null);
    }
    public static void betray(ServerPlayer p) {
        for(ServerLevel l:p.level().getServer().getAllLevels())
          for(Entity e:l.getAllEntities())
            if(e instanceof Mob m&&ownerMatches(m,p)&&Rules.ILLAGERS.contains(Data.kind(m))){
                release(m);m.setTarget(p);
            }
        Interactions.tell(p,"邪悪な村人との停戦が終わりました");
    }
    public static void tick(Mob m) {
        String type=Data.kind(m);ServerPlayer p=Data.owner(m);
        if(Data.owned(m)){
            if(Data.flag(m,"temporary")) {
                boolean close=false;
                for(Mob x:nearby(m,Tuning.INSTANCE.groupRadius))
                  if(Data.kind(x).equals("pillager")&&sameOwner(m,x)&&Data.mode(x)!=Rules.Mode.DISMISSED){close=true;break;}
                if(!close){release(m);return;}
            }
            if(type.equals("pillager")&&p!=null&&Data.mode(m)!=Rules.Mode.DISMISSED)
                for(Mob x:nearby(m,Tuning.INSTANCE.groupRadius))
                    if(x!=m&&Rules.ILLAGERS.contains(Data.kind(x))&&!Data.owned(x))adopt(x,p,true);
            if(m.getTarget()!=null&&protects(m,m.getTarget()))m.setTarget(null);
            return;
        }
        if(type.equals("piglin_brute"))
            for(Mob x:nearby(m,24))
              if(Data.kind(x).equals("piglin")&&Data.owned(x)&&!Data.text(m,"bribed").equals(Data.text(x,"owner"))){
                  m.setTarget(x);break;
              }
        if(type.equals("iron_golem"))
            for(Mob x:nearby(m,20))if(Data.kind(x).equals("pillager")&&Data.owned(x)){m.setTarget(x);break;}
        if(m instanceof Enemy&&m.getTarget()==null&&!type.equals("enderman"))
            for(Mob x:nearby(m,16))if(Data.controlled(x)&&!Rules.ILLAGERS.contains(Data.kind(x))&&!protects(m,x)){
                m.setTarget(x);break;
            }
    }
}

