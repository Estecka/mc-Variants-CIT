# Variants-CIT
An alternative CIT format designed to handle large amounts of variants.

This mod isn't as flexible as optifine, but excels in scenarios where one item has many variants all based on the same pieces of data. It yields better performances when extreme amounts of CITs are available, and uses a resource format that is less redundant, requiring only one short file to configure all possible variants of an item at once.

## Resource Pack Format
This is an overview, please see the [wiki](https://github.com/Estecka/mc-Variants-CIT/wiki) for a complete guide.

The format revolves around item variants being automatically associated to models or textures with matching names.
Instead of defining a condition for every CIT, you define a single rule that governs all CITs in a collection, (so-called **modules**). This module defines what item is affected, how to figure out its variants, and where their models are located.

For example, here's a simple module that would change the texture of enchanted books :
```json
{
	"type": "stored_enchantment", // How to compute the item's "variant ID"
	"items": "minecraft:enchanted_book", // The affected item type(s)
	"modelPrefix": "item/book_cit/", // The folder containing the possible models/textures.
	"assetGen": "item_model/generated", // Automatically generate models from texture (if models are missing)
	"parameters": { // Extra options specific to the module type (specified at the top)
		"levelSeparator": "_lvl_" // Include the enchantment level in the variant ID
	}
}
```
Here, a book with the enchantment `minecraft:unbreaking` at level 2 will have the variant ID `minecraft:unbreaking_lvl_2`, and thus use the texture stored at `/assets/minecraft/texture/item/book_cit/unbreaking_lvl_2.png`. 
This single module will work for every possible enchantment, vanilla or modded, so long as a corresponding texture exists.

The module type above has a purpose-made logic for enchanted books. If no module type exists for a specific component, you can still use more generic modules to get a variant from any component:

```json
{
	"type": "component_data",
	"items": "minecraft:suspicious_stew",
	"modelPrefix": "item/suspicious_stew_cit/",
	"parameters": {
		"componentType": "suspicious_stew_effects", // The component containing the variant ID
		"nbtPath": "[0].id" // The location of the variant ID in the component.
	}
}
```

At a higher level, you can create variants by combining multiple pieces of data from multiple components:
```json
{
	"type": "component_format",
	"items": "minecraft:diamond_sword",
	"modelPrefix": "item/trimmed_diamond_sword/",
	"parameters":{
		"format": "${pattern}_${material}", // How to combine various pieces of data into a variant ID
		"variables": { // Where to find those pieces of data.
			"pattern": {
				"componentType": "trim",
				"nbtPath": ".pattern"
			},
			"material": {
				"componentType": "trim",
				"nbtPath": ".material",
				"transform": "discard_namespace"
			}
		}
	}
}
```
