# Project Rules &amp; AI/IDE Instructions

## 1. Project Identity

| Field | Value |
|-------|-------|
| Project name | ReCraft Collect — Zombie Purge |
| Mod ID | `recraftcollect` |
| Package | `com.vyrriox.recraftcollect` |
| Tech stack | Java 17, Minecraft Forge 1.20.1-47.2.0, Gradle 8.7 (ForgeGradle 6.0.16+) |
| Author | vyrriox |
| License | All Rights Reserved |
| Dependencies | Minecraft 1.20.1, Forge 47+, GSON (bundled with MC) |
| Side | Server-only (no client mod required) |

## 2. Git Workflow

- **Default branch**: `main`. Direct commits allowed (no protected-branch policy).
- **Commit convention**: imperative tense, English. No AI attribution. Format: `<type>: <message>` where type ∈ {feat, fix, refactor, docs, perf, chore}. Example: `feat: add modid wildcard support to ZombieValueConfig`.
- **Release process**:
  1. Bump version in `build.gradle` only when explicitly asked.
  2. Update `CHANGELOG.md` (English first, then `## Modifications` block in French).
  3. Generate `TEST_PROCEDURE_vX.Y.Z.html` if version bumped.
  4. Commit, push, tag `vX.Y.Z` if requested.
- **Never** push to `main` without running `./gradlew build` first.

## 3. Code Conventions

- **Code language**: English (identifiers, comments, log messages, JSON keys).
- **In-game text language**: French (commands, chat messages, boss bar, leaderboard).
- **Naming**:
  - Classes: `PascalCase` (`ZombieScoreManager`, `ZombieKillCommand`).
  - Methods/fields: `camelCase` (`getPlayerScore`, `bossBarRadius`).
  - Constants: `UPPER_SNAKE_CASE` (`MODID`, `DEFAULT_GOAL`, `OBJECTIVE_NAME`).
  - Config files: `recraftcollect-<topic>.json` (kebab-case prefix).
  - Entity IDs in config: `modid:entity_name` (lowercase, snake_case after colon).
- **Architecture**:
  - `command/` — Brigadier command builders.
  - `config/` — File-backed configuration (JSON). Hot-reloadable.
  - `data/` — `SavedData` subclasses (world-persistent state) and pure resolvers.
  - `leaderboard/` — World-rendering helpers (armor stand spawners).
  - Root `ReCraftCollect` — `@Mod` entry point, event subscribers, boss bar lifecycle.
- **Don't**:
  - Don't add a CommonProxy / ClientProxy split — server-only.
  - Don't introduce new dependencies without an explicit ask.
  - Don't write client-side rendering code (no `@OnlyIn(Dist.CLIENT)`).
  - Don't bundle French strings outside chat / boss bar / leaderboard text.
  - Don't increment version unless the user explicitly asks.

## 4. Project Structure

```
mod-recraft-collect/
├── build.gradle                      Forge build config, version source of truth
├── settings.gradle                   Gradle plugin management
├── gradle.properties                 JVM settings
├── gradlew / gradlew.bat             Gradle wrapper
├── README.md                         Bilingual user-facing docs
├── CHANGELOG.md                      Bilingual version history
├── RULES.md                          This file
├── TEST_PROCEDURE_v2.0.0.html        Manual QA checklist
├── curseforge_page.md                Public listing copy
└── src/main/
    ├── resources/
    │   ├── pack.mcmeta               Resource pack metadata
    │   └── META-INF/mods.toml        Forge mod manifest
    └── java/com/vyrriox/recraftcollect/
        ├── ReCraftCollect.java       @Mod entry, LivingDeathEvent, boss bar
        ├── command/
        │   └── ZombieKillCommand.java     /zk command tree
        ├── config/
        │   ├── MilestoneConfig.java       Milestones JSON I/O
        │   └── ZombieValueConfig.java     Entity-id → points JSON I/O
        ├── data/
        │   ├── ZombieScoreManager.java    SavedData: scores, goal, milestones-reached, boss bar zone, leaderboard pos
        │   └── ZombieKillResolver.java    Pure entity → point lookup
        └── leaderboard/
            └── LeaderboardDisplay.java    Armor stand spawner / cleaner
```

## 5. Adding a New Feature (Step by Step)

1. Create a feature branch: `git checkout -b feat/<short-name>` (or work on `main` for trivial changes).
2. If the feature is config-driven, decide whether it lives in `MilestoneConfig`, `ZombieValueConfig`, or a new `XxxConfig` class. Always provide `init(Path)`, `load()`, `save()`, and a `initDefaults()` if applicable.
3. If the feature affects per-world state, add fields to `ZombieScoreManager` with matching `load()` / `save()` NBT keys. Bump no NBT version — handle missing keys with `tag.contains(...)` checks.
4. If the feature exposes commands, add new `Commands.literal(...)` branches in `ZombieKillCommand.register()`. Player commands are unrestricted; admin commands require `.requires(src -> src.hasPermission(2))` (OP 3 for destructive ops).
5. If the feature listens to game events, add an `@SubscribeEvent` method in `ReCraftCollect` (the only event-bus handler class).
6. Run `./gradlew build` — must produce `BUILD SUCCESSFUL`.
7. Update `README.md` (both EN and FR command tables).
8. Add an entry to `CHANGELOG.md` under the current `## [Unreleased]` or current version section, English first then French mirror.
9. Add manual test cases to `TEST_PROCEDURE_vX.Y.Z.html`.
10. Commit, push.

## 6. Testing Checklist

Before any commit/push:

- [ ] `./gradlew build` returns `BUILD SUCCESSFUL`, no warnings introduced
- [ ] No new top-level deps in `build.gradle`
- [ ] `mods.toml` version still references `${file.jarVersion}` (don't hardcode)
- [ ] No `Co-Authored-By` / Claude / AI attribution anywhere in source or commits
- [ ] All chat / boss bar / leaderboard text is in French
- [ ] All log messages, identifiers, JSON keys are in English
- [ ] No `@OnlyIn(Dist.CLIENT)` annotation
- [ ] No reference to old food-collection symbols (`FoodScoreManager`, `FoodUnitCalculator`, `setpoint`, `deposit`)
- [ ] Manual run on a Forge 1.20.1 server with at least one modded zombie tested
- [ ] `README.md` and `CHANGELOG.md` updated (bilingual)

## 7. Environment Setup

```bash
git clone <repo-url>
cd mod-recraft-collect
./gradlew build                      # produces build/libs/recraftcollect-<version>.jar
./gradlew runServer                  # local Forge dev server with the mod loaded
```

- **JDK**: 17 (any vendor). Toolchain auto-resolves via foojay.
- **IDE**: IntelliJ IDEA Community / Ultimate or Eclipse. Run `./gradlew genIntellijRuns` (IDEA) or `./gradlew genEclipseRuns` (Eclipse) to create run configs.
- **Test world**: include Zombie Extreme, Undead Revamp 2, Apocalypse Now, Spawn Eggs to validate the default value config end-to-end.

## 8. AI Assistant Instructions

1. **Never bump the project version unless explicitly asked.** v2.0.0 is the current locked version.
2. **Never add `Co-Authored-By: Claude` / AI attribution** in commits, file headers, or comments.
3. **Communicate with the user in French.** Code, identifiers, comments, JSON keys stay in English.
4. **Preserve `vyrriox` as author** wherever metadata is present (`mods.toml`, code headers, `package.json`, etc.). Never strip co-authors.
5. **The mod is server-only.** Reject any client-side rendering, `@OnlyIn(Dist.CLIENT)` code, or asset (texture/model) work.
6. **Persistence is sacred.** When changing `ZombieScoreManager`, always read missing NBT keys defensively (`tag.contains(...)`) so v1.x and v2.x saves both load.
7. **Modded entity IDs are best-effort.** The default config in `ZombieValueConfig.initDefaults()` lists guessed IDs. If the user reports a tier mismatch, fix the ID in the defaults *and* mention `/zk setvalue` for in-game fixes.
8. **Test before you ship.** Always run `./gradlew build` before reporting work as complete. Never claim "it works" without compilation success.
9. **Player feedback is a UX contract.** Action-bar messages must use the per-tier color scheme defined in `ReCraftCollect.sendKillFeedback`. Don't change colors without user approval.
10. **Push automatically after a code change** (per `CLAUDE.md` global rule), but exclude `.claude/` and `CLAUDE.md` files via `.gitignore`.
11. **Fix bugs autonomously.** If a regression is found in logs or while testing, identify root cause, apply minimal fix, log to `ERROR_LOG.md`, and push.
