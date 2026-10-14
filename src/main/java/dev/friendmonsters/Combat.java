package dev.friendmonsters;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
public final class Combat {
    private Combat(){}
    private static final Map<Mob,Boolean> CURSE_PAUSES=new WeakHashMap<>();
    private static final java.util.Set<LivingEntity> APPLYING_BITE=java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
    /** Tick native attack goals and Brain attack cooldowns on exactly two of each five ticks. */
    public static void curseTick(Mob mob){
        endCurseTick(mob);
        if(!mob.hasEffect(FriendMonsters.CURSE)||mob.isNoAi())return;
        boolean fighting=mob.getTarget()!=null||mob.getBrain().hasMemoryValue(
            net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET);
        if(fighting&&Math.floorMod(mob.level().getGameTime()+mob.getId(),5)>=2){
            CURSE_PAUSES.put(mob,false);
            // Navigation keeps progressing; the curse slows attacks rather than erasing their target.
            mob.getNavigation().tick();mob.getMoveControl().tick();mob.getLookControl().tick();
            mob.setNoAi(true);
        }
    }
    public static boolean isCursePaused(Mob mob){return CURSE_PAUSES.containsKey(mob);}
    public static void endCurseTick(Mob mob){
        Boolean before=CURSE_PAUSES.remove(mob);if(before!=null)mob.setNoAi(before);
    }
    public static Entity attacker(Entity source) {
        return source instanceof Projectile projectile && projectile.getOwner()!=null
            ? projectile.getOwner():source;
    }
    public static boolean friendly(Entity source,LivingEntity victim) {
        Entity e=attacker(source);
        return e instanceof LivingEntity living && Relations.protects(living,victim);
    }
    public static void projectileJoin(Entity entity){
        if(!(entity instanceof Projectile projectile))return;
        if(!(projectile.getOwner() instanceof Mob mob)||!Data.owned(mob))return;
        if(entity instanceof Arrow arrow && Rules.SKELETONS.contains(Data.kind(mob))){
            List<ItemStack> stock=Data.items(mob,"arrows");
            if(!stock.isEmpty() && !stock.getFirst().isEmpty()){
                ItemStack first=stock.getFirst();
                if(first.has(DataComponents.POTION_CONTENTS)){
                    var contents=first.get(DataComponents.POTION_CONTENTS);
                    arrow.getPickupItemStackOrigin().set(DataComponents.POTION_CONTENTS,
                        net.minecraft.world.item.alchemy.PotionContents.EMPTY);
                    for(MobEffectInstance effect:contents.getAllEffects())arrow.addEffect(new MobEffectInstance(effect));
                    arrow.getPickupItemStackOrigin().set(DataComponents.POTION_DURATION_SCALE,
                        first.getOrDefault(DataComponents.POTION_DURATION_SCALE,1.0f));
                    first.shrink(1);
                    Data.items(mob,"arrows",stock);
                }
            }
        }
        if(entity instanceof LargeFireball && Data.kind(mob).equals("ghast")
          && Data.number(mob,"charges")>0){
            Data.set(mob,"charges",Data.number(mob,"charges")-1);
            Data.set(entity,"boost",true);
        }
    }
    public static void slimeCapture(LivingEntity source,LivingEntity victim){
        if(!(source instanceof Slime slime)||!Data.owned(slime)||!Data.kind(slime).equals("slime"))return;
        if(!(victim instanceof Mob target) || Data.owned(target) ||
           Data.kind(target).equals("warden") || Data.kind(target).equals("wither") ||
           Data.kind(target).equals("ender_dragon"))return;
        if(!Data.text(slime,"prey").isEmpty() || !Data.text(target,"captor").isEmpty())return;
        if(!target.startRiding(slime))return;
        Data.set(slime,"prey",target.getStringUUID());
        Data.set(target,"captor",slime.getStringUUID());
        Data.set(target,"was_no_ai",target.isNoAi());
        Data.set(target,"was_invisible",target.isInvisible());
        target.setNoAi(true);target.setInvisible(true);
        slime.setTarget(null);Data.set(slime,"last_digest",slime.level().getGameTime());
    }
    /** Vanilla slimes only contact-damage players and golems, so Mob support needs this path. */
    public static void slimeAttack(Slime slime){
        if(!(slime.level() instanceof ServerLevel level)||!Data.owned(slime)||
            Data.mode(slime)==Rules.Mode.STAY||Data.mode(slime)==Rules.Mode.DISMISSED||
            !Data.text(slime,"prey").isEmpty())return;
        LivingEntity target=CompanionAI.combatTarget(slime);
        if(target==null||Relations.protects(slime,target)||!slime.hasLineOfSight(target)||
            !slime.isWithinMeleeAttackRange(target)||level.getGameTime()<Data.time(slime,"next_melee"))return;
        Data.set(slime,"next_melee",level.getGameTime()+Rules.cooldown(20,slime.hasEffect(FriendMonsters.CURSE)));
        if(Data.kind(slime).equals("slime")){
            slimeCapture(slime,target);
            if(!Data.text(slime,"prey").isEmpty())return;
        }
        slime.doHurtTarget(level,target);
    }
    public static void digestion(Slime slime){
        if(!(slime.level() instanceof ServerLevel level)||!Data.kind(slime).equals("slime"))return;
        Mob prey=null;
        for(Entity e:slime.getPassengers())if(e instanceof Mob m&&m.getStringUUID().equals(Data.text(slime,"prey")))prey=m;
        if(prey==null){Data.remove(slime,"prey");return;}
        if(level.getGameTime()-Data.time(slime,"last_digest")>=15){
            Data.set(slime,"last_digest",level.getGameTime());
            prey.hurtServer(level,slime.damageSources().mobAttack(slime),1);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.ITEM_SLIME,
                slime.getX(),slime.getY()+slime.getBbHeight()/2,slime.getZ(),4,.15,.15,.15,.03);
        }
    }
    public static void releasePrey(Slime slime){
        for(Entity e:List.copyOf(slime.getPassengers()))if(e instanceof Mob prey&&
            slime.getStringUUID().equals(Data.text(prey,"captor"))){
            prey.stopRiding();prey.setNoAi(Data.flag(prey,"was_no_ai"));
            prey.setInvisible(Data.flag(prey,"was_invisible"));
            Data.remove(prey,"captor");
        }
        Data.remove(slime,"prey");
    }
    public static void onAttack(LivingEntity attacker,LivingEntity victim){
        if(!Data.owned(attacker))return;
        String kind=Data.kind(attacker);
        if(kind.equals("cave_spider")&&APPLYING_BITE.add(attacker)){
            try{
                for(MobEffectInstance effect:Data.load(attacker,"potions",MobEffectInstance.CODEC.listOf(),List.of())){
                    if(effect.getEffect().value().isInstantenous()&&victim.level() instanceof ServerLevel level)
                        effect.getEffect().value().applyInstantenousEffect(level,attacker,attacker,victim,effect.getAmplifier(),1);
                    else victim.addEffect(new MobEffectInstance(effect),attacker);
                }
            }finally{APPLYING_BITE.remove(attacker);}
        }
        if(kind.equals("magma_cube")&&attacker.hasEffect(FriendMonsters.HEAT))
            victim.igniteForSeconds(6);
        if(kind.equals("slime"))slimeCapture(attacker,victim);
    }
    public static void creeperShoot(Creeper mob){
        if(!(mob.level() instanceof ServerLevel level)||Data.number(mob,"tnt")<=0||
            Data.mode(mob)==Rules.Mode.STAY||Data.mode(mob)==Rules.Mode.DISMISSED)return;
        if(level.getGameTime()-Data.time(mob,"last_tnt")<Rules.cooldown(35,mob.hasEffect(FriendMonsters.CURSE)))return;
        LivingEntity target=CompanionAI.combatTarget(mob);
        if(target==null||mob.distanceToSqr(target)>30*30||!mob.hasLineOfSight(target))return;
        Vec3 from=mob.getEyePosition(), diff=target.getEyePosition().subtract(from);
        Snowball ball=new Snowball(level,mob,new ItemStack(Items.TNT));
        ball.setPos(from);
        ball.shoot(diff.x,diff.y+diff.horizontalDistance()*0.13,diff.z,2.2f,1.2f);
        Data.set(ball,"tnt_projectile",true);
        level.addFreshEntity(ball);
        Data.set(mob,"tnt",Data.number(mob,"tnt")-1);
        Data.set(mob,"last_tnt",level.getGameTime());
        mob.setSwellDir(-1);mob.setTarget(null);
    }
    public static void blast(Projectile ball){
        if(!Data.flag(ball,"tnt_projectile"))return;
        if(ball.level() instanceof ServerLevel level){
            level.explode(ball,ball.getX(),ball.getY(),ball.getZ(),
                Tuning.INSTANCE.creeperThrownTntPower,Level.ExplosionInteraction.MOB);
            ball.discard();
        }
    }
    public static void boostedBlast(Projectile projectile){
        if(!Data.flag(projectile,"boost"))return;
        Data.remove(projectile,"boost");
        if(projectile.level() instanceof ServerLevel level){
            level.explode(projectile,projectile.getX(),projectile.getY(),projectile.getZ(),
                1.8f,Level.ExplosionInteraction.NONE);
        }
    }
}
