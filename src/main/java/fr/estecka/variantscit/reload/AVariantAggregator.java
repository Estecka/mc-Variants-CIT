package fr.estecka.variantscit.reload;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import fr.estecka.variantscit.VariantLibrary;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public abstract class AVariantAggregator
{
	public abstract VariantLibrary CreateLibrary(ModuleDefinition definition, ResourceManager manager);

	/**
	 * Finds all resources of a given type, whose id start with the given prefix.
	 * @param rootDirectory The type of the resources to look for.
	 * @return Maps the variant ID to its corresponding model ID
	 */
	static protected Map<Identifier,Identifier> FindVariants(ResourceManager manager, String rootDirectory, String modelPrefix, String suffix){
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
	static protected HashMap<String,Identifier> FindSpecials(ResourceManager manager, String rootDirectory, Map<String,Identifier> requested, String suffix){
		Set<Identifier> valid = new HashSet<>();

		// ResourceId to ModelId
		Map<Identifier, Identifier> resourceIds = requested.values().stream().collect(Collectors.toMap(
			id -> id.withPrefixedPath(rootDirectory+'/').withSuffixedPath(suffix),
			id -> id
		));
		for (Identifier fileId : manager.findResources(rootDirectory, id->resourceIds.keySet().contains(id)).keySet())
			valid.add(resourceIds.get(fileId));

		var result = new HashMap<>(requested);
		result.entrySet().removeIf(e -> !valid.contains(e.getValue()));
		return result;
	}

}
