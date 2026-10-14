package dev.friendmonsters;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import java.util.List;
public final class Species {
    private Species() {}
    public static boolean firstDragonDefeated(Mob dragon) {
        if(!(dragon.level() instanceof ServerLevel level))return false;
        var fight=level.getDragonFight();
        return fight!=null && fight.hasPreviouslyKilledDragon();
    }
    public static void onRecruit(Mob mob) {
        String type=Data.kind(mob);
        if(type.equals("ghast"))mob.setCustomName(net.minecraft.network.chat.Component.literal("Happy Ghast"));
        if(type.equals("cave_spider"))mob.setHealth(Math.min(mob.getHealth(),Tuning.INSTANCE.caveSpiderStartingHp));
        if(type.equals("magma_cube") && mob instanceof MagmaCube cube) {
            Data.set(mob,"magma_stage",cube.getSize()>=4?2:cube.getSize()>=2?1:0);
            Data.set(mob,"magma_next",mob.level().getGameTime()+Tuning.INSTANCE.magmaDecayTicks);
        }
        if(Rules.ZOMBIES.contains(type)) {
            Data.set(mob,"initial_max_hp",mob.getMaxHealth());
            Data.set(mob,"permanent_max_hp",mob.getMaxHealth());
        }
        if(type.equals("enderman"))Data.set(mob,"last_trade",mob.level().getGameTime());
    }
    public static void applyPermanentPotion(Mob mob,MobEffectInstance potion) {
        if(!potion.getEffect().value().isInstantenous())
            mob.addEffect(new MobEffectInstance(potion.getEffect(),-1,potion.getAmplifier(),false,false,true));
    }
    public static void feedMagma(Mob m) {
        if(!(m instanceof MagmaCube cube))return;
        int stage=Data.number(m,"magma_stage");
        if(stage>=2) {
            m.addEffect(new MobEffectInstance(FriendMonsters.HEAT, -1));
        } else {
            stage++;Data.set(m,"magma_stage",stage);
            cube.setSize(stage==1?2:4,false);
        }
        Data.set(m,"magma_next",m.level().getGameTime()+Tuning.INSTANCE.magmaDecayTicks);
    }
    public static void clearPenalty(Mob m) {
        restore(m,Attributes.MOVEMENT_SPEED,"speed");
        restore(m,Attributes.ATTACK_DAMAGE,"damage");
    }
    private static void restore(Mob m,net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr,String name) {
        var value=m.getAttribute(attr);
        if(value!=null&&Data.flag(m,"adjusted_"+name))value.setBaseValue(Data.decimal(m,"original_"+name));
        Data.remove(m,"adjusted_"+name);
    }
    private static void updatePenalty(Mob m,boolean penalty) {
        adjust(m,Attributes.MOVEMENT_SPEED,"speed",penalty);
        adjust(m,Attributes.ATTACK_DAMAGE,"damage",penalty);
    }
    private static void adjust(Mob m,net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr,
                               String name,boolean penalty) {
        var a=m.getAttribute(attr);if(a==null)return;
        if(!Data.flag(m,"adjusted_"+name)) {
            Data.set(m,"original_"+name,a.getBaseValue());
            Data.set(m,"adjusted_"+name,true);
        }
        a.setBaseValue(Data.decimal(m,"original_"+name)*(penalty?0.8:1.0));
    }
    public static void tick(Mob mob) {
        if(!(mob.level() instanceof ServerLevel world))return;
        long now=world.getGameTime();
        String type=Data.kind(mob);
        ServerPlayer owner=Data.owner(mob);
        if(type.equals("magma_cube")&&mob instanceof MagmaCube cube) {
            if(world.dimension()==Level.OVERWORLD && now>=Data.time(mob,"magma_next")) {
                int stage=Data.number(mob,"magma_stage");
                if(stage>0) {
                    stage--;Data.set(mob,"magma_stage",stage);
                    cube.setSize(stage==1?2:1,false);
                } else mob.hurtServer(world,mob.damageSources().magic(),1);
                Data.set(mob,"magma_next",now+Tuning.INSTANCE.magmaDecayTicks);
                if(stage<2)mob.removeEffect(FriendMonsters.HEAT);
            } else if(world.dimension()!=Level.OVERWORLD)
                Data.set(mob,"magma_next",now+Tuning.INSTANCE.magmaDecayTicks);
        }
        if(Rules.ZOMBIES.contains(type)) {
            boolean desert=world.getBiome(mob.blockPosition()).is(Biomes.DESERT);
            boolean penalty=Rules.weakened(type,world.getDayTime(),
                world.dimension().identifier().getPath(),desert,mob.isInWater());
            updatePenalty(mob,penalty);
            double hpMax=Data.decimal(mob,"permanent_max_hp");
            if(hpMax>0 && mob.getHealth()>hpMax)mob.setHealth((float)hpMax);
        }
        if(type.equals("spider")) {
            for(MobEffectInstance effect:Data.load(mob,"potions",MobEffectInstance.CODEC.listOf(),List.of()))
                if(!mob.hasEffect(effect.getEffect()))applyPermanentPotion(mob,effect);
        }
        if(owner!=null && Data.mode(mob)==Rules.Mode.FOLLOW && type.equals("silverfish"))
            owner.addEffect(new MobEffectInstance(MobEffects.HASTE,50,0,true,false));
        if(owner!=null && Data.mode(mob)==Rules.Mode.FOLLOW && type.equals("endermite") &&
           now%Tuning.INSTANCE.endermiteSpawnIntervalTicks==0 &&
           world.random.nextDouble()<Tuning.INSTANCE.endermiteSpawnChance) {
            int x=owner.blockPosition().getX()+world.random.nextInt(25)-12;
            int z=owner.blockPosition().getZ()+world.random.nextInt(25)-12;
            int y=world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z);
            if(Math.abs(y-owner.getY())<16 && world.getEntitiesOfClass(EnderMan.class,
                owner.getBoundingBox().inflate(28)).size()<8) {
                EnderMan spawn=EntityType.ENDERMAN.create(world,net.minecraft.world.entity.EntitySpawnReason.EVENT);
                if(spawn!=null){spawn.snapTo(x+0.5,y,z+0.5,0,0);world.addFreshEntity(spawn);}
            }
        }
        if(type.equals("enderman")&&now-Data.time(mob,"last_trade")>=Tuning.INSTANCE.endermanDepartureTicks) {
            mob.discard();
        }
    }
}

