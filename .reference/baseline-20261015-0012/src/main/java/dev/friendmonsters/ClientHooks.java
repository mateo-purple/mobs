package dev.friendmonsters;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
@EventBusSubscriber(modid=FriendMonsters.ID,value=Dist.CLIENT)
public final class ClientHooks {
    private ClientHooks(){}
    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event){
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null||mc.screen!=null||
           !(mc.player.getVehicle() instanceof EnderDragon))return;
        float f=(mc.options.keyUp.isDown()?1:0)-(mc.options.keyDown.isDown()?1:0);
        float s=(mc.options.keyRight.isDown()?1:0)-(mc.options.keyLeft.isDown()?1:0);
        float y=(mc.options.keyJump.isDown()?1:0)-(mc.options.keySprint.isDown()?1:0);
        ClientPacketDistributor.sendToServer(new Network.DragonPacket(f,s,y,0));
    }
    @SubscribeEvent
    public static void mouse(InputEvent.MouseButton.Pre event){
        if(event.getAction()!=1)return;
        Minecraft mc=Minecraft.getInstance();
        if(mc.screen!=null||mc.player==null||
           !(mc.player.getVehicle() instanceof EnderDragon))return;
        int attack=event.getButton()==0?1:event.getButton()==1?2:0;
        if(attack==0)return;
        ClientPacketDistributor.sendToServer(new Network.DragonPacket(0,0,0,attack));
        event.setCanceled(true);
    }
}
