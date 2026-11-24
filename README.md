# LivestockCraft

> **Status: in development.** Only early alpha builds are published; no gameplay yet.

LivestockCraft is a Minecraft mod about breeding and domestication. Wild ancestors
(for example the aurochs) are tamed over several generations. At each domestication
stage the animal gets a new model and texture, until it becomes the domestic animal
we know (the cow).

Animals are modelled in [Blockbench](https://www.blockbench.net/) and animated with
[GeckoLib](https://github.com/bernie-g/geckolib).

## Supported versions

| Minecraft | Loaders           | Java |
|-----------|-------------------|------|
| 1.21.1    | NeoForge, Fabric  | 21   |

## Dependencies

Install the jar matching your loader, together with the mods below. The game refuses
to start with a clear error if a required mod is missing or too old.

### NeoForge

| Mod | Status | Minimum version |
| --- | ------ | --------------- |
| [NeoForge](https://neoforged.net/) | Required | 21.1.267 |
| [GeckoLib](https://modrinth.com/mod/geckolib) 4.x | Required | 4.9.4 |

### Fabric

| Mod | Status | Minimum version |
| --- | ------ | --------------- |
| [Fabric Loader](https://fabricmc.net/use/) | Required | 0.20.1 |
| [Fabric API](https://modrinth.com/mod/fabric-api) | Required | 0.116.18+1.21.1 |
| [GeckoLib](https://modrinth.com/mod/geckolib) 4.x | Required | 4.9.4 |
| [Mod Menu](https://modrinth.com/mod/modmenu) | Optional (mod list and config screen) | any |

GeckoLib must be a 4.x version (GeckoLib 5 is not supported). Alpha builds up to
`v26.48.1-alpha` did not need it yet.

Minimum versions come from `stonecutter.properties.toml` and are enforced by the
generated `fabric.mod.json` / `neoforge.mods.toml`.

## Building

```bash
./gradlew build                        # build every loader
./gradlew :1.21.1-fabric:runClient     # Fabric dev client
./gradlew :1.21.1-neoforge:runClient   # NeoForge dev client
```

Jars end up in `versions/1.21.1-<loader>/build/libs/`.

The project uses [Stonecutter](https://stonecutter.kikugie.dev/): a single source tree
in `src/main/java`, with loader-specific code isolated in `platform/{fabric,neoforge}`.
Mod metadata (`fabric.mod.json`, `neoforge.mods.toml`) is generated at build time from
`stonecutter.properties.toml` and the `platform { dependencies { ... } }` block of each
`build.<loader>.gradle.kts`. Don't write these files by hand.

Models, animations and textures follow the Blockbench to GeckoLib workflow described in
[docs/geckolib-workflow.md](docs/geckolib-workflow.md).

## Versioning and releases

LivestockCraft uses [CalVer](https://calver.org/) `YY.0W.MINOR`, based on the ISO week:
`26.48.0` is the first release of week 48 of 2026, `26.48.1` the next one in the same
week. Pre-releases end with `-alpha` or `-beta`. Changes are listed in [CHANGELOG.md](CHANGELOG.md).

Releases are created from GitHub: **Actions > release > Run workflow**, pick a channel
(`alpha`, `beta` or `release`). The workflow computes the version, updates
`stonecutter.properties.toml` and `CHANGELOG.md`, builds both loaders, tags the commit,
creates the GitHub release with the jars and, when enabled, publishes to Modrinth and
CurseForge. Tick **dry-run** to check the version and notes without publishing anything.

Publishing to Modrinth and CurseForge needs these repository settings
(**Settings > Secrets and variables > Actions**):

| Name | Type | Value |
| ---- | ---- | ----- |
| `PUB_MODS_ENABLE` | Variable | `true` |
| `PUB_MODRINTH_PROJECT_ID` | Variable | Modrinth project ID |
| `PUB_CURSEFORGE_PROJECT_ID` | Variable | CurseForge project ID |
| `PUB_MODRINTH_TOKEN` | Secret | Modrinth personal access token |
| `PUB_CURSEFORGE_TOKEN` | Secret | CurseForge API token |

## License

[MIT](LICENSE). The build setup is based on
[rotgruengelb/stonecutter-mod-template](https://github.com/rotgruengelb/stonecutter-mod-template).
