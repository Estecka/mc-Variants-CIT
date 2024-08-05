# Enchants CIT
A specialized CIT logic for enchanted books, made to allow enchantment-based resource packs to work on MC 1.21 without the help of optifine nor CIT-resewn.

**This mod is not a plug-and-play replacement for Optifine or CIT-resewn.**
It uses its own resource format, and requires some changes be made to previous packs for them to work.

The current release of the mod includes the textures from [Even Better Enchants](https://modrinth.com/resourcepack/even-better-enchants).

## Resource Pack Format

The mod will looks for enchantment-specific [item models](https://minecraft.wiki/w/Model#Item_models) located at **`/assets/<namespace>/models/item/enchanted_book/<name>.json`**, where `<namespace>` and `<name>` correspond to the enchantment's identifier.

The model for book with multiple enchanted is hardcoded at `/assets/enchant-cit/models/item/multi_enchanted_book.json`

Texture pathes are defined in the model themselves. By default they follow the same naming convention as the models, (simply replace `models` with `textures` in the path).
