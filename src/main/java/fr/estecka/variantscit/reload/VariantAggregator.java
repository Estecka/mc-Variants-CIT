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
import fr.estecka.variantscit.assetgen.GeneratorPresets;
import fr.estecka.variantscit.assetgen.HotswappableResourceManager;
import fr.estecka.variantscit.assetgen.IAssetGenerator;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

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

			this.assetGenerators.put(module, module.assetGen().orElse(GeneratorPresets.LegacyGenerator(module)));
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

	public void GatherAll(HotswappableResourceManager manager){
		var genPack = GeneratedResourcePack.INSTANCE.Reset();

		// Asset generation passes
		GatherType(EAssetType.EQUIP_TEXTURE, manager.Get());
		GatherType(EAssetType.ITEM_TEXTURE,  manager.Get());
		UpdateGeneratedPack(genPack, manager);
		GatherType(EAssetType.BAKED_MODEL,   manager.Get());
		UpdateGeneratedPack(genPack, manager);

		// Populate variant libraries
		GatherType(EAssetType.ITEM_STATE,    manager.Get());
		GatherType(EAssetType.EQUIPMENT,     manager.Get());
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
			case EAssetType.EQUIP_TEXTURE -> EAssetGenPass.EQUIPMENTS;
			case EAssetType.ITEM_TEXTURE  -> EAssetGenPass.BAKED_MODELS;
			case EAssetType.BAKED_MODEL   -> EAssetGenPass.ITEM_STATES;
		};

		for (var entry : GetLibraryMap(assetType.context).entrySet())
		{
			ModuleDefinition module = entry.getKey();
			VariantLibrary library = entry.getValue();
			VariantsCitMod.LOGGER.PushLabel(moduleIds.get(module));

			// TODO: EnchantmentVector and AxolotlVariant will refuse stuff like "_pulling_1" for bows
			boolean accepted = this.ApplyModelToModule(assetType.isFundamental, module, library, assetId);

			if (accepted && generatorPass != null){
				IAssetGenerator generator = this.assetGenerators.get(module);
				IAssetGenerator.Result generatedResources = generator.AcceptAsset(generatorPass, assetId);

				for (var r : generatedResources.entrySet()){
					Identifier resourceId = generatorPass.GetOutputResourceId(r.getKey());
					this.OnGeneratedResource(resourceId, module.modelPrefix(), r.getValue());
				}
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

	private void UpdateGeneratedPack(Map<Identifier, InputSupplier<InputStream>> pack, HotswappableResourceManager manager){
		for (var entry : this.generatedAssets.entrySet())
			pack.put(entry.getKey(), entry.getValue().resource);

		manager.Refresh();
	}

	private void OnGeneratedResource(Identifier resourceId, String modelPrefix, InputSupplier<InputStream> resource){
		int priority = modelPrefix.length();
		GeneratedAsset oldAsset = this.generatedAssets.get(resourceId);

		if (oldAsset == null || oldAsset.priority < priority)
		{
			this.generatedAssets.put(resourceId, new GeneratedAsset(resource, priority));
			// VariantsCitMod.LOGGER.warn("Generated asset: {}", resourceId);
		}
		else if (oldAsset != null && oldAsset.priority == priority && !oldAsset.resource.equals(resource))
			this.conflictingModelPrefixes.add(modelPrefix);
	}
}
