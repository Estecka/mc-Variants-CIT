# Enchants CIT
A specialized CIT logic for enchanted books, made to allow enchantment-based resource packs to work on MC 1.21 without the help of optifine nor CIT-resewn.

**This mod is not a plug-and-play replacement for Optifine or CIT-resewn.**
It uses its own resource format, and requires some changes be made to older packs for them to work.

The current release includes the textures from [**Even Better Enchants**](https://modrinth.com/resourcepack/even-better-enchants).

## Resource Pack Format

### Enchantment specific models
The mod looks for [item models](https://minecraft.wiki/w/Model#Item_models) with pathes formatted as  
**`/assets/<namespace>/models/item/enchanted_book/<name>.json`**, where `<namespace>` and `<name>` match an enchantment's identifier.

Texture pathes are defined in the model themselves; I recommend following the same naming convention as the models. (Simply replace `models` with `textures` in the path.)

### Level specific models
Models for different levels of enchantment can be defined in the overrides of the aforementioned models, using the `level` predicate.


Example: `/assets/minecraft/models/item/enchanted_book/unbreaking.json`
```json
{
	"parent": "item/enchanted_book_lvl/unbreaking_1",
	"overrides": [
		{
			"predicate": { "level": 2 },
			"model": "item/enchanted_book_lvl/unbreaking_2"
		},
		{
			"predicate": { "level": 3 },
			"model": "item/enchanted_book_lvl/unbreaking_3"
		}
	]
}
```

Models for overrides should be stored **outside** of `item/enchanted_book` and subfolders, otherwise each level will also be treated as its own enchantment. (This is unlikely to cause any issue, but is less optimised.)

### Multi-enchantment
The mod only supports a single model for books with multiple enchantments, hardcoded at:  
**`/assets/enchants-cit/models/item/multi_enchanted_book.json`**

The behaviour of the `level` predicate is undefined on this type of books.
