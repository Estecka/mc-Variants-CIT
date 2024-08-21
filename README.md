# Variants-CIT
A specialized CIT logic for miscellanous variant-based items.
This allows the creation of CIT resource packs for MC 1.21, without relying on optifine nor CIT-resewn.

**This mod is not a plug-and-play replacement for Optifine/CIT-resewn.**
It uses its own resource format, and requires some changes be made to older packs for them to work.
On low-end PCs, using this mod instead of the optifine format may lead to improved performances in scenarios where a single item has very many different variants.

## Resource Pack Format
A resource pack who wants to modify an item must declare what item it changes, and where its resources are located.
The item type is inferred from the file name: `/assets/<namespace>/variants-cit/item/<name>.json`

Only one logic is supported per item type. Multiple resource packs that affect the same item will be compatible if they are using the same config. Otherwise, the config from the top-most pack is used. See the wiki for recommendations on what config to use for what item.

Here's a example of CIT module that would reproduce the behaviour of Enchants-CIT :

`/assets/minecraft/variant-cits/item/enchanted_book.json`
```json
{
	"type": "stored_enchantments",
	"variantsDir": "item/enchanted_book",
	"fallback": null,
	"special": {
		"multi": "enchants-cit:item/multi_enchanted_book"
	}
}
```

### `type`
**Mandatory**, Namespaced Identifier

The type of CIT logic to use. This affects how an item's variant is identified, and what special models are available to it.

### `variantsDir`
**Mandatory**, String

The directory where the models for all variants are found, relative to the `models` folder.
All models in this directory are automatically matched to a variant with the corresponding namespace and name.


### `fallback`
**Optional**, Namespaced Identifier. Defaults to the vanilla model.

The model to use for items for which a variant was identified, but not model was provided for this variant. If unset, the vanilla model of the item will be used.

### `special`
**Optional**, maps Strings to Namespaced Identifiers. All values default to the vanilla model.

A list of models that the CIT module may use in certain exceptional circumstances.
The list of models, and when they are used, depends on the `type` of the CIT module.


