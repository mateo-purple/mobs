package dev.friendmonsters;

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
