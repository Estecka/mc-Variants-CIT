# Variants-CIT
A CIT logic optimized around items with standardized variants.

This mod isn't as flexible as optifine, but excels in scenarios where one item has many variants based on the same piece of data. It yields better performances when extreme amounts of CITs are available, and uses a resource format that is less redundant, requiring only a short file to configure all variants at once.

## Supported Components
The mod can handle **Axolotl Buckets**, **Enchantments**, **Music Discs**, **Goat Horns**, **Painting Variants**, and **Potions**. Other mods can easily create add logic for their own components or items.
There are also more generic modules that can identify a variant from the `custom_data` or `custom_name` component of an item. 

If Mojang ever makes these items data-driven, you can expect Banner Patterns, Trim Templates, and Pottery Sherds to become supported in the future. 

## Resource Pack Format
This is an overview, please see the [wiki](https://github.com/Estecka/mc-Variants-CIT/wiki) for a complete guide.

The format revolves around item variants being automatically associated to models or textures with matching names.
Instead of defining a condition for every CIT, you define a single rule that governs all CITs in a collection, (so-called **modules**). This modules defines what item is affected, how to figure out its variants, and where their models are located.

For example, here's a simplistic module that would change the texture of enchanted books :
```json
{
	"type": "stored_enchantment",
	"items": ["enchanted_book"],
	"modelPrefix": "item/book_cit/",
	"modelParent": "item/generated"
}
```
Here, a book with the enchantment `minecraft:unbreaking` will use the texture stored at `/assets/minecraft/texture/item/book_cit/unbreaking.png`. This single module will work for every enchantment, vanilla or modded, so long as a corresponding texture exists.
