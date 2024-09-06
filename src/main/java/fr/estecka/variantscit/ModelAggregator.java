package fr.estecka.variantscit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import fr.estecka.variantscit.ModuleLoader.ProtoModule;
import net.fabricmc.fabric.impl.client.model.loading.ModelLoadingConstants;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class ModelAggregator
{
	public final Set<ModelIdentifier> modelsToLoad = new HashSet<>();
	public final Map<ModelIdentifier, Identifier> modelsToCreate = new HashMap<>();

	public VariantLibrary CreateLibrary(ProtoModule prototype, ResourceManager manager){
		Map<Identifier,ModelIdentifier> allVariants = new HashMap<>();
		Map<String,ModelIdentifier> allSpecials = new HashMap<>();

		final String prefix = prototype.definition().modelPrefix();
		final Optional<Identifier> modelParent = prototype.definition().modelParent();
		final var specials = prototype.definition().specialModels();

		var varModels = FindVariants(manager, "models", prefix, ".json");
		var speModels = FindSpecials(manager, "models", specials, ".json");
		allVariants.putAll(varModels);
		allSpecials.putAll(speModels);
		this.modelsToLoad.addAll(varModels.values());
		this.modelsToLoad.addAll(speModels.values());

		if (modelParent.isPresent())
		{
			var varTextures = FindVariants(manager, "textures", prefix, ".png" );
			var speTextures = FindSpecials(manager, "textures", specials, ".png" );
			varModels.keySet().forEach(varTextures::remove);
			speModels.keySet().forEach(speTextures::remove);
			allVariants.putAll(varTextures);
			allSpecials.putAll(speTextures);
			varTextures.values().forEach(model -> AddModelToCreate(model, modelParent.get()));
			speTextures.values().forEach(model -> AddModelToCreate(model, modelParent.get()));
		}

		return new VariantLibrary(
			prototype.definition().GetFallbackModelId(),
			allVariants,
			allSpecials
		);
	}

	private void AddModelToCreate(ModelIdentifier model, Identifier parent){
		if  (!this.modelsToCreate.containsKey(model))
			modelsToCreate.put(model, parent);
		else if (!modelsToCreate.get(model).equals(parent))
			VariantsCitMod.LOGGER.error("Conflicting definitions for model {}", model.id());
	}

	/**
	 * Finds all models/textures for a given prefix.
	 * @return Maps the variant ID to its corresponding model ID
	 */
	private Map<Identifier,ModelIdentifier> FindVariants(ResourceManager manager, String rootDirectory, String modelPrefix, String suffix){
		Map<Identifier, ModelIdentifier> results = new HashMap<>();

		String fullPrefix = rootDirectory+'/'+modelPrefix;
		String directory = fullPrefix.substring(0, fullPrefix.lastIndexOf('/'));
		for (Identifier fileId : manager.findResources(directory, id -> id.getPath().startsWith(fullPrefix) && id.getPath().endsWith(suffix)).keySet())
		{
			String namespace = fileId.getNamespace();
			String modelName, variantName;
			modelName = fileId.getPath();
			modelName = modelName.substring((rootDirectory+'/').length(), modelName.length()-suffix.length());
			variantName = modelName.substring(modelPrefix.length());

			results.put(
				Identifier.of(namespace, variantName),
				ModelLoadingConstants.toResourceModelId(Identifier.of(namespace, modelName))
			);
		}

		return results;
	}

	/**
	 * Finds which of the requested model/texture IDs are actually available.
	 * @return The model/texture IDs
	 */
	private Map<String,ModelIdentifier> FindSpecials(ResourceManager manager, String rootDirectory, Map<String,Identifier> requested, String suffix){
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
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				e-> ModelLoadingConstants.toResourceModelId(e.getValue())
			))
			;
	}

}
