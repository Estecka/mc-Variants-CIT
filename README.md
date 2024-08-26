# Variants-CIT
A CIT logic for MC 1.21, specialized in handling items with standardized variants.

**This mod is not a plug-and-play replacement for Optifine/CIT-resewn;** it uses its own resource format. Changes to older packs are required for them to work.
On low-end PCs, using this mod instead of the optifine format may lead to improved performances in scenarios where a single item has very many different variants.

The mod is not as all-purpose as optifine, it still requires specialized code for most item type, but this code is modular and easy to expand upon. Other mods can also add custom modules for their own items.

Built-in modules support **Axolotl Buckets**, **Enchanted Books**, **Music Discs**, **Goat Horns**, and **Potions**.
If Mojang ever makes these items more componentized, you can expect Banner Patterns, Trim Templates, and Pottery Sherds to become supported in the future.

For datapack makers, there is also a more generic module that can pull a variant id from the `custom_data` component of an item.

## Resource Pack Format
A resource pack who wants to modify an item must start by providing a configuration file to declare what item to change, where its resources are located, and which logic to use.
The targetd item type is automatically derived from the file name: `/assets/<namespace>/variants-cit/item/<name>.json`

The mod will look for [item models](https://minecraft.wiki/w/Model#Item_models) stored in a chosen directory, and automatically associate them to variants with the corresponding namespace and name.

For example, here's a module that would reproduce the behaviour of the previous version of the mod, Enchants-CIT :  
`/assets/minecraft/variant-cits/item/enchanted_book.json`
```json
{
	"type": "stored_enchantments",
	"modelPrefix": "item/enchanted_book/",
	"special": {
		"multi": "enchants-cit:item/multi_enchanted_book"
	}
}
```
Here, the enchantment `minecraft:unbreaking` will be associated with the model `minecraft:item/enchanted_book/unbreaking`, which is stored at `/assets/minecraft/models/item/enchanted_book/unbreaking.json`

Some module types may define additional models to use in special cases, or take addional parameters. See the [wiki](https://github.com/Estecka/mc-Variants-CIT/wiki) for a more complete guide.