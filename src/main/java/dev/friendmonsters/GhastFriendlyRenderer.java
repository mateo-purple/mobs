package dev.friendmonsters;
import net.minecraft.client.renderer.entity.GhastRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.GhastRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import java.util.Map;
import java.util.WeakHashMap;
@EventBusSubscriber(modid=FriendMonsters.ID,value=Dist.CLIENT)
public final class GhastFriendlyRenderer extends GhastRenderer {
    private final Map<GhastRenderState,Boolean> friend=new WeakHashMap<>();
    private static final Identifier HAPPY=Identifier.withDefaultNamespace(
        "textures/entity/ghast/happy_ghast.png");
    public GhastFriendlyRenderer(EntityRendererProvider.Context context){super(context);}
    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event){
        event.registerEntityRenderer(EntityType.GHAST,GhastFriendlyRenderer::new);
    }
    @Override
    public void extractRenderState(Ghast mob,GhastRenderState state,float partial){
        super.extractRenderState(mob,state,partial);
        friend.put(state,Data.flag(mob,"friendly_visual"));
    }
    @Override
    public Identifier getTextureLocation(GhastRenderState state){
        return friend.getOrDefault(state,false)?HAPPY:super.getTextureLocation(state);
    }
}
