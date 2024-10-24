# Breaking Changes
## 1.21
Initial Release

## 1.21.2
### No workaround:
- (FAPI) `onInitializeModelLoader` renamed to `initalize`
### Possible workaround:
- `JsonUnbakedModel::deserialize(String)` was removed. The `(Reader)` overload sill exists.
- (Yarn?) `ModelLoader` moved to `ModelBaker`, multiple internals renammed.
