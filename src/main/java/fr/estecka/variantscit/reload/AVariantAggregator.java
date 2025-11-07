package fr.estecka.variantscit.reload;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import fr.estecka.variantscit.VariantLibrary;
import net.minecraft.util.Identifier;

public abstract class AVariantAggregator
{
	public abstract VariantLibrary CreateLibrary(ModuleDefinition definition, VCitResourceManager manager);

	/**
	 * Finds all resources of a given type, whose id start with the given prefix.
	 * @param rootDirectory The type of the resources to look for.
	 * @return Maps the variant ID to its corresponding model ID
	 */
	static protected Map<Identifier,Identifier> FindVariants(List<Identifier> assets, String modelPrefix){
		Map<Identifier, Identifier> results = new HashMap<>();

		for (Identifier assetId : assets)
		if  (assetId.getPath().startsWith(modelPrefix))
		{
			results.put(
				assetId.withPath(path->path.substring(modelPrefix.length())),
				assetId
			);
		}

		return results;
	}

	/**
	 * Finds which of the requested model/texture IDs are actually available.
	 * @return The model/texture IDs
	 */
	static protected HashMap<String,Identifier> FindSpecials(List<Identifier> assets, Map<String,Identifier> requested){
		Set<Identifier> valid = new HashSet<>();

		for (Identifier assetId : assets)
		if  (requested.containsValue(assetId))
		{
			valid.add(assetId);
		}

		var result = new HashMap<>(requested);
		result.entrySet().removeIf(e -> !valid.contains(e.getValue()));
		return result;
	}

}
