# GeckoLib asset workflow

LivestockCraft animals are modelled in [Blockbench](https://www.blockbench.net/) and
rendered with [GeckoLib](https://github.com/bernie-g/geckolib) 4.x (Minecraft 1.21.1).

## Folders

All paths are under `src/main/resources/assets/livestockcraft/`:

| Folder | Content | File name |
| ------ | ------- | --------- |
| `geo/` | Models | `<name>.geo.json` |
| `animations/` | Animations | `<name>.animation.json` |
| `textures/entity/` | Textures | `<name>.png` |

Use the same lowercase `snake_case` name for the three files of one model,
e.g. `aurochs.geo.json`, `aurochs.animation.json`, `aurochs.png`.

## From Blockbench to the mod

1. In Blockbench, install the **GeckoLib Animation Utils** plugin
   (*File > Plugins*, search "GeckoLib").
2. Create the model with *File > New > GeckoLib Animated Model*, object type **Entity**.
   Keep the texture size used by the model (e.g. 64x64).
3. Name bones clearly (`head`, `body`, `leg_front_left`...): renderers and animations
   reference bones by name.
4. Export with the GeckoLib plugin's export actions:
   - the model (`.geo.json`) into `geo/`
   - the animations (`.animation.json`, from the *Animate* tab) into `animations/`
   - the texture (`.png`, from the texture's context menu) into `textures/entity/`
5. Run the dev client (`./gradlew :1.21.1-fabric:runClient` or
   `:1.21.1-neoforge:runClient`) and check the entity in game.

## Domestication tiers

Each domestication tier can have its own look. Use one model, animation and texture set
per tier, with the tier as a suffix:

```text
geo/aurochs_wild.geo.json
geo/aurochs_tier_1.geo.json
textures/entity/aurochs_wild.png
textures/entity/aurochs_tier_1.png
```

When only the texture changes between two tiers, reuse the same model and animation
files and add a new texture.

## Notes

- Commits that only add or change assets use the 🎨 emoji and must **not** use
  `[ci skip]`: assets are packaged in the jar.
- Empty folders are kept in git with a `.gitkeep` file, which is excluded from the jar.
