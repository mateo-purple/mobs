package dev.friendmonsters;

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
