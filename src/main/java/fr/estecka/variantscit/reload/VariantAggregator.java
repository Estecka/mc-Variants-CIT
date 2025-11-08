package fr.estecka.variantscit.reload;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import fr.estecka.variantscit.VariantLibrary;
import fr.estecka.variantscit.mixin.BakedModelManagerMixin;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

/**
 * @implNote Texture and Baked model ids usually start with `item/` however here
 * this prefix is stripped off so that their ids match those of the item states.
 * These `item/` need to be re-added in {@link BakedModelManagerMixin} for asset
 * generation.
 */
public class VariantAggregator
{
	static public record ModelToCreate(
		Identifier parent,
		int priority
	){}

	public final Map<ModuleDefinition, VariantLibrary> item_model = new IdentityHashMap<>();
	public final Map<ModuleDefinition, VariantLibrary> equippable = new IdentityHashMap<>();

	// item_model assetgen
	private final Set<Identifier> acceptedItemModels = new HashSet<>();
	public final Map<Identifier, ModelToCreate> modelsToCreate = new HashMap<>();
	public final Set<Identifier> itemStatesToCreate = new HashSet<>();
	public final Set<String> conflictingModelPrefixes = new HashSet<>();


	public VariantAggregator(Collection<ModuleDefinition> modules){

		for (ModuleDefinition module : modules)
		for (EModuleContext context : module.contexts())
		{
			switch (context) {
				case ITEM_MODEL: this.item_model.put(module, EmptyLibrary(module)); break;
				case EQUIPPABLE: this.equippable.put(module, EmptyLibrary(module)); break;
			}
		}
	}

	static private VariantLibrary EmptyLibrary(ModuleDefinition module) {
		return new VariantLibrary(
			module.fallbackModel().orElse(null),
			new HashMap<>(),
			module.specialModels()
		);
	}

	public Optional<VariantLibrary> GetLibrary(EModuleContext context, ModuleDefinition module){
		return Optional.ofNullable(switch (context) {
			default -> throw new AssertionError();
			case ITEM_MODEL -> this.item_model.get(module);
			case EQUIPPABLE -> this.equippable.get(module);
		});
	}

	public void GatherAll(ResourceManager manager){
		GatherType(EAssetType.ITEM_STATE,  manager);
		GatherType(EAssetType.BAKED_MODEL, manager);
		GatherType(EAssetType.TEXTURE,     manager);
		GatherType(EAssetType.EQUIPMENT,   manager);

		// Share generated assets accross modules
		GatherIds(EAssetType.BAKED_MODEL, this.modelsToCreate.keySet().stream());
		GatherIds(EAssetType.ITEM_STATE,  this.itemStatesToCreate.stream());
	}

	private void GatherType(EAssetType assetType, ResourceManager manager){
		Set<Identifier> resources = manager.findResources(assetType.directory, id->id.getPath().endsWith(assetType.suffix)).keySet();

		Stream<Identifier> ids = resources.stream().map(
			id->id.withPath(path->path.substring(
				assetType.directory.length() + 1,
				path.length() - assetType.suffix.length()
			))
		);

		GatherIds(assetType, ids);
	}

	private void GatherIds(EAssetType assetType, Stream<Identifier> assets){
		assets.forEach(assetId -> ApplyModelToAll(assetType, assetId));
	}

	private void ApplyModelToAll(EAssetType assetType, Identifier assetId){
		var module2Lib = switch(assetType.context){
			default -> throw new AssertionError();
			case ITEM_MODEL -> this.item_model;
			case EQUIPPABLE -> this.equippable;
		};

		for (var entry : module2Lib.entrySet())
		if  (IsTypeAcceptable(assetType, entry.getKey()))
		{
			ModuleDefinition module = entry.getKey();
			VariantLibrary library = entry.getValue();

			boolean accepted = this.ApplyModelToModule(module, library, assetId);
			if (accepted && assetType.context == EModuleContext.ITEM_MODEL) {
				switch (assetType) {
					default: /* no-op */;
					break;

					// Fallthrough
					case TEXTURE:     OnAcceptedTexture   (module, assetId);
					case BAKED_MODEL: OnAcceptedBakedModel(module, assetId);
					break;
				}

				this.acceptedItemModels.add(assetId);
			} 
		}
	}

	static private boolean IsTypeAcceptable(EAssetType type, ModuleDefinition module){
		switch (type) {
			default:          return true;
			case BAKED_MODEL: return module.itemGen();
			case TEXTURE:     return module.itemGen() && module.modelParent().isPresent();
		}
	}

	private boolean ApplyModelToModule(ModuleDefinition module, VariantLibrary library, Identifier modelId){
		boolean accepted = false;

		if (modelId.equals(library.fallbackModel()))
			accepted = true;

		if (library.specialModels().containsValue(modelId))
			accepted = true;

		if (modelId.getPath().startsWith(module.modelPrefix())){
			accepted = true;
			Identifier variantId = Identifier.of(
				modelId.getNamespace(),
				modelId.getPath().substring(module.modelPrefix().length())
			);
			library.variantModels().put(variantId, modelId);
		}

		return accepted;
	}

	private void OnAcceptedBakedModel(ModuleDefinition module, Identifier modelId){
		if (!this.acceptedItemModels.contains(modelId))
			this.itemStatesToCreate.add(modelId);
	}

	private void OnAcceptedTexture(ModuleDefinition module, Identifier modelId){
		int priority = module.modelPrefix().length();
		Identifier parent = module.modelParent().get();
		ModelToCreate oldModel = this.modelsToCreate.get(modelId);

		if ((oldModel != null && oldModel.priority < priority)
		|| (!this.acceptedItemModels.contains(modelId))
		){
			this.modelsToCreate.put(modelId, new ModelToCreate(parent, priority));
		}
		else if (oldModel != null && oldModel.priority == priority && oldModel.parent.equals(parent)){
			this.conflictingModelPrefixes.add(module.modelPrefix());
		}
	}
}
