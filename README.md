# LivestockCraft

> **Status: in development.** Not playable yet, no release published.

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

* **GeckoLib** (required)
* **Fabric API** (required, Fabric only)

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
