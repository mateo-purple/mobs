package dev.friendmonsters;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;

/** Vanilla still updates dragon parts; only the boss's autonomous flight is replaced. */
public final class CompanionDragonPhase extends AbstractDragonPhaseInstance {
    public static final EnderDragonPhase<CompanionDragonPhase> TYPE =
        EnderDragonPhase.create(CompanionDragonPhase.class, "FriendMonsters");

    public CompanionDragonPhase(EnderDragon dragon) { super(dragon); }
    @Override public EnderDragonPhase<CompanionDragonPhase> getPhase() { return TYPE; }
    @Override public void doServerTick(ServerLevel level) { DragonDrive.advance(dragon); }
}
