# Breaking Changes
## 1.21
Initial Release

## 1.21.2
### No workaround:
- A `BakedModelManager` was moved from `ItemModels` to `ItemRenderer`
- (FAPI) `onInitializeModelLoader` renamed to `initalize`
### Possible workaround:
- `JsonUnbakedModel::deserialize(String)` was removed. The `(Reader)` overload sill exists.
- (Yarn?) `ModelLoader` moved to `ModelBaker`, multiple internals renammed.

## 1.21.5
- `NbtCompound::getXXXX` methods now return optionals instead of nullables. A new overload with fallback can be used to avoid Optional instanciations.
- `NbtElement::asString` no longer works on non-string, and is now wrapped in an optional.
- The structures of Goat Horn and Music Disc components have changed, now using Lazy Registry Entries.
- `Text` components are no longer stored in stringified form.
- `EquippableComponent` takes new field.
- `HorseArmorFeatureRenderer` was replaced with `SaddleFeatureRenderer`

## 1.21.6
- `ItemAsset.Property::new` takes an extra argument
- `EquippableComponent` takes new fields.

## 1.21.9
### No workaround:
- The signature of `BakedModelManager::reload` has changed. The `ResourceManager` passed as argument is now wrapped into another data type.
### Possible workaround:
- `NbtComponent::getNbt` was removed. use `copyNbt`, or create an accessor.

## 1.21.11
### Backward compatible:
- `ItemAsset.Properties::new` takes another parameter. Use the statically available `DEFAULT` instead.
