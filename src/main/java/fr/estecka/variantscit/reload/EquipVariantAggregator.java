package fr.estecka.variantscit.reload;

import java.util.HashMap;
import java.util.Map;
import fr.estecka.variantscit.VariantLibrary;
import net.minecraft.util.Identifier;

public class EquipVariantAggregator
extends AVariantAggregator
{
	@Override
	public VariantLibrary CreateLibrary(ModuleDefinition definition, VCitResourceManager manager){
		Map<Identifier,Identifier> allVariants = new HashMap<>();
		Map<String,Identifier> allSpecials = new HashMap<>();

		final String prefix = definition.modelPrefix();
		final var specials = new HashMap<>(definition.specialModels());
		definition.fallbackModel().ifPresent(fallback -> specials.put(null, fallback));

		// Variants from equipments
		{
			var varItems  = FindVariants(manager.equipments, prefix);
			var speItems  = FindSpecials(manager.equipments, specials);
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
