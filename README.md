# Enchants CIT
A specialized CIT logic for enchanted books, made to allow [Even Better Enchants](https://modrinth.com/resourcepack/even-better-enchants) to work on MC 1.21 without the help of optifine nor CIT-resewn.

## Resource Pack Format

The mod will looks for enchantment-specific [item models](https://minecraft.wiki/w/Model#Item_models) located at **`/assets/<namespace>/models/item/enchanted_book/<name>.json`**, where `<namespace>` and `<name>` correspond to the enchantment's identifier.

Texture pathes are defined in the model themselves. By default, I follow the same naming convention as the models, (simply replace `models` with `textures`).

## Caveat

**This mod is technically incompatible with the current release of EBE.**

As of writting, the original resource pack stores its textures outside of the `textures` folder, making them inaccessible by vanilla model definitions.
Instead, this mods includes EBE's textures at different locations.
