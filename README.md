# Variants-CIT
A CIT logic for MC 1.21, optimized around items with standardized variants.

**This mod is not a plug-and-play replacement for Optifine/CIT-resewn;** it uses its own resource format. Changes to older packs are required for them to work.

The mod contains built-in logic for handling **Axolotl Buckets**, **Enchanted Books**, **Music Discs**, **Goat Horns**, and **Potions**. 
There are also more generic modules that can identify a variant from the `custom_data` or `custom_name` component of an item, other mods can easily create custom logic for their own items.

If Mojang ever makes these items data-driven, you can expect Banner Patterns, Trim Templates, and Pottery Sherds to become supported in the future. 

## Difference with Optifine/CIT-Resewn
The base concept for this mod was born out of a need for _optimization_, at a time when optifine was still an up-to-date CIT option. 
This comes at the cost of some flexibility; while being _multi_-purpose, it may not be as _all_-purpose.

The typical scenarios this mod targets are when a single item type has a large amount of variants, and all those variants can be unified under a single all-encompassing rules (so-called modules).

## Resource Pack Format
This is an overview, see the [wiki](https://github.com/Estecka/mc-Variants-CIT/wiki) for a complete guide.

The format revolves around item variants (reduced to namespaced identifiers) being automatically associated to [item models](https://minecraft.wiki/w/Model#Item_models) with matching names, stored in a directory of your choosing.

Resource packs must start by providing a module configuration, that defines what item is affected, how to figure out its variant, and where their models are located.

For example, here's a module that would reproduce the behaviour of the previous version of the mod, Enchants-CIT :  
`/assets/minecraft/variant-cits/item/enchanted_book.json`
```json
{
	"type": "stored_enchantments",
	"items": ["enchanted_book"],
	"modelPrefix": "item/enchanted_book/",
	"special": {
		"multi": "enchants-cit:item/multi_enchanted_book"
	}
}
```
Here, the enchantment `minecraft:unbreaking` will be associated with the model stored at `/assets/minecraft/models/item/enchanted_book/unbreaking.json`

Some module types may define additional models to use in special cases, or take addional parameters.
