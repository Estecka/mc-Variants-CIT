package fr.estecka.variantscit.reload;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import fr.estecka.variantscit.VariantLibrary;
import net.minecraft.util.Identifier;

public class ItemVariantAggregator
extends AVariantAggregator
{
	static public record ModelToCreate(
		Identifier parent,
		int specificity
	){}

	/**
	 * Maps each model ID to its parents.
	 */
	public final Map<Identifier, ModelToCreate> modelsToCreate = new HashMap<>();
	public final Set<Identifier> itemStatesToCreate = new HashSet<>();
	public final Set<String> conflictingModelPrefixes = new HashSet<>();

	@Override
	public VariantLibrary CreateLibrary(ModuleDefinition definition, VCitResourceManager manager){
		Map<Identifier,Identifier> allVariants = new HashMap<>();
		Map<String,Identifier> allSpecials = new HashMap<>();

		final String prefix = definition.modelPrefix();
		final Optional<Identifier> modelParent = definition.modelParent();
		final var specials = new HashMap<>(definition.specialModels());
		definition.fallbackModel().ifPresent(fallback -> specials.put(null, fallback));

		// Variants from items
		{
			var varItems  = FindVariants(manager.items, prefix);
			var speItems  = FindSpecials(manager.items, specials);
			allVariants.putAll(varItems);
			allSpecials.putAll(speItems);
		}

		// Variants from models
		if (definition.itemGen())
		{
			var varModels = FindVariants(manager.models, "item/"+prefix);
			var speModels = FindSpecials(manager.models, specials);
			allVariants.keySet().forEach(varModels::remove);
			allSpecials.keySet().forEach(speModels::remove);
			allVariants.putAll(varModels);
			allSpecials.putAll(speModels);
			this.itemStatesToCreate.addAll(varModels.values());
			this.itemStatesToCreate.addAll(speModels.values());
		}

		// Variants from textures
		if (modelParent.isPresent())
		{
			var varTextures = FindVariants(manager.textures, "item/"+prefix);
			var speTextures = FindSpecials(manager.textures, specials);
			allVariants.keySet().forEach(varTextures::remove);
			allSpecials.keySet().forEach(speTextures::remove);
			allVariants.putAll(varTextures);
			allSpecials.putAll(speTextures);
			this.itemStatesToCreate.addAll(varTextures.values());
			this.itemStatesToCreate.addAll(speTextures.values());
			varTextures.values().forEach(model -> AddModelToCreate(model, modelParent.get(), prefix));
			speTextures.values().forEach(model -> AddModelToCreate(model, modelParent.get(), prefix));
		}

		allSpecials.remove(null);
		return new VariantLibrary(
			definition.fallbackModel().orElse(null),
			allVariants,
			allSpecials
		);
	}

	private void AddModelToCreate(Identifier modelId, Identifier parent, String modelPrefix){
		int specificity = modelPrefix.length();
		var modelData = new ModelToCreate(parent, specificity);
		var old = this.modelsToCreate.get(modelId);

		if (old == null || old.specificity < modelData.specificity)
			modelsToCreate.put(modelId, modelData);
		else if (old.specificity == modelData.specificity)
			conflictingModelPrefixes.add(modelPrefix);
	}

}
