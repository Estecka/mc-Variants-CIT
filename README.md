# Variants-CIT
A streamlined CIT format for items with standardized variants.

This mod isn't as flexible as optifine, but excels in scenarios where one item has many variants based on the same piece of data. It yields better performances when extreme amounts of CITs are available, and uses a resource format that is less redundant, requiring only a short file to configure all possible variants at once.

## Supported Components
Specialized modules are available for **Axolotl Buckets**, **Enchantments**, **Goat Horns**, **Music Discs**, **Painting Variants**, **Potions**, and **Trims**. More may be added as needed, and other mods can easily create add logic to it.

There are also more generic modules that can identify a variant from the `custom_data`, `custom_name`, and various `entity_data` components.

## Resource Pack Format
This is an overview, please see the [wiki](https://github.com/Estecka/mc-Variants-CIT/wiki) for a complete guide.

The format revolves around item variants being automatically associated to models or textures with matching names.
Instead of defining a condition for every CIT, you define a single rule that governs all CITs in a collection, (so-called **modules**). This modules defines what item is affected, how to figure out its variants, and where their models are located.

For example, here's a simple module that would change the texture of enchanted books :
```json
{
	"type": "stored_enchantment",
	"items": "minecraft:enchanted_book",
	"modelPrefix": "item/book_cit/",
	"modelParent": "item/generated",
	"parameters": {
		"levelSeparator": "_lvl_"
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
		"componentType": "suspicious_stew_effects",
		"nbtPath": "[0].id"
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
		"format": "${pattern}_${material}",
		"variables": {
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
