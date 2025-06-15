package fr.estecka.variantscit.reload;

import java.util.HashMap;
import java.util.Map;
import fr.estecka.variantscit.VariantLibrary;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class EquipVariantAggregator
extends AVariantAggregator
{
	@Override
	public VariantLibrary CreateLibrary(ModuleDefinition definition, ResourceManager manager){
		Map<Identifier,Identifier> allVariants = new HashMap<>();
		Map<String,Identifier> allSpecials = new HashMap<>();

		final String prefix = definition.modelPrefix();
		final var specials = new HashMap<>(definition.specialModels());
		definition.fallbackModel().ifPresent(fallback -> specials.put(null, fallback));

		// Variants from items
		{
			var varItems  = FindVariants(manager, "equipment", prefix, ".json");
			var speItems  = FindSpecials(manager, "equipment", specials, ".json");
			allVariants.putAll(varItems);
			allSpecials.putAll(speItems);
		}

		allSpecials.remove(null);
		return new VariantLibrary(
			definition.fallbackModel().orElse(null),
			allVariants,
			allSpecials
		);
	}

}
