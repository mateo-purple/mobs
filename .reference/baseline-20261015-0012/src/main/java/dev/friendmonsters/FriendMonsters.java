package dev.friendmonsters;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

@Mod(FriendMonsters.ID)
public final class FriendMonsters {
    public static final String ID = "friendmonsters";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, ID);
    public static final DeferredHolder<MobEffect, MobEffect> CURSE = EFFECTS.register("elder_curse", () -> new Effect(MobEffectCategory.HARMFUL, 0x596e73));
    public static final DeferredHolder<MobEffect, MobEffect> HEAT = EFFECTS.register("magma_heat", () -> new Effect(MobEffectCategory.BENEFICIAL, 0xef6925));
    private static final class Effect extends MobEffect {
        Effect(MobEffectCategory category, int color) { super(category, color); }
    }
    public FriendMonsters(IEventBus modBus) {
        EFFECTS.register(modBus);
        modBus.addListener(Network::register);
        if(net.neoforged.fml.loading.FMLEnvironment.getDist()==net.neoforged.api.distmarker.Dist.CLIENT)
            modBus.addListener(GhastFriendlyRenderer::register);
        Tuning.load();
        NeoForge.EVENT_BUS.register(Events.class);
        LOGGER.info("FRIEND MONSTERS 0.1.0-alpha.1 for Minecraft 1.21.11 / NeoForge");
    }
}
