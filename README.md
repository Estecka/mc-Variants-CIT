# Enchants CIT
A specialized CIT logic for enchanted books, made to allow enchantment-based resource packs to work on MC 1.21 without the help of optifine nor CIT-resewn.

**This mod is not a plug-and-play replacement for Optifine or CIT-resewn.**
It uses its own resource format, and requires some changes be made to older packs for them to work.

The current release includes the textures from [**Even Better Enchants**](https://modrinth.com/resourcepack/even-better-enchants).

## Resource Pack Format

The mod looks for [item models](https://minecraft.wiki/w/Model#Item_models) with pathes formatted as  
**`/assets/<namespace>/models/item/enchanted_book/<name>.json`**, where `<namespace>` and `<name>` match an enchantment's identifier.

The model for books with multiple enchantments is hardcoded at:  
`/assets/enchants-cit/models/item/multi_enchanted_book.json`

Texture pathes are defined in the model themselves; I recommend following the same naming convention as the models. (Simply replace `models` with `textures` in the path.)
