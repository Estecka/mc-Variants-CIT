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
