# BEDROCK STRATA Field Guide (companion to ENEND)

This is an entirely static, bilingual Japanese/English guide in the public GitHub Pages repository `mateo-purple/mobs`, intended at:

`https://mateo-purple.github.io/mobs/bedrock-strata/`

The ENEND Mobdex remains at `/mobs/`. The pages link to each other; both use `enend.lang` in localStorage for persistent language selection. The page has no trackers or remote JS dependencies.

## Canon and sources

Editorial opening prose from the user-provided bilingual BEDROCK STRATA manuscript, checked against the privately maintained VOID ARCHIVE alpha.12 build commit `ce7b3c3` (2026-10-25). The English manuscript's obsolete anvil repair sentence was corrected to crafting grid to match the latest Japanese manuscript and actual in-game `glowing_obsidian_full_repair` recipe.

All 17 copied pixel textures are real mod assets found inside the user's own local VOID ARCHIVE working directory at site creation time, not generative-AI imagery. The `human` / `giant` / `killer_bunny` species without trustworthy art use textual silhouettes instead. Existing ENEND mob renders are reused by relative URL, with species-specific notes clarifying that classic textures may vary.

The mod's internal identifier is `voidarchive`. BEDROCK STRATA is the field-guide heading supplied by the user; do not mistake it for a changed runtime mod ID. Avoid asserting that the fictional 'world between Nether and Overworld' is a real separate Minecraft dimension. No private mod source code or binaries are included in the published site.

## Site structure

- `index.html`: semantic sections and authored bilingual manuscript structure
- `styles.css`: own warm-copper / violet archive theme, responsive
- `content.js`: keyed Japanese/English strings, full lists for biomes, mobs, relics
- `app.js`: language switch, region filters, Mob search/filter, disclosures, mobile nav
- `assets/`: mod texture specimens and local SVG favicon

Minecraft 26.3, NeoForge 26.3.0.83, Java 25. This guide describes alpha.12 candidate gameplay. Gameplay validation is separate; do not present as an official Mojang/Microsoft site.
