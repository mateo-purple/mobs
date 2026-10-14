from pathlib import Path
root=Path(__file__).parent
files={
'src/main/java/dev/friendmonsters/Rules.java':r'''package dev.friendmonsters;

import java.util.Set;

/** Gameplay rules without Minecraft dependencies; also exercised by unit tests. */
public final class Rules {
    private Rules() {}
    public enum Mode { FOLLOW, STAY, PATROL, DISMISSED }
    public static final Set<String> ZOMBIES = Set.of("zombie", "husk", "zombie_villager", "drowned", "zombified_piglin");
    public static final Set<String> SKELETONS = Set.of("skeleton", "stray", "bogged", "parched", "wither_skeleton");
    public static final Set<String> ILLAGERS = Set.of("pillager", "vindicator", "evoker", "witch");
    public static final Set<String> FLYING = Set.of("blaze", "ghast", "phantom", "wither", "ender_dragon");
    public static final Set<String> AQUATIC = Set.of("guardian", "elder_guardian", "drowned");
    public static final Set<String> NON_COMBAT = Set.of("enderman", "hoglin", "piglin_brute");
    public static final Set<String> RAW_MEAT = Set.of("beef", "porkchop", "chicken", "mutton", "rabbit");
    public static boolean isCommand(boolean emptyHand, boolean sneaking, boolean specialInteraction) {
        return emptyHand && (!specialInteraction || sneaking);
    }
    public static Mode nextMode(Mode old, boolean pillager) {
        if (pillager) return old == Mode.FOLLOW ? Mode.DISMISSED : Mode.FOLLOW;
        return switch (old) { case FOLLOW -> Mode.STAY; case STAY -> Mode.PATROL; default -> Mode.FOLLOW; };
    }
    public static double wardenChance(double hp) {
        if (!Double.isFinite(hp) || hp <= 0 || hp > 50) return 0;
        return Math.min(0.06, 0.06 * (51.0 - hp) / 50.0);
    }
    public static boolean witherEligible(double hp) { return hp > 0 && hp <= 10; }
    public static double dragonChance(boolean firstKillCompleted, boolean egg, boolean crystal) {
        return firstKillCompleted ? (egg ? 1.0 : crystal ? 0.001 : 0.0) : 0.0;
    }
    public static boolean weakened(String species, long time, String dimension, boolean desert, boolean inWater) {
        return switch (species) {
            case "zombie", "zombie_villager" -> Math.floorMod(time, 24000L) < 12000L;
            case "husk" -> !desert;
            case "zombified_piglin" -> !dimension.equals("the_nether");
            case "drowned" -> !inWater;
            default -> false;
        };
    }
    public static int smallerMagmaSize(int size) { return size > 2 ? 2 : 1; }
    public static int biggerMagmaSize(int size) { return size < 2 ? 2 : 4; }
    public static long merchantDay(long worldTime) { return Math.floorDiv(worldTime, 24000L); }
    public static int cooldown(int ordinary, boolean cursed) { return cursed ? (int)Math.ceil(ordinary / 0.4) : ordinary; }
    public static int rewardTier(double roll) {
        if (roll < 0.70) return 0;
        if (roll < 0.90) return 1;
        if (roll < 0.98) return 2;
        if (roll < 0.998) return 3;
        return 4;
    }
}
''',
'src/main/java/dev/friendmonsters/Tuning.java':r'''package dev.friendmonsters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.neoforged.fml.loading.FMLPaths;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** Only previously unspecified values are provisional. No configuration screen is added. */
public final class Tuning {
    public static Tuning INSTANCE = new Tuning();
    public Map<String, Double> recruitmentChance = new LinkedHashMap<>();
    public double patrolRadius = 16, groupRadius = 24, followTeleportDistance = 40;
    public double witherReturnDistance = 40, elderCurseRadius = 24;
    public int elderCurseDurationTicks = 700, magmaDecayTicks = 600;
    public int endermanDepartureTicks = 1200, endermiteSpawnIntervalTicks = 200;
    public double endermiteSpawnChance = 0.15, dragonFlightSpeed = 0.9;
    public float creeperThrownTntPower = 2.5f;
    public float caveSpiderStartingHp = 8;
    public Tuning() {
        put(0.20, "zombie", "husk", "zombie_villager", "drowned", "zombified_piglin");
        put(0.02, "skeleton", "stray", "bogged", "parched");
        put(0.05, "creeper", "spider", "cave_spider", "pillager", "piglin", "elder_guardian", "silverfish");
        put(0.10, "slime", "magma_cube", "ghast", "enderman", "endermite");
        put(0.20, "wither_skeleton"); put(0.40, "blaze");
        put(0.01, "shulker"); put(0.001, "wither");
        put(0.60, "hoglin"); put(1.0, "phantom");
    }
    private void put(double chance, String... species) { for (String s : species) recruitmentChance.put(s, chance); }
    public double chance(String species) {
        double x = recruitmentChance.getOrDefault(species, 0.0);
        return Double.isFinite(x) ? Math.clamp(x, 0, 1) : 0;
    }
    public static void load() {
        Path file = FMLPaths.CONFIGDIR.get().resolve("friendmonsters.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            if (Files.exists(file)) {
                Tuning loaded = gson.fromJson(Files.readString(file, StandardCharsets.UTF_8), Tuning.class);
                if (loaded != null && loaded.recruitmentChance != null) INSTANCE = loaded;
            } else {
                Files.createDirectories(file.getParent());
                Files.writeString(file, gson.toJson(INSTANCE), StandardCharsets.UTF_8);
            }
            INSTANCE.magmaDecayTicks = Math.max(20, INSTANCE.magmaDecayTicks);
            INSTANCE.endermiteSpawnIntervalTicks = Math.max(20, INSTANCE.endermiteSpawnIntervalTicks);
            INSTANCE.patrolRadius = Math.clamp(INSTANCE.patrolRadius, 4, 64);
            INSTANCE.groupRadius = Math.clamp(INSTANCE.groupRadius, 4, 64);
            INSTANCE.dragonFlightSpeed = Math.clamp(INSTANCE.dragonFlightSpeed, 0.1, 2.0);
        } catch (Exception ex) {
            FriendMonsters.LOGGER.error("Could not read friendmonsters.json; using safe defaults", ex);
        }
    }
}
''',
'src/main/java/dev/friendmonsters/Data.java':r'''package dev.friendmonsters;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

/** Persistent data is stored by NeoForge with each original vanilla entity. */
public final class Data {
    private Data() {}
    private static String key(String name) { return "fm_" + name; }
    public static CompoundTag tag(Entity e) { return e.getPersistentData(); }
    public static String text(Entity e, String name) { return tag(e).getString(key(name)).orElse(""); }
    public static int number(Entity e, String name) { return tag(e).getInt(key(name)).orElse(0); }
    public static long time(Entity e, String name) { return tag(e).getLong(key(name)).orElse(0L); }
    public static double decimal(Entity e, String name) { return tag(e).getDouble(key(name)).orElse(0.0); }
    public static boolean flag(Entity e, String name) { return tag(e).getBoolean(key(name)).orElse(false); }
    public static void set(Entity e, String k, String v) { tag(e).putString(key(k), v); }
    public static void set(Entity e, String k, int v) { tag(e).putInt(key(k), v); }
    public static void set(Entity e, String k, long v) { tag(e).putLong(key(k), v); }
    public static void set(Entity e, String k, double v) { tag(e).putDouble(key(k), v); }
    public static void set(Entity e, String k, boolean v) { tag(e).putBoolean(key(k), v); }
    public static void remove(Entity e, String k) { tag(e).remove(key(k)); }
    public static String kind(Entity e) { return BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getPath(); }
    public static boolean owned(Entity e) { return !text(e, "owner").isEmpty(); }
    public static UUID ownerId(Entity e) {
        try { return UUID.fromString(text(e, "owner")); } catch (IllegalArgumentException ex) { return null; }
    }
    public static ServerPlayer owner(Entity e) {
        if (!(e.level() instanceof ServerLevel level)) return null;
        UUID id = ownerId(e);
        return id == null ? null : level.getServer().getPlayerList().getPlayer(id);
    }
    public static Rules.Mode mode(Entity e) {
        int n = number(e, "mode");
        return n >= 0 && n < Rules.Mode.values().length ? Rules.Mode.values()[n] : Rules.Mode.FOLLOW;
    }
    public static void mode(Entity e, Rules.Mode mode) { set(e, "mode", mode.ordinal()); }
    public static boolean controlled(Entity e) {
        return owned(e) && !Rules.NON_COMBAT.contains(kind(e)) && mode(e) != Rules.Mode.DISMISSED;
    }
    public static <T> void save(Entity e, String name, Codec<T> codec, T value) {
        var ops = e.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        codec.encodeStart(ops, value).resultOrPartial(FriendMonsters.LOGGER::error)
            .ifPresent(t -> tag(e).put(key(name), t));
    }
    public static <T> T load(Entity e, String name, Codec<T> codec, T fallback) {
        Tag value = tag(e).get(key(name));
        if (value == null) return fallback;
        return codec.parse(e.registryAccess().createSerializationContext(NbtOps.INSTANCE), value)
            .resultOrPartial(FriendMonsters.LOGGER::error).orElse(fallback);
    }
    public static List<ItemStack> items(Entity e, String name) {
        return new ArrayList<>(load(e, name, ItemStack.CODEC.listOf(), List.of()));
    }
    public static void items(Entity e, String name, List<ItemStack> items) {
        save(e, name, ItemStack.CODEC.listOf(), items.stream().filter(s -> !s.isEmpty()).toList());
    }
    public static void anchor(Entity e) {
        set(e,"anchor_x",e.getX()); set(e,"anchor_y",e.getY()); set(e,"anchor_z",e.getZ());
    }
}
''',
'src/main/java/dev/friendmonsters/FriendMonsters.java':r'''package dev.friendmonsters;

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
        Tuning.load();
        NeoForge.EVENT_BUS.register(Events.class);
        LOGGER.info("FRIEND MONSTERS 0.1.0-alpha.1 for Minecraft 1.21.11 / NeoForge");
    }
}
''',
'src/test/java/dev/friendmonsters/RulesTest.java':r'''package dev.friendmonsters;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RulesTest {
    @Test void commandRouting() {
        assertTrue(Rules.isCommand(true, false, false));
        assertTrue(Rules.isCommand(true, true, false));
        assertFalse(Rules.isCommand(false, true, false));
        assertFalse(Rules.isCommand(true, false, true));
        assertTrue(Rules.isCommand(true, true, true));
    }
    @Test void modeCycles() {
        assertEquals(Rules.Mode.STAY, Rules.nextMode(Rules.Mode.FOLLOW, false));
        assertEquals(Rules.Mode.PATROL, Rules.nextMode(Rules.Mode.STAY, false));
        assertEquals(Rules.Mode.FOLLOW, Rules.nextMode(Rules.Mode.PATROL, false));
        assertEquals(Rules.Mode.DISMISSED, Rules.nextMode(Rules.Mode.FOLLOW, true));
        assertEquals(Rules.Mode.FOLLOW, Rules.nextMode(Rules.Mode.DISMISSED, true));
    }
    @Test void wardenBoundaries() {
        assertEquals(0, Rules.wardenChance(50.001)); assertEquals(0, Rules.wardenChance(0));
        assertEquals(0.0012, Rules.wardenChance(50), 1e-10);
        assertEquals(0.06, Rules.wardenChance(1), 1e-10);
        assertTrue(Rules.wardenChance(20) > Rules.wardenChance(40));
    }
    @Test void witherBoundaries() {
        assertFalse(Rules.witherEligible(10.001)); assertTrue(Rules.witherEligible(10));
        assertFalse(Rules.witherEligible(0));
    }
    @Test void firstDragonCannotBeRecruited() {
        assertEquals(0, Rules.dragonChance(false, true, false));
        assertEquals(0, Rules.dragonChance(false, false, true));
        assertEquals(1, Rules.dragonChance(true, true, false));
        assertEquals(0.001, Rules.dragonChance(true, false, true));
    }
    @Test void environmentPenaltiesAreNotLightLevelChecks() {
        assertTrue(Rules.weakened("zombie", 1000, "overworld", false, false));
        assertFalse(Rules.weakened("zombie", 14000, "overworld", false, false));
        assertFalse(Rules.weakened("husk", 1000, "overworld", true, false));
        assertTrue(Rules.weakened("husk", 14000, "overworld", false, false));
        assertFalse(Rules.weakened("zombified_piglin", 1000, "the_nether", false, false));
        assertTrue(Rules.weakened("zombified_piglin", 14000, "the_end", false, false));
        assertTrue(Rules.weakened("drowned", 14000, "overworld", false, false));
    }
    @Test void magmaUsesTheThreeNaturalSizes() {
        assertEquals(2, Rules.biggerMagmaSize(1)); assertEquals(4, Rules.biggerMagmaSize(2));
        assertEquals(4, Rules.biggerMagmaSize(4)); assertEquals(2, Rules.smallerMagmaSize(4));
        assertEquals(1, Rules.smallerMagmaSize(2)); assertEquals(1, Rules.smallerMagmaSize(1));
    }
    @Test void curseMeansFortyPercentAttackFrequency() {
        assertEquals(50, Rules.cooldown(20, true)); assertEquals(20, Rules.cooldown(20, false));
    }
    @Test void tradeDayUsesMinecraftTicks() {
        assertEquals(0, Rules.merchantDay(23999)); assertEquals(1, Rules.merchantDay(24000));
        assertEquals(3, Rules.merchantDay(72000));
    }
    @Test void endermanTierEdges() {
        assertEquals(0, Rules.rewardTier(0)); assertEquals(1, Rules.rewardTier(.70));
        assertEquals(2, Rules.rewardTier(.90)); assertEquals(3, Rules.rewardTier(.98));
        assertEquals(4, Rules.rewardTier(.998));
    }
}
'''
}
for name,text in files.items():
    p=root/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(text,encoding='utf-8')
print('Wrote',len(files),'core files')
