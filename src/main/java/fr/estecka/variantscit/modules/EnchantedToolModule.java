package fr.estecka.variantscit.modules;

import java.util.Iterator;
import fr.estecka.variantscit.api.IVariantManager;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.entry.RegistryEntry;

public class EnchantedToolModule
extends AComponentCachingModule<ItemEnchantmentsComponent>
{
	public EnchantedToolModule(){
		super(DataComponentTypes.ENCHANTMENTS);
	}

	@Override
	public ModelIdentifier GetModelForComponent(ItemEnchantmentsComponent enchants, IVariantManager models){
		if (enchants == null || enchants.isEmpty())
			return null;

		Iterator<Entry<RegistryEntry<Enchantment>>> iterator = enchants.getEnchantmentEntries().iterator();
		var bestFit = iterator.next();
		while (iterator.hasNext()){
			var contestant = iterator.next();
			if (Compare(contestant, bestFit, models) > 0)
				bestFit = contestant;
		}

		return models.GetVariantModel(bestFit.getKey().getKey().get().getValue());
	}

	public int Compare(Entry<RegistryEntry<Enchantment>> a, Entry<RegistryEntry<Enchantment>> b, IVariantManager models){
		int result = 0;

		result = Boolean.compare(
			models.HasVariantModel(a.getKey().getKey().get().getValue()),
			models.HasVariantModel(b.getKey().getKey().get().getValue())
		);
		if (result != 0) return result;

		result = a.getKey().value().exclusiveSet().size() - b.getKey().value().exclusiveSet().size();
		if (result != 0) return result;

		result = a.getIntValue() - b.getIntValue();
		if (result != 0) return result;

		return result;
	}
}
