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

## License

[MIT](LICENSE). The build setup is based on
[rotgruengelb/stonecutter-mod-template](https://github.com/rotgruengelb/stonecutter-mod-template).
