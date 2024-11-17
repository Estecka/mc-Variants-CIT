package fr.estecka.variantscit.reload;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import fr.estecka.variantscit.VariantLibrary;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.reload.ModuleLoader.ProtoModule;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class ModelAggregator
{
	/**
	 * Maps each model ID to its parents.
	 */
	public final Map<Identifier, Identifier> modelsToCreate = new HashMap<>();
	public final Set<Identifier> itemsToCreate = new HashSet<>();
	// public final Set<Identifier> modelsToLoad = new HashSet<>();

	public VariantLibrary CreateLibrary(ProtoModule prototype, ResourceManager manager){
		Map<Identifier,Identifier> allVariants = new HashMap<>();
		Map<String,Identifier> allSpecials = new HashMap<>();

		final String prefix = prototype.definition().modelPrefix();
		final Optional<Identifier> modelParent = prototype.definition().modelParent();
		final var specials = new HashMap<>(prototype.definition().specialModels());
		prototype.definition().fallbackModel().ifPresent(fallback -> specials.put(null, fallback));

		// Variants from items
		{
			var varItems  = FindVariants(manager, "items", prefix, ".json");
			var speItems  = FindSpecials(manager, "items", specials, ".json");
			allVariants.putAll(varItems);
			allSpecials.putAll(speItems);
		}

		// Variants from models
		if (prototype.definition().itemGen())
		{
			var varModels = FindVariants(manager, "models/item", prefix, ".json");
			var speModels = FindSpecials(manager, "models/item", specials, ".json");
			allVariants.keySet().forEach(varModels::remove);
			allSpecials.keySet().forEach(speModels::remove);
			allVariants.putAll(varModels);
			allSpecials.putAll(speModels);
			this.itemsToCreate.addAll(varModels.values());
			this.itemsToCreate.addAll(speModels.values());
		}

		// Variants from textures
		if (modelParent.isPresent())
		{
			var varTextures = FindVariants(manager, "textures/item", prefix, ".png");
			var speTextures = FindSpecials(manager, "textures/item", specials, ".png");
			allVariants.keySet().forEach(varTextures::remove);
			allSpecials.keySet().forEach(speTextures::remove);
			allVariants.putAll(varTextures);
			allSpecials.putAll(speTextures);
			this.itemsToCreate.addAll(varTextures.values());
			this.itemsToCreate.addAll(speTextures.values());
			varTextures.values().forEach(model -> AddModelToCreate(model, modelParent.get()));
			speTextures.values().forEach(model -> AddModelToCreate(model, modelParent.get()));
		}

		allSpecials.remove(null);
		return new VariantLibrary(
			prototype.definition().fallbackModel().orElse(null),
			allVariants,
			allSpecials
		);
	}

	private void AddModelToCreate(Identifier model, Identifier parent){
		if  (!this.modelsToCreate.containsKey(model))
			modelsToCreate.put(model, parent);
		else if (!modelsToCreate.get(model).equals(parent))
			VariantsCitMod.LOGGER.error("Conflicting definitions for model {}", model);
	}

	/**
	 * Finds all resources of a given type, whose id start with the given prefix.
	 * @param rootDirectory The type of the resources to look for.
	 * @return Maps the variant ID to its corresponding model ID
	 */
	private Map<Identifier,Identifier> FindVariants(ResourceManager manager, String rootDirectory, String modelPrefix, String suffix){
		Map<Identifier, Identifier> results = new HashMap<>();

		String fullPrefix = rootDirectory+'/'+modelPrefix;
		String directory = fullPrefix.substring(0, fullPrefix.lastIndexOf('/'));
		for (Identifier fileId : manager.findResources(directory, id -> id.getPath().startsWith(fullPrefix) && id.getPath().endsWith(suffix)).keySet())
		{
			String namespace = fileId.getNamespace();
			String assetName, variantName;
			assetName = fileId.getPath();
			assetName = assetName.substring((rootDirectory+'/').length(), assetName.length()-suffix.length());
			variantName = assetName.substring(modelPrefix.length());

			results.put(
				Identifier.of(namespace, variantName),
				Identifier.of(namespace, assetName)
			);
		}

		return results;
	}

	/**
	 * Finds which of the requested model/texture IDs are actually available.
	 * @return The model/texture IDs
	 */
	private Map<String,Identifier> FindSpecials(ResourceManager manager, String rootDirectory, Map<String,Identifier> requested, String suffix){
		Set<Identifier> valid = new HashSet<>();

		// ResourceId to ModelId
		Map<Identifier, Identifier> resourceIds = requested.values().stream().collect(Collectors.toMap(
			id -> id.withPrefixedPath(rootDirectory+'/').withSuffixedPath(suffix),
			id -> id
		));
		for (Identifier fileId : manager.findResources(rootDirectory, id->resourceIds.keySet().contains(id)).keySet())
			valid.add(resourceIds.get(fileId));

		return requested.entrySet().stream()
			.filter(e -> valid.contains(e.getValue()))
			// May be simplified
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				Map.Entry::getValue
			))
			;
	}

}
