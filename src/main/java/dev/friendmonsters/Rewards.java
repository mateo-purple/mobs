package dev.friendmonsters;
import com.mojang.serialization.Codec;
import net.minecraft.world.entity.Mob;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import java.util.ArrayList;
import java.util.List;
public final class Rewards {
    private Rewards() {}
    public static void order(Mob enderman,ServerPlayer player) {
        Data.set(enderman,"last_trade",enderman.level().getGameTime());
        List<Integer> pending=new ArrayList<>(Data.load(enderman,"reward_tiers",Codec.INT.listOf(),List.of()));
        pending.add(Rules.rewardTier(enderman.getRandom().nextDouble()));
        Data.save(enderman,"reward_tiers",Codec.INT.listOf(),pending);
        Data.set(enderman,"return",enderman.level().getGameTime()+1);
        enderman.setTarget(null);
        enderman.getNavigation().stop();
        enderman.randomTeleport(player.getX()+12,player.getY()+1,player.getZ()+10,true);
        player.level().playSound(null,player.blockPosition(),SoundEvents.ENDERMAN_TELEPORT,SoundSource.NEUTRAL,1,1);
    }
    public static void tick(Mob mob) {
        if(!Data.kind(mob).equals("enderman") || !(mob.level() instanceof ServerLevel world))return;
        long pending=Data.time(mob,"return");
        if(pending==0 || world.getGameTime()<pending)return;
        ServerPlayer owner=Data.owner(mob);
        if(owner==null || owner.level()!=world)return;
        List<Integer> queued=new ArrayList<>(Data.load(mob,"reward_tiers",Codec.INT.listOf(),List.of()));
        // Honor an already saved order from builds predating the serialized queue.
        int tier=queued.isEmpty()?Rules.rewardTier(world.random.nextDouble()):Math.clamp(queued.getFirst(),0,4);
        mob.randomTeleport(owner.getX()+1.4,owner.getY(),owner.getZ()+1.4,true);
        world.sendParticles(ParticleTypes.PORTAL,mob.getX(),mob.getY()+1,mob.getZ(),45,.5,.8,.5,.5);
        world.playSound(null,mob.blockPosition(),SoundEvents.ENDERMAN_TELEPORT,SoundSource.NEUTRAL,1,1);
        BlockPos base=owner.blockPosition();
        BlockPos dest=null;
        for(int dy=0;dy<=2 && dest==null;dy++)
            for(int dx=-4;dx<=4 && dest==null;dx++)
                for(int dz=-4;dz<=4 && dest==null;dz++){
                    BlockPos check=base.offset(dx,dy,dz);
                    if(world.getBlockState(check).isAir() && world.getBlockState(check.below()).isCollisionShapeFullBlock(world,check.below()) &&
                       world.getEntities(null,new AABB(check)).isEmpty() && !touchesChest(world,check))
                        dest=check;
                }
        if(dest==null){
            Data.set(mob,"return",world.getGameTime()+20);
            if(!Data.flag(mob,"reward_blocked"))Interactions.tell(owner,"ブロックを置ける場所がありません。空いた場所でお渡しします");
            Data.set(mob,"reward_blocked",true);return;
        }
        Data.remove(mob,"reward_blocked");
        if(tier==0){
            List<Block> common=List.of(Blocks.STONE,Blocks.DIRT,Blocks.GRAVEL,Blocks.SAND,Blocks.OAK_LOG,
                Blocks.COBBLESTONE,Blocks.CLAY,Blocks.MOSS_BLOCK,Blocks.TUFF,
                Blocks.NETHERRACK,Blocks.DEEPSLATE,Blocks.BASALT,Blocks.MUD,Blocks.CALCITE,
                Blocks.PRISMARINE,Blocks.END_STONE,Blocks.GRANITE);
            world.setBlock(dest,common.get(world.random.nextInt(common.size())).defaultBlockState(),3);
        }else if(tier==1) {
            List<Block> ores=List.of(Blocks.COAL_ORE,Blocks.IRON_ORE,Blocks.COPPER_ORE,Blocks.GOLD_ORE,
                Blocks.LAPIS_ORE,Blocks.REDSTONE_ORE,Blocks.DIAMOND_ORE,Blocks.EMERALD_ORE,Blocks.ANCIENT_DEBRIS);
            world.setBlock(dest,ores.get(world.random.nextInt(ores.size())).defaultBlockState(),3);
        }else if(tier==3) {
            List<Block> wealth=List.of(Blocks.DIAMOND_BLOCK,Blocks.EMERALD_BLOCK,Blocks.GOLD_BLOCK,
                Blocks.IRON_BLOCK,Blocks.NETHERITE_BLOCK);
            world.setBlock(dest,wealth.get(world.random.nextInt(wealth.size())).defaultBlockState(),3);
        }else {
            world.setBlock(dest,Blocks.CHEST.defaultBlockState(),3);
            if(world.getBlockEntity(dest) instanceof ChestBlockEntity chest){
                var table=tier==4 ? BuiltInLootTables.END_CITY_TREASURE :
                    switch(world.random.nextInt(4)) {
                        case 0 -> BuiltInLootTables.ABANDONED_MINESHAFT;
                        case 1 -> BuiltInLootTables.SIMPLE_DUNGEON;
                        case 2 -> BuiltInLootTables.DESERT_PYRAMID;
                        default -> BuiltInLootTables.PILLAGER_OUTPOST;
                    };
                chest.setLootTable(table);chest.setLootTableSeed(world.random.nextLong());
            }
        }
        if(!queued.isEmpty())queued.removeFirst();
        Data.save(mob,"reward_tiers",Codec.INT.listOf(),queued);
        if(queued.isEmpty())Data.remove(mob,"return");
        else Data.set(mob,"return",world.getGameTime()+1);
        Interactions.tell(owner,"エンダーマンの拾得品：ランク"+(tier+1));
    }
    private static boolean touchesChest(ServerLevel world,BlockPos pos){
        for(Direction side:Direction.Plane.HORIZONTAL){
            var state=world.getBlockState(pos.relative(side));
            if(state.is(Blocks.CHEST) || state.is(Blocks.TRAPPED_CHEST))return true;
        }
        return false;
    }
}

