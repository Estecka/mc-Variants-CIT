package fr.estecka.variantscit.modules;

import java.util.Iterator;
import fr.estecka.variantscit.api.ISimpleCitModule;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class EnchantedToolModule
implements ISimpleCitModule
{
	@Override
	public Identifier GetItemVariant(ItemStack stack){
		ItemEnchantmentsComponent enchants = stack.get(DataComponentTypes.ENCHANTMENTS);

		if (enchants == null || enchants.isEmpty())
			return null;

		Iterator<Entry<RegistryEntry<Enchantment>>> iterator = enchants.getEnchantmentEntries().iterator();
		var bestFit = iterator.next();
		while (iterator.hasNext()){
			var contestant = iterator.next();
			if (Compare(contestant, bestFit) > 0)
				bestFit = contestant;
		}

		return bestFit.getKey().getKey().get().getValue();
	}

	public int Compare(Entry<RegistryEntry<Enchantment>> a, Entry<RegistryEntry<Enchantment>> b){
		int result = 0;

		result = a.getKey().value().exclusiveSet().size() - b.getKey().value().exclusiveSet().size();
		if (result != 0) return result;

		result = a.getIntValue() - b.getIntValue();
		if (result != 0) return result;

		return result;
	}
}
