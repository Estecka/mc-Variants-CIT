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
- `custom_data` may now look for variants inside nested pathes. The parameter `nbtKey` is being renamed to `nbtPath`.
- `custom_name` will now always convert all names into valid identifiers. The case sensitivity option was removed.
- `custom_name`'s special names are now case-sensitive.
## 2.4
- Modules may now automatically generate models for provided textures.
- Optimized some modules, reducing how often they recompute an item's model.
- Fixed Trident and spyglass (in stack form) not being affected by CITs.
## 2.5
- Added the `painting_variant` module.
- Added the `requiredEnchantment` parameter to the `enchantment` module.
- Started looking for modules in `variants-cit`, marked `variant-cits` as deprecated.
