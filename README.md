# Enchants CIT
A specialized CIT logic for enchanted books, made to allow enchantment-based resource packs to work on MC 1.21 without the help of optifine nor CIT-resewn.

**This mod is not a plug-and-play replacement for Optifine or CIT-resewn.**
It uses its own resource format, and requires some changes be made to older packs for them to work.

The current release includes the textures from [**Even Better Enchants**](https://modrinth.com/resourcepack/even-better-enchants).

## Resource Pack Format

### Enchantment-specific models
The mod looks for [item models](https://minecraft.wiki/w/Model#Item_models) with pathes formatted like this, where `<namespace>` and `<name>` match an enchantment's identifier:  
**`/assets/<namespace>/models/item/enchanted_book/<name>.json`**

Texture pathes are defined in the models themselves; I recommend following the same naming convention as the models. (Simply replace `models` with `textures` in the path.)

Enchantments with no model will fall back to the vanilla one.

### Level-specific models
The mod provides a **`level`** predicate, which can be used to define overrides in the aforementioned models.

**The models used as overrides should be stored outside of `item/enchanted_book` and its subfolders,** otherwise each level will also be treated as its own enchantment. (This is unlikely to cause bugs, but is less optimised.)

Example: `/assets/minecraft/models/item/enchanted_book/unbreaking.json`
```json
{
	"parent": "item/levelled_enchanted_book/unbreaking_1",
	"overrides": [
		{
			"predicate": { "level": 2 },
			"model": "item/levelled_enchanted_book/unbreaking_2"
		},
		{
			"predicate": { "level": 3 },
			"model": "item/levelled_enchanted_book/unbreaking_3"
		}
	]
}
```

### Multi-enchantment
The mod only supports a single model for books with multiple enchantments, hardcoded at:  
**`/assets/enchants-cit/models/item/multi_enchanted_book.json`**

The behaviour of the `level` predicate is undefined on this type of books.
