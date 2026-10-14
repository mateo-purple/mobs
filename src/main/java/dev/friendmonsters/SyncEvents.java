package dev.friendmonsters;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid=FriendMonsters.ID)
public final class SyncEvents {
    private SyncEvents() {}
    @SubscribeEvent
    public static void tracking(PlayerEvent.StartTracking event){
        if(event.getEntity() instanceof ServerPlayer player && event.getTarget() instanceof Mob mob)
            PacketDistributor.sendToPlayer(player,new Network.AllyPacket(mob.getId(),Data.owned(mob)));
    }
}
