package dev.friendmonsters;

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
