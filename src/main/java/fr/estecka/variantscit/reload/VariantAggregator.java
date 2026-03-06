package fr.estecka.variantscit.reload;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.ResourceManager;
import fr.estecka.variantscit.modules.libraries.VariantLibrary;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.assetgen.EAssetGenPass;
import fr.estecka.variantscit.assetgen.GeneratedResourcePack;
import fr.estecka.variantscit.assetgen.GeneratorPresets;
import fr.estecka.variantscit.assetgen.HotswappableResourceManager;
import fr.estecka.variantscit.assetgen.IAssetGenerator;

public class VariantAggregator
{
	static public record GeneratedAsset(
		IoSupplier<InputStream> resource,
		int priority
	){}

	private final Map<ModuleDefinition, ResourceLocation> moduleIds = new IdentityHashMap<>();
	private final Map<ModuleDefinition, IAssetGenerator> assetGenerators = new IdentityHashMap<>();
	private final Map<ModuleDefinition, VariantLibrary> item_model = new IdentityHashMap<>();
	private final Map<ModuleDefinition, VariantLibrary> equippable = new IdentityHashMap<>();

	public final Map<ResourceLocation, GeneratedAsset> generatedAssets = new HashMap<>();
	public final Set<String> conflictingModelPrefixes = new HashSet<>();


	public VariantAggregator(Map<ResourceLocation, ModuleDefinition> modules){
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
		Set<ResourceLocation> resources = manager.listResources(assetType.directory, id->id.getPath().endsWith(assetType.suffix)).keySet();

		Stream<ResourceLocation> shortIds = resources.stream().map(id->assetType.GetShortId(id).get());
		GatherIds(assetType, shortIds);
	}

	private void GatherIds(EAssetType assetType, Stream<ResourceLocation> assets){
		assets.forEach(shortId -> ApplyModelToAll(assetType, shortId));
	}

	private void ApplyModelToAll(EAssetType assetType, ResourceLocation shortId){
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

			this.ApplyModelToModule(assetType.isFundamental, module, library, shortId);

			if (generatorPass != null){
				IAssetGenerator generator = this.assetGenerators.get(module);
				IAssetGenerator.Result generatedResources = generator.AcceptAsset(generatorPass, shortId);

				for (var e : generatedResources.entrySet())
				if  (this.ApplyModelToModule(false, module, library, e.getValue().radical()))
				{
					ResourceLocation resourceId = generatorPass.GetOutputResourceId(e.getKey());
					this.OnGeneratedResource(resourceId, module.modelPrefix(), e.getValue().resource());
				}
			}

			VariantsCitMod.LOGGER.PopLabel();
		}
	}

	/**
	 * @return Whether the provided ID asset can be added to this library.
	 * @param isFundamental If true, add the asset to the library on success.
	 */
	private boolean ApplyModelToModule(boolean isFundamental, ModuleDefinition module, VariantLibrary library, ResourceLocation shortId){
		boolean accepted = false;

		if (shortId.equals(library.fallbackModel()))
			accepted = true;

		if (library.specialModels().containsValue(shortId))
			accepted = true;

		if (shortId.getPath().startsWith(module.modelPrefix())){
			ResourceLocation variantId = ResourceLocation.fromNamespaceAndPath(
				shortId.getNamespace(),
				shortId.getPath().substring(module.modelPrefix().length())
			);

			if (module.parameters().AcceptsVariant(variantId)){
				accepted = true;
				if (isFundamental)
					library.variantModels().put(variantId, shortId);
			}

		}

		return accepted;
	}

	private void UpdateGeneratedPack(Map<ResourceLocation, IoSupplier<InputStream>> pack, HotswappableResourceManager manager){
		for (var entry : this.generatedAssets.entrySet())
			pack.put(entry.getKey(), entry.getValue().resource);

		manager.Refresh();
	}

	private void OnGeneratedResource(ResourceLocation resourceId, String modelPrefix, IoSupplier<InputStream> resource){
		int priority = modelPrefix.length();
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
