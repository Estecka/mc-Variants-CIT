# v1
## 1.0
### 1.0.0
- Initial Release as Enchants-CIT
### 1.0.1
- Fixed an incompatibility with ModernFix
## 1.1
- Added the `level` model predicate for enchanted books.

# v2
## 2.0
- Renammed from Enchants-CIT to Variants-CIT
- CIT logic is now modular, and can be extended via an api
- CIT modules are now enabled and configured via resource packs
- Added CIT modules: `axolotl_variant`, `custom_data`, `instrument`, `jukebox_playable`, `potion_type`, `stored_enchantments`
- Removed embedded resource pack.
## 2.1
- Added the `custom_name` module
- Fixed the `parameters` block being mandatory on modules with only optional parameters.
- Fixed items with no variant using the fallback model instead of the vanilla one.
## 2.2
- `custom_data` module now has a `caseSensitive` option.
- Parameterized modules no longer need to register a constructor. They may be built directly from their codec instead.
- Special modules no longer need to register a constructor. They may be registered the same way as Simple modules.
- Special modules are no longer required to implement their variant logic separately from the special logic.
- The old ways of registering special and parameterized modules has been marked as deprecated.
## 2.3
- It's now possible to apply multiple modules to a single item.
- It's now possible to apply a single module to multiple items.
- Added the module `enchantment` for tools and armours.
- `stored_enchantments` (plural) is being renamed to `stored_enchantment` (singular).
- `custom_data` may now look for variants inside nested pathes. The parameter `nbtKey` is being deprecated in favor of `nbtPath`.
- `custom_name` will now always convert all names into valid identifiers. The case sensitivity option was removed.
- `custom_name`'s special names are now case-sensitive.
## 2.4
- Modules may now automatically generate models for provided textures.
- Optimized some modules, reducing how often they recompute an item's model.
- Fixed Trident and spyglass (in stack form) not being affected by CITs.
## 2.5
### 2.5.0
- Added the `painting_variant` module.
- Added the `requiredEnchantments` parameter to the `enchantment` module.
- Started looking for modules in `variants-cit`, marked `variant-cits` as deprecated.
### 2.5.1
- Updated for MC 1.21.2
## 2.6
### 2.6.0
- Added the modules `bucket_entity_data`, `entity_data` and `block_entity_data`.
- Added `bucket_entity_age` model predicate.
### 2.6.1
- Fixed rendering of trident in hand.
## 2.7
### 2.7.0
- Added the option `levelSeparator` to the module `stored_enchantment`.
### 2.7.1
- `custom_data` and siblings will now accept numeric data.
## 2.8
- Added the modules `trim`, `trim_pattern` and `trim_material`.
## 2.9
- Added modules: `component_data` and `component_format`.
- `custom_data` etc. now accept parameters similar to the `component_data` module.
- `custom_data` etc.'s `nbtPath` can now navigate through arrays and key sets.
- `custom_data` etc.'s `caseSensitive` option is being deprecated in favor of `transform`
- Fixed `custom_data` etc. adding a type suffix to numeric data.
- Modules may now specify `items` as a single value instead of an array.
## 2.10
### 2.10.0
- Added module: `item_count`
- Module `axolotl_variant` now has built-in support for baby variants.
- Modules `component_data` and `component_format` can now use non-component and non-nbt based properties: `item_type`, `axolotl_variant`, `bucket_entity_age`, `painting_variant`.
- Fixed `component_format` not accepting "`:`" as a valid character in the format.
- Fixed `discard_namespace` not discarding the "`:`" separator
- Fixed `nbtPath` no longer accepting uppercases as valid characters. (Regression in 2.9)
- Fixed some legacy `nbtPath` format no longer working. (Regression in 2.9)
### 2.10.1
- Fixed crash when loading an inexistent item name.
## 2.11
### 2.11.0
- Item properties may now take a fallback.
- Added a regex transform.
- Fixed overly lenient json parsing silently failing and falling back.
- Fixed a crash on malformed json.
### 2.11.1
- Fixed crash on invalid regex substitution strings.
## 2.12
- Added `durability` module.
- Added options `matchAll` and `validate` to the regex transform.
- Added `sanitize_auto` transform.
- Added `rich_text_array` data type. (E.g: lore component)
- Added `auto` data type.
- `expect` now accept arrays.
- `nbtPath` is now optional and defaults to using the whole component as data.
- Added simplified mono-string syntax for ItemComponent properties.
- Format variables now accept upper-cases, numbers and low-dashes
- Marked module types `custom_data`, `entity_data`, `bucket_entity_data` or `block_entity_data` as deprecated. (Use `component_data` instead.)
## 2.13
### 2.13.0
- Fixed faulty performance optimisation on a few specific modules.
- Unified the `enchantment` and `stored_enchantment` modules:
	- `enchantment` can now include an enchantment's level in the variant id, and use the `multi` special model.
	- `stored_enchantment` will now apply CITs to books with multiple enchantments, if `multi` is left undefined.
	- `stored_enchantment` can now use the `requiredEnchantments` parameter.
	- The `multi` special model will only count enchantments outside of the requirements.
### 2.13.1
- Made the debug options for `component_data` and `component_format` a bit more verbose.
## 2.14
- Added transforms: `null`, `test`, `successive`, `alternative`, `whitelist`, `blacklist`, `remap`, `charset_remap`.
- All transforms can now be marked as optional, or take a fallback.
## 2.15
- Transform `charset_remap` can now replace a single character with a longer string.
## 2.16
- Added quoting and escaping syntax to nbtPath keynames.

# v3
## 3.0
Since v2.5:
- Now using `items/` instead of `models/` as primary assets.
- Added `itemsFromModels` options to modules. Defaults to true to provide backward compatibility.
- Corresponding models and textures will now be searched for in `models/item/` and `textures/item/`, instead of `models/` and `items/`.
- Fallback models, special models, and model prefixes will have their leading `item/` stripped off, in order to offer some backward compatibility with older packs.
- Added the numeric property `variants-cit:stored_enchantment_level`, as a replacement for the `level` model override predicate
- Java API: Removed all deprecated methods.
- Java API: Replaced all `ModelIdentifier`s with plain `Identifier`s.
## 3.1
- Added module types from v2.6
- Added numeric properties `custom_data`, `bucket_entity_data`, `entity_data` and `block_entity_data`, as a replacement for the `bucket_entity_age` predicate.
- Added a mechanism to automatically populate `dispatch_range`'s entries.
- Added a mechanism to make modded item states compatible with vanilla.
## 3.2
### 3.2.0
- Added the option `levelSeparator` to the module `stored_enchantment`. (Merged with v2.7)
### 3.2.1
- `custom_data` and siblings will now accept numeric data. (Merged with v2.7.1)
## 3.3
- Added the modules `trim`, `trim_pattern` and `trim_material`. (Merged with v2.8)
## 3.4
### 3.4.0
- Added modules: `component_data` and `component_format`. (Merged with v2.9)
- `custom_data` etc. now accept parameters similar to the `component_data` module.
- `custom_data` etc.'s `nbtPath` can now navigate through arrays and key sets.
- `custom_data` etc.'s `caseSensitive` option is being deprecated in favor of `transform`
- Fixed `custom_data` etc. adding a type suffix to numeric data.
- Modules may now specify `items` as a single value instead of an array.
### 3.4.1
- Fixed special models not loading properly. (Regression introduced in v3.0)
### 3.4.2
- Fixed crash when loading a fallback model. (Regression introduced in v3.4.1)
## 3.5
### 3.5.0
(Merged with v2.10)
- Added module: `item_count`
- Module `axolotl_variant` now has built-in support for baby variants.
- Modules `component_data` and `component_format` can now use non-component and non-nbt based properties: `item_type`, `axolotl_variant`, `bucket_entity_age`, `painting_variant`.
- Fixed `component_format` not accepting "`:`" as a valid character in the format.
- Fixed `discard_namespace` not discarding the "`:`" separator
- Fixed `nbtPath` no longer accepting uppercases as valid characters. (Regression in 3.4)
- Fixed some legacy `nbtPath` format no longer working. (Regression in 3.4)
### 3.5.1
- Fixed crash when loading an inexistent item name.
## 3.6
### 3.6.0
- Item properties may now take a fallback.
- Added a regex transform.
- Fixed overly lenient json parsing silently failing and falling back.
- Fixed a crash on malformed json.
### 3.6.1
- Fixed crash on invalid regex substitution strings.
## 3.7
- Added `durability` module.
- Added options `matchAll` and `validate` to the regex transform.
- Added `sanitize_auto` transform.
- Added `rich_text_array` data type. (E.g: lore component)
- Added `auto` data type.
- `expect` now accept arrays.
- `nbtPath` is now optional and defaults to using the whole component as data.
- Added simplified mono-string syntax for ItemComponent properties.
- Format variables now accept upper-cases, numbers and low-dashes
- Marked module types `custom_data`, `entity_data`, `bucket_entity_data` or `block_entity_data` as deprecated. (Use `component_data` instead.)
## 3.8
### 3.8.0
- Modules can now override the `assetId` of the `equippable` component, and change the look of equipped armor.
### 3.8.1
- Renaming the `aspect` field to `context`.
### 3.8.2
- Made equippable cache use a weak map.
## 3.9
### 3.9.0
- Removed the previously deprecated `aspect` field.
- Fixed faulty performance optimisation on a few specific modules.
- Unified the `enchantment` and `stored_enchantment` modules:
	- `enchantment` can now include an enchantment's level in the variant id, and use the `multi` special model.
	- `stored_enchantment` will now apply CITs to books with multiple enchantments, if `multi` is left undefined.
	- `stored_enchantment` can now use the `requiredEnchantments` parameter.
	- The `multi` special model will only count enchantments outside of the requirements.
### 3.9.1
- Made the debug options for `component_data` and `component_format` a bit more verbose.
## 3.10
- Added transforms: `null`, `test`, `successive`, `alternative`, `whitelist`, `blacklist`, `remap`, `charset_remap`.
- All transforms can now be marked as optional, or take a fallback.
## 3.11
### 3.11.0
- Modules `durability` and `item_count` may now use the fallback model.
- Added modules `enchantment_vector` and `stored_enchantment_vector`.
- Transform `charset_remap` can now replace a single character with a longer string.
### 3.11.1
- Fixed `parameters` having become mandatory on all modules.
## 3.12
- Module types may now restrict what variant ids they are allowed to collect and generate.
- Modules with more specific `modelPrefix`es will take priority in case of conflicting `modelParent`s.
- Reduced log spam in case of conflicting `modelParent`s.
- Modules may now collect assets that have been generated by other modules.
- Fixed extreme loading times when too many modules crawl through large zip packs.
## 3.13
### 3.13.0
- Added quoting and escaping syntax to nbtPath keynames.
- Added `/variants-cit` commands: `summary`, `walkthrough` `dump`.
- Module IDs now longer includes the leading `item/`
### 3.13.1
- Fixed crash on `enchantment_vector` when set to ignore enchantment levels.
- Fixed `optionalLevel` defaulting to false instead of true.
## 3.14
- Generated assets are now provided via a built-in virtual resource pack.
- Added VCIT asset types: `templates` and `assetgen_presets`.
- Added module option `assetGen`.
- Added command `/variants-cit assetgen`.
## 3.15
- Added the `radicalPath` option to asset generators.
- Added the `RADICAL` variables to template.
- Modules will now be looked for in `modules/` in addition to the old directories.
## 3.16
### 3.16.0
- Added command: `/variants-cit assetgen createPack`
- Renamed existing commands.
- Changed the radical so it defaults to the output rather than the input.
- Fixed the `RADICAL` template variables using an incorrect value.
- Fixed a crash when certain modules try to access null components.
- Added module `component_threshold`
### 3.16.1
- Fixed the `fishing_rod` assetGen preset.
