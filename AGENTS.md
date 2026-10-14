# FRIEND MONSTERS — instructions for Codex

Read `CODEX_HANDOFF.md` before editing or building. These are the user's requirements.

- Finish the Minecraft **Java 1.21.11 / NeoForge 21.11.45 / Java 21** mod in this workspace. No Replit.
- The user explicitly wants actual implementation and a usable `.jar`, not merely plans or more questions. Prioritize implementing code over exhaustive testing.
- Honor vanilla UI only: NO new management GUI; use existing vanilla merchant/trade screens.
- Regular companions: empty-hand right-click cycles **FOLLOW -> STAY (truly stationary) -> PATROL (wander + auto fight)**.
- Where a vanilla right-click action is needed (trade, riding, other special actions), use **sneak + empty-hand right-click for command**, normal right-click for the special action.
- Treat "noncombat" as exceptional. **All other allied hostile mobs actively support their owner in combat.** Avoid friendly fire except the intentionally indiscriminate Warden.
- Protect pre-existing and concurrent changes. First inspect all current files, build logs and recent timestamps. Do not wipe edits, overwrite others' active work, discard diffs, force push, or modify `C:\Users\woody\FRIEND_MONSTERS` (the older Fabric project).
- The current workspace may be actively changing. Check the latest file state and reconcile instead of applying stale snapshots.
- Avoid needless test cycles: compile, fix blocking errors, run focused smoke test if feasible. Do not report success from `createMinecraftArtifacts` alone.
- Output the installable JAR to `build/libs` and a simple Japanese install/readme. Never claim complete unless mod compilation succeeds.
- If blocked, preserve partial work and leave exact actionable blocker/log.
