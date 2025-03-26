# v1
## 1.0
### 1.0.0
- Initial Release
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
- `custom_data` and siblings will now accept numeric data
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
- Added module: `item_count`
- Module `axolotl_variant` now has built-in support for baby variants.
- Module `component_format` can now use non-component and non-nbt based properties: `item_type`, `axolotl_variant`, `bucket_entity_age`
- Fixed `component_format` not accepting "`:`" as a valid character in the format.
- Fixed `discard_namespace` not discarding the "`:`" separator
- Fixed `nbtPath` no longer accepting uppercases as valid characters. (Regression in 2.9)
- Fixed some legacy `nbtPath` format no longer working. (Regression in 2.9)
