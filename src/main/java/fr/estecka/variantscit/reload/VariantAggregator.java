package fr.estecka.variantscit.reload;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import fr.estecka.variantscit.modules.libraries.VariantLibrary;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.assetgen.EAssetGenPass;
import fr.estecka.variantscit.assetgen.GeneratedResourcePack;
import fr.estecka.variantscit.assetgen.GeneratorsRegistry;
import fr.estecka.variantscit.assetgen.IAssetGenerator;
import fr.estecka.variantscit.mixin.BakedModelManagerMixin;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

/**
 * @implNote Texture and Baked Model ids usually start with `item/` however here
 * this prefix is stripped off so that their ids match those of the item states.
 * These `item/` need to be re-added in {@link BakedModelManagerMixin} for asset
 * generation.
 */
public class VariantAggregator
{
	static public record GeneratedAsset(
		InputSupplier<InputStream> resource,
		int priority
	){}

	private final Map<ModuleDefinition, Identifier> moduleIds = new IdentityHashMap<>();
	private final Map<ModuleDefinition, IAssetGenerator> assetGenerators = new IdentityHashMap<>();
	private final Map<ModuleDefinition, VariantLibrary> item_model = new IdentityHashMap<>();
	private final Map<ModuleDefinition, VariantLibrary> equippable = new IdentityHashMap<>();

	public final Map<Identifier, GeneratedAsset> generatedAssets = new HashMap<>();
	public final Set<String> conflictingModelPrefixes = new HashSet<>();


	public VariantAggregator(Map<Identifier, ModuleDefinition> modules){
		for (var entry : modules.entrySet()){
			ModuleDefinition module = entry.getValue();
			this.moduleIds.put(module, entry.getKey());
			for (EModuleContext context : module.contexts())
				GetLibraryMap(context).put(module, EmptyLibrary(module));

			this.assetGenerators.put(module, GeneratorsRegistry.LegacyGenerator(module));
		}
	}

	static private VariantLibrary EmptyLibrary(ModuleDefinition module) {
		return new VariantLibrary(
			module.fallbackModel().orElse(null),
			new HashMap<>(),
			module.specialModels()
		);
	}

	private Map<ModuleDefinition, VariantLibrary> GetLibraryMap(EModuleContext context){
		return switch (context){
			default -> throw new AssertionError("Invalid Context");
			case EQUIPPABLE -> this.equippable;
			case ITEM_MODEL -> this.item_model;
		};
	}

	public Optional<VariantLibrary> GetLibrary(EModuleContext context, ModuleDefinition module){
		return Optional.ofNullable(GetLibraryMap(context).get(module));
	}

	public void GatherAll(ResourceManager manager){
		var genPack = GeneratedResourcePack.INSTANCE.Reset();

		// Asset generation passes
		GatherType(EAssetType.TEXTURE,     manager);
		UpdateGeneratedPack(genPack);
		GatherType(EAssetType.BAKED_MODEL, manager);
		UpdateGeneratedPack(genPack);

		// Populate variant libraries
		GatherType(EAssetType.ITEM_STATE,  manager);
		GatherType(EAssetType.EQUIPMENT,   manager);
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
		EAssetGenPass generatorPass = switch (assetType){
			default -> null;
			case EAssetType.TEXTURE     -> EAssetGenPass.BAKED_MODELS;
			case EAssetType.BAKED_MODEL -> EAssetGenPass.ITEM_STATES;
		};

		for (var entry : GetLibraryMap(assetType.context).entrySet())
		{
			ModuleDefinition module = entry.getKey();
			VariantLibrary library = entry.getValue();
			VariantsCitMod.LOGGER.PushLabel(moduleIds.get(module));

			// TODO: EnchantmentVector and AxolotlVariant will refuse stuff like "_pulling_1" for bows
			boolean accepted = this.ApplyModelToModule(assetType.isFundamental, module, library, assetId);

			if (accepted && generatorPass != null){
				Identifier resourceId = generatorPass.GetOutputResourceId(assetId);
				IAssetGenerator generator = this.assetGenerators.get(module);
				InputSupplier<InputStream> resource = generator.AcceptAsset(generatorPass, assetId);

				if (resource != null)
					this.OnGeneratedResource(resourceId, module.modelPrefix(), resource);
			}

			VariantsCitMod.LOGGER.PopLabel();
		}
	}

	private boolean ApplyModelToModule(boolean isFundamental, ModuleDefinition module, VariantLibrary library, Identifier assetId){
		boolean accepted = false;

		if (assetId.equals(library.fallbackModel()))
			accepted = true;

		if (library.specialModels().containsValue(assetId))
			accepted = true;

		if (assetId.getPath().startsWith(module.modelPrefix())){
			Identifier variantId = Identifier.of(
				assetId.getNamespace(),
				assetId.getPath().substring(module.modelPrefix().length())
			);

			if (module.parameters().AcceptsVariant(variantId)){
				accepted = true;
				if (isFundamental)
					library.variantModels().put(variantId, assetId);
			}

		}

		return accepted;
	}

	private void UpdateGeneratedPack(Map<Identifier, InputSupplier<InputStream>> pack){
		for (var entry : this.generatedAssets.entrySet())
			pack.put(entry.getKey(), entry.getValue().resource);
	}

	private void OnGeneratedResource(Identifier resourceId, String modelPrefix, InputSupplier<InputStream> resource){
		int priority = modelPrefix.length();
		GeneratedAsset oldAsset = this.generatedAssets.get(resourceId);

		if (oldAsset == null || oldAsset.priority < priority)
			this.generatedAssets.put(resourceId, new GeneratedAsset(resource, priority));
		else if (oldAsset != null && oldAsset.priority == priority && !oldAsset.resource.equals(resource))
			this.conflictingModelPrefixes.add(modelPrefix);
	}
}
