package dev.friendmonsters;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
public final class Network {
    private Network(){}
    public record DragonPacket(float forward,float strafe,float rise,int action)
            implements CustomPacketPayload {
        public static final Type<DragonPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(FriendMonsters.ID,"dragon_control"));
        public static final StreamCodec<RegistryFriendlyByteBuf,DragonPacket> STREAM_CODEC =
            new StreamCodec<>() {
                public DragonPacket decode(RegistryFriendlyByteBuf buf) {
                    return new DragonPacket(buf.readFloat(),buf.readFloat(),buf.readFloat(),buf.readInt());
                }
                public void encode(RegistryFriendlyByteBuf buf,DragonPacket packet) {
                    buf.writeFloat(packet.forward());buf.writeFloat(packet.strafe());
                    buf.writeFloat(packet.rise());buf.writeInt(packet.action());
                }
            };
        public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(DragonPacket.TYPE,DragonPacket.STREAM_CODEC,
            (packet,context)->{
                if(context.player() instanceof ServerPlayer player)
                    DragonDrive.input(player,packet.forward(),packet.strafe(),
                        packet.rise(),packet.action());
            });
    }
    public static void sync(Mob mob){}
}
