package fr.estecka.variantscit.reload;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.ResourceManager;
import fr.estecka.variantscit.modules.libraries.IVariantLibrary;
import fr.estecka.variantscit.modules.libraries.VariantLibrary;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.assetgen.EAssetGenPass;
import fr.estecka.variantscit.assetgen.GeneratedResourcePack;
import fr.estecka.variantscit.assetgen.GeneratorPresets;
import fr.estecka.variantscit.assetgen.HotswappableResourceManager;
import fr.estecka.variantscit.assetgen.IAssetGenerator;
import fr.estecka.variantscit.collections.HashBiMap;
import fr.estecka.variantscit.collections.IBiMap;
import fr.estecka.variantscit.collections.NestedMaps;

public class VariantAggregator
{
	static public record GeneratedAsset(
		IoSupplier<InputStream> resource,
		int priority
	){}

	private final Map<ModuleDefinition, Identifier> moduleIds = new IdentityHashMap<>();
	private final Map<ModuleDefinition, IAssetGenerator> assetGenerators = new IdentityHashMap<>();
	private final IBiMap<EModuleHook, ModuleDefinition, VariantLibrary> variantLibraries = NestedMaps.Create(HashBiMap::new, IdentityHashMap::new);

	public final Map<Identifier, GeneratedAsset> generatedAssets = new HashMap<>();
	public final Set<String> conflictingModelPrefixes = new HashSet<>();


	public VariantAggregator(Map<Identifier, ModuleDefinition> modules){
		for (var entry : modules.entrySet()){
			ModuleDefinition module = entry.getValue();
			this.moduleIds.put(module, entry.getKey());
			for (EModuleHook hook : module.hooks())
				this.variantLibraries.put(hook, module, InitialLibrary(module));

			this.assetGenerators.put(module, module.assetGen().orElse(GeneratorPresets.LegacyGenerator(module)));
		}
	}

	static private VariantLibrary InitialLibrary(ModuleDefinition module) {
		var fallbackModel = module.libraryDefinition().hardcodedList().get(IVariantLibrary.FALLBACK_VARIANT_ID);
		return new VariantLibrary(fallbackModel);
	}

	public Optional<VariantLibrary> GetLibrary(EModuleHook hook, ModuleDefinition module){
		return Optional.ofNullable(variantLibraries.get(hook, module));
	}

	public void GatherAll(HotswappableResourceManager manager){
		var genPack = GeneratedResourcePack.INSTANCE.Reset();

		// Asset generation passes
		GatherType(EAssetType.TRIM_TEXTURE,  manager.Get());
		GatherType(EAssetType.EQUIP_TEXTURE, manager.Get());
		GatherType(EAssetType.ITEM_TEXTURE,  manager.Get());
		UpdateGeneratedPack(genPack, manager);
		GatherType(EAssetType.BAKED_MODEL,   manager.Get());
		UpdateGeneratedPack(genPack, manager);

		// Populate variant libraries
		GatherType(EAssetType.TRIM_MODEL,    manager.Get());
		GatherType(EAssetType.ITEM_STATE,    manager.Get());
		GatherType(EAssetType.EQUIPMENT,     manager.Get());
	}

	private void GatherType(EAssetType assetType, ResourceManager manager){
		Set<Identifier> resources = manager.listResources(assetType.packDirectory, id->id.getPath().endsWith(assetType.suffix)).keySet();

		Stream<Identifier> modelIds = resources.stream().map(id->assetType.GetModelId(id).get());
		GatherIds(assetType, modelIds);
	}

	private void GatherIds(EAssetType assetType, Stream<Identifier> assets){
		assets.forEach(modelId -> ApplyModelToAll(assetType, modelId));
	}

	private void ApplyModelToAll(EAssetType assetType, Identifier modelId){
		EAssetGenPass generatorPass = switch (assetType){
			default -> null;
			case EAssetType.TRIM_TEXTURE  -> EAssetGenPass.TRIMS;
			case EAssetType.EQUIP_TEXTURE -> EAssetGenPass.EQUIPMENTS;
			case EAssetType.ITEM_TEXTURE  -> EAssetGenPass.BAKED_MODELS;
			case EAssetType.BAKED_MODEL   -> EAssetGenPass.ITEM_STATES;
		};

		for (var entry : variantLibraries.initIfAbsent(assetType.hook).entrySet())
		{
			ModuleDefinition module = entry.getKey();
			VariantLibrary library = entry.getValue();
			VariantsCitMod.LOGGER.PushLabel(moduleIds.get(module));

			this.ApplyModelToModule(assetType.isFundamental, module, library, modelId);

			if (generatorPass != null){
				IAssetGenerator generator = this.assetGenerators.get(module);
				IAssetGenerator.Result generatedResources = generator.AcceptAsset(generatorPass, modelId);

				for (var e : generatedResources.entrySet())
				if  (this.ApplyModelToModule(false, module, library, e.getValue().radical()))
				{
					Identifier resourceId = generatorPass.GetOutputResourceId(e.getKey());
					this.OnGeneratedResource(resourceId, module.libraryDefinition(), e.getValue().resource());
				}
			}

			VariantsCitMod.LOGGER.PopLabel();
		}
	}

	/**
	 * @return Whether the provided ID asset can be added to this library.
	 * @param isFundamental If true, add the asset to the library on success.
	 */
	private boolean ApplyModelToModule(boolean isFundamental, ModuleDefinition module, VariantLibrary library, Identifier modelId){
		boolean accepted = false;

		Set<Identifier> variants = module.libraryDefinition().GetVariantIds(modelId);
		for (Identifier variantId : variants)
		if  (module.parameters().AcceptsVariant(variantId) || variantId.getNamespace().equals(VariantsCitMod.MODID))
		{
			accepted = true;
			if (isFundamental)
				library.variantModels().put(variantId, modelId);
		}

		return accepted;
	}

	private void UpdateGeneratedPack(Map<Identifier, IoSupplier<InputStream>> pack, HotswappableResourceManager manager){
		for (var entry : this.generatedAssets.entrySet())
			pack.put(entry.getKey(), entry.getValue().resource);

		manager.Refresh();
	}

	private void OnGeneratedResource(Identifier resourceId, LibraryDefinition libDefinition, IoSupplier<InputStream> resource){
		int priority = libDefinition.modelPrefix().map(String::length).orElse(0);
		GeneratedAsset oldAsset = this.generatedAssets.get(resourceId);

		if (oldAsset == null || oldAsset.priority < priority)
		{
			this.generatedAssets.put(resourceId, new GeneratedAsset(resource, priority));
			// VariantsCitMod.LOGGER.warn("Generated asset: {}", resourceId);
		}
		// // FIXME: Fix false positives
		// else if (oldAsset != null && oldAsset.priority == priority && !oldAsset.resource.equals(resource))
		// 	this.conflictingModelPrefixes.add(modelPrefix);
	}
}
