package dev.friendmonsters;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.Level;
import java.util.List;
public final class Species {
    private Species() {}
    private static final Identifier ECOLOGY_PENALTY=Identifier.fromNamespaceAndPath(FriendMonsters.ID,"ecology_penalty");
    public static boolean firstDragonDefeated(Mob dragon) {
        if(!(dragon.level() instanceof ServerLevel level))return false;
        var fight=level.getDragonFight();
        return fight!=null && fight.hasPreviouslyKilledDragon();
    }
    public static void onRecruit(Mob mob) {
        String type=Data.kind(mob);
        if(type.equals("ghast"))mob.setCustomName(net.minecraft.network.chat.Component.literal("Happy Ghast"));
        if(type.equals("cave_spider")) {
            var health=mob.getAttribute(Attributes.MAX_HEALTH);
            if(health!=null)health.setBaseValue(Tuning.INSTANCE.caveSpiderStartingHp);
            mob.setHealth(Math.min(mob.getHealth(),Tuning.INSTANCE.caveSpiderStartingHp));
        }
        if(type.equals("magma_cube") && mob instanceof MagmaCube cube) {
            Data.set(mob,"magma_stage",cube.getSize()>=4?2:cube.getSize()>=2?1:0);
            Data.set(mob,"magma_next",mob.level().getGameTime()+Tuning.INSTANCE.magmaDecayTicks);
        }
        if(Rules.ZOMBIES.contains(type)) {
            Data.set(mob,"initial_max_hp",mob.getMaxHealth());
            Data.set(mob,"permanent_max_hp",mob.getMaxHealth());
            Data.set(mob,"permanent_health",mob.getHealth());
        }
        if(type.equals("elder_guardian"))Data.set(mob,"elder_next_curse",mob.level().getGameTime()+600);
        if(Data.flag(mob,"retreat_active"))mob.setNoAi(Data.flag(mob,"retreat_was_no_ai"));
        Data.remove(mob,"retreat");Data.remove(mob,"retreat_active");Data.remove(mob,"retreat_was_no_ai");
        if(type.equals("enderman"))Data.set(mob,"last_trade",mob.level().getGameTime());
    }
    public static void applyPermanentPotion(Mob mob,MobEffectInstance potion) {
        if(potion.getEffect().value().isInstantenous()) {
            if(mob.level() instanceof ServerLevel level)
                potion.getEffect().value().applyInstantenousEffect(level,mob,Data.owner(mob),mob,potion.getAmplifier(),1);
        } else
            mob.addEffect(new MobEffectInstance(potion.getEffect(),-1,potion.getAmplifier(),false,false,true));
    }
    /** Final health damage, after armor and absorption, is also permanent maximum-health damage. */
    public static void wearZombie(Mob mob,float amount) {
        if(!Rules.ZOMBIES.contains(Data.kind(mob)) || amount<=0 || !Float.isFinite(amount))return;
        double previous=Data.decimal(mob,"permanent_max_hp");
        if(previous<=0)previous=mob.getMaxHealth();
        double remaining=Math.max(0,previous-amount);
        Data.set(mob,"permanent_max_hp",remaining);
        Data.set(mob,"permanent_health",(double)Math.min(mob.getHealth(),remaining));
        enforceZombieHealth(mob);
    }
    public static void enforceZombieHealth(Mob mob) {
        if(!Data.owned(mob)||!Rules.ZOMBIES.contains(Data.kind(mob)))return;
        if(!Data.tag(mob).contains("fm_permanent_health"))
            Data.set(mob,"permanent_health",mob.getHealth());
        if(!Data.tag(mob).contains("fm_permanent_max_hp"))
            Data.set(mob,"permanent_max_hp",mob.getMaxHealth());
        double maximum=Math.max(0,Data.decimal(mob,"permanent_max_hp"));
        var attribute=mob.getAttribute(Attributes.MAX_HEALTH);
        if(attribute!=null && attribute.getBaseValue()!=maximum)attribute.setBaseValue(maximum);
        float ceiling=(float)Math.min(maximum,Data.decimal(mob,"permanent_health"));
        if(mob.getHealth()>ceiling)mob.setHealth(ceiling);
        // Direct health loss (including nonstandard mod damage) can never become recoverable later.
        if(mob.getHealth()<ceiling)Data.set(mob,"permanent_health",mob.getHealth());
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
    private static void restore(Mob m,Holder<Attribute> attr,String name) {
        var value=m.getAttribute(attr);
        if(value!=null) {
            value.removeModifier(ECOLOGY_PENALTY);
            // Migrate saves produced by the earlier base-value implementation exactly once.
            if(Data.flag(m,"adjusted_"+name))value.setBaseValue(Data.decimal(m,"original_"+name));
        }
        Data.remove(m,"adjusted_"+name);
    }
    private static void updatePenalty(Mob m,boolean penalty) {
        adjust(m,Attributes.MOVEMENT_SPEED,"speed",penalty);
        adjust(m,Attributes.ATTACK_DAMAGE,"damage",penalty);
    }
    private static void adjust(Mob m,Holder<Attribute> attr,
                               String name,boolean penalty) {
        var a=m.getAttribute(attr);if(a==null)return;
        if(Data.flag(m,"adjusted_"+name)) {
            a.setBaseValue(Data.decimal(m,"original_"+name));Data.remove(m,"adjusted_"+name);
        }
        if(penalty && !a.hasModifier(ECOLOGY_PENALTY))
            a.addTransientModifier(new AttributeModifier(ECOLOGY_PENALTY,-0.2,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        else if(!penalty)a.removeModifier(ECOLOGY_PENALTY);
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
                    cube.setHealth(Math.min(cube.getHealth(),cube.getMaxHealth()));
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
            enforceZombieHealth(mob);
        }
        if(type.equals("spider")) {
            for(MobEffectInstance effect:Data.load(mob,"potions",MobEffectInstance.CODEC.listOf(),List.of()))
                if(!effect.getEffect().value().isInstantenous()&&!mob.hasEffect(effect.getEffect()))applyPermanentPotion(mob,effect);
        }
        if(owner!=null && owner.level()==world && Data.mode(mob)==Rules.Mode.FOLLOW && type.equals("silverfish"))
            owner.addEffect(new MobEffectInstance(MobEffects.HASTE,50,0,true,false));
        if(owner!=null && owner.level()==world && Data.mode(mob)==Rules.Mode.FOLLOW && type.equals("endermite") &&
           now%Tuning.INSTANCE.endermiteSpawnIntervalTicks==0 &&
           world.random.nextDouble()<Tuning.INSTANCE.endermiteSpawnChance) {
            spawnEndermanNear(world,owner);
        }
        if(type.equals("elder_guardian")&&owner!=null&&owner.level()==world&&now>=Data.time(mob,"elder_next_curse")) {
            Data.set(mob,"elder_next_curse",now+600);
            boolean cursed=false;
            for(Mob other:Relations.nearby(mob,Tuning.INSTANCE.elderCurseRadius))
                if(Relations.targetable(other,owner)) {
                    other.addEffect(new MobEffectInstance(FriendMonsters.CURSE,Tuning.INSTANCE.elderCurseDurationTicks));
                    cursed=true;
                }
            if(cursed)world.playSound(null,mob.blockPosition(),SoundEvents.ELDER_GUARDIAN_CURSE,SoundSource.HOSTILE,1,1);
        }
        if(type.equals("enderman")&&now-Data.time(mob,"last_trade")>=Tuning.INSTANCE.endermanDepartureTicks) {
            world.sendParticles(ParticleTypes.PORTAL,mob.getX(),mob.getY()+1,mob.getZ(),24,.5,.8,.5,.1);
            world.playSound(null,mob.blockPosition(),SoundEvents.ENDERMAN_TELEPORT,SoundSource.NEUTRAL,1,1);
            mob.discard();
        }
    }
    private static void spawnEndermanNear(ServerLevel world,ServerPlayer owner) {
        if(world.getEntitiesOfClass(EnderMan.class,owner.getBoundingBox().inflate(28)).size()>=8)return;
        for(int attempt=0;attempt<6;attempt++) {
            int x=owner.blockPosition().getX()+world.random.nextInt(25)-12;
            int z=owner.blockPosition().getZ()+world.random.nextInt(25)-12;
            int y=owner.blockPosition().getY()+world.random.nextInt(13)-6;
            BlockPos pos=new BlockPos(x,y,z);
            // The bonus does not load new chunks or bypass vanilla light, ground, or collision rules.
            if(!world.hasChunkAt(pos)||!SpawnPlacements.checkSpawnRules(EntityType.ENDERMAN,world,EntitySpawnReason.NATURAL,pos,world.random))continue;
            EnderMan spawn=EntityType.ENDERMAN.create(world,EntitySpawnReason.EVENT);
            if(spawn==null)return;
            spawn.snapTo(x+0.5,y,z+0.5,0,0);
            if(spawn.checkSpawnObstruction(world)&&world.addFreshEntity(spawn))return;
        }
    }
}

