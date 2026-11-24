# AGENTS.md

Guidelines for AI agents (and humans) working on **LivestockCraft**.
Read this file fully before making any change.

---

## 1. Project overview

LivestockCraft is a Minecraft mod about livestock breeding and domestication.

- **Target:** Minecraft **1.21.1** only, loaders **NeoForge** and **Fabric**. Forge is NOT supported.
- **Publishing:** Modrinth and CurseForge.
- **Mod id:** `livestockcraft`
  - **Base package:** `fr.noltox.livestockcraft`
- **License:** MIT
- **Language of the project:** code, comments, commits, docs and issues are written in **English**.
  (Discussions with the maintainer may happen in French.)
- **Models/animations:** 3D models made in Blockbench, rendered and animated with **GeckoLib 4.x** (required dependency on both loaders).

### Core concept

Every animal (vanilla or not) has breeds and behaviors depending on the biome.

1. **Wild form** - spawns naturally: sturdy, aggressive, distinct look (e.g. aurochs for cows).
2. **Taming** - via food. The animal becomes passive but is not yet domesticated.
3. **Domestication rate (%)**, increases with each birth, **proportional to the parents' rate**.
4. **Tiers** - at thresholds (e.g. 25%, 50%, ...) the animal changes **model, texture, behavior and yields** (meat, milk, eggs, wool...).
5. **Common pets** (dog, cat...) can unlock extra features at tiers (e.g. dog gets a sheepdog ability at 50%).

### Design principles (anti-goals included)

- Animals must stay **vanilla-like**: no overly complex behavior or AI.
- Breeding must stay **easy and automation-friendly** (farms, hoppers, redstone). Never add mechanics that break automation.
- The focus is on **visual beauty** and the **domestication system**, not on grind.
- Behavior and yields must be **data-driven** whenever possible (config, datapacks, loot tables), so they are tweakable by modpack makers.

---

## 2. Repository layout

The project is based on the *Stonecutter Mod Template*. Key facts:

- There are **no separate Gradle modules per loader**. A single shared `src/main/java` is used by all loaders.
  Stonecutter creates one sub-project per version + loader (e.g. `1.21.1-fabric`, `1.21.1-neoforge`) and toggles loader-specific code with comments such as `//? fabric {`.
- **Common code** lives in `fr.noltox.livestockcraft`.
- **Loader-specific code** lives in `platform/{fabric,neoforge}/`, behind a `Platform` interface. Keep it thin: init, registration, data access only.
- **`build-logic/`** holds custom Gradle plugins. They **generate `fabric.mod.json` and `neoforge.mods.toml` at build time** from `stonecutter.properties.toml` and each `build.*.gradle.kts`.
  - Never hand-write these metadata files.
  - Dependencies are declared in `platform { dependencies { required(...) } }` and their versions in `stonecutter.properties.toml`. This also feeds Modrinth/CurseForge publishing.
- **`.sc_active_version`** selects the active Stonecutter version. A pre-commit hook blocks commits that change it; only bypass it deliberately (`--no-verify`) and say so in the commit.
- **CI:** `build.yml` and `release.yml` (GitHub Actions). Keep them loader/version agnostic.
- **Datagen:** infrastructure kept for 1.21.1 (loot tables, tags, spawns...). Prefer datagen over hand-written JSON.

Planned packages (create them only when needed):

```text
domestication/   rates, tiers, inheritance logic (pure logic, loader-agnostic)
entity/          entity types, attributes, goals
client/render/   GeckoLib renderers and models
config/          configuration
registry/        registration helpers
```

Assets (GeckoLib): `assets/livestockcraft/geo/`, `assets/livestockcraft/animations/`, `assets/livestockcraft/textures/entity/`.

---

## 3. Build & run

```bash
./gradlew build               # build everything
./gradlew runClient           # run the active version's client
```

Use the Stonecutter active-version tasks to target a specific loader. Check `stonecutter.gradle.kts` before inventing commands. If a command fails, read the error; do not guess.

Java: Minecraft 1.21.1 targets **Java 21** bytecode.

---

## 4. Agent roles

Use the smallest set of agents that fits the task. One agent may play several roles on small tasks, but **never skip the verification role**.

| Role | Responsibility | May edit code? |
| ---- | -------------- | -------------- |
| **Explorer** | Read-only. Maps the code, finds usages, answers "how does X work today?". Reports findings, never changes files. | No |
| **Planner** | Turns a request into small ordered steps, identifies affected files and loader-specific risks. Asks the maintainer when a decision is theirs. | No |
| **Implementer** | Writes code, assets wiring, data. Works on one step at a time. Keeps common code loader-agnostic. | Yes |
| **Verifier** | Builds both loaders, launches `runClient` for each, checks logs, confirms the feature really works. Reports evidence, not assumptions. | No (reports only) |
| **Reviewer** | Reviews a diff against this file: architecture, vanilla feel, automation-friendliness, commit hygiene. | No (comments only) |

### Rules for all agents

1. **Explore before editing.** Read the relevant code and this file first. Never assume the layout.
2. **No scope creep.** Do only what was asked. Mention extra ideas instead of implementing them.
3. **Ask when it is the maintainer's call** (design, naming of gameplay features, deleting files you are unsure about). Otherwise decide, and state the decision.
4. **Never claim success without evidence.** "Builds" means `./gradlew build` passed for **both** NeoForge and Fabric. "Works" means it was checked in a running client.
5. **Do not delete** files you are unsure about; ask first.
6. **Do not touch** generated metadata, `.sc_active_version`, or CI unless the task requires it.
7. **Dependencies:** add them through `build-logic`/`stonecutter.properties.toml`, pinned to a version verified in official docs or the official Maven. Never guess a version.
8. **Parallel work:** only split tasks that do not share files. Each agent states which files it owns.
9. **Final report:** what changed, what was verified (with commands), what is left or uncertain.

### Code rules

- Common logic first: put domestication math, tier thresholds and inheritance in loader-agnostic classes that are easy to unit test.
- Loader-specific code stays behind `Platform` or `//? loader {` blocks and stays minimal.
- Tier thresholds, yields, spawn weights and inheritance factors must be **configurable or data-driven**, not hard-coded constants scattered in the code.
- Keep entities close to vanilla behavior: reuse vanilla goals and attributes; only tune values per tier.
- Synchronize anything the client needs for rendering (e.g. domestication tier) through the proper synced data mechanism, not ad-hoc packets.
- Add tests for pure logic (inheritance formula, tier resolution) when feasible.

---

## 5. Git & commit conventions

Commit messages must be **compatible with automatic commit generation in VS Code** (GitHub Copilot) and readable by humans.

### Format

```text
{emoji} {simple and technical summary, in English}

- {change 1}
- {change 2}
```

- **Subject line (required):** one emoji, one space, then a short imperative summary that is both simple and technical. Max ~72 characters, no trailing period.
- **Body (optional but recommended):** a blank line, then a bullet list (hyphen bullets) of the changes made. One bullet per meaningful change.
- Everything is in **English**, imperative mood (`Add`, `Fix`, `Remove`, not `Added`/`Adds`).
- One logical change per commit. Do not mix cleanup, features and fixes.
- Do not add other prefixes (`feat:`, `fix:`...) - the emoji is the type.

### Emoji reference

| Emoji | Use for |
| ----- | ------- |
| ✨ | New feature |
| 🐛 | Bug fix |
| ♻️ | Refactor (no behavior change) |
| ⚡ | Performance |
| 🔥 | Remove code or files |
| 🪛 | Build, Gradle, Stonecutter, dependencies |
| 🔧 | Configuration |
| 🎨 | Models, textures, animations, assets |
| 🌍 | Lang files / translations |
| 📝 | Documentation |
| ✅ | Tests |
| 👷 | CI / GitHub Actions |
| 🔖 | Release / version bump |
| 🎉 | Initial commit / project setup milestone |
| 🚧 | Work in progress |

### Examples

```text
🔥 Remove Forge loader and legacy Minecraft versions

- Delete build.forge.gradle.kts and platform/forge
- Drop 1.19.2, 1.21.7, 1.21.11 and 26.x from Stonecutter settings
- Remove Loader.Forge branches from build-logic
```

```text
🪛 Add GeckoLib 4.x dependency for NeoForge and Fabric

- Declare required dependency in both build.*.gradle.kts
- Pin the version in stonecutter.properties.toml
```

```text
✨ Add domestication rate inheritance on breeding

- Compute child rate from the parents' average rate
- Clamp the result to the configured min and max
- Add unit tests for the inheritance formula
```

```text
🐛 Fix cow tier not synced to the client
```

### VS Code setup (maintainer)

To make Copilot generate messages in this format, add to `.vscode/settings.json`:

```json
{
  "github.copilot.chat.commitMessageGeneration.instructions": [
    {
      "text": "Write the commit message in English using this format: '{emoji} {simple and technical summary in imperative mood, max 72 chars}', then a blank line and a bullet list ('- ') of the changes made. Use one emoji from: ✨ feature, 🐛 fix, ♻️ refactor, ⚡ performance, 🔥 remove, 🪛 build/dependencies, 🔧 config, 🎨 assets/models, 🌍 lang, 📝 docs, ✅ tests, 👷 CI, 🔖 release, 🎉 setup, 🚧 WIP. No 'feat:'/'fix:' prefixes. No trailing period on the subject."
    }
  ]
}
```

### Changelog note

`cliff.toml` (git-cliff) generates the changelog. If it parses conventional commits, adapt its commit parsers to the emoji format above before relying on it for releases.

---

## 6. Definition of done

A task is done only when:

- [ ] The change matches the request and stays in scope.
- [ ] `./gradlew build` passes for NeoForge **and** Fabric.
- [ ] The feature was checked in a running client when it is player-visible.
- [ ] Domestication/yield values are configurable or data-driven where relevant.
- [ ] No Forge or other-version leftovers, no dead example code.
- [ ] Commits follow section 5.
- [ ] The final report lists changes, verification evidence and open questions.
