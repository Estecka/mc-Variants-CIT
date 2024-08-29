package fr.estecka.variantscit.modules;

import fr.estecka.variantscit.api.ICitModule;
import fr.estecka.variantscit.api.IVariantManager;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.ItemStack;

public class EnchantedBookModule
implements ICitModule
{
	@Override
	public ModelIdentifier GetItemModel(ItemStack stack, IVariantManager modelProvider){
		ItemEnchantmentsComponent enchants = stack.get(DataComponentTypes.STORED_ENCHANTMENTS);

		if (enchants == null || enchants.isEmpty())
			return null;
		else if (enchants.getSize() == 1)
			return modelProvider.GetVariantModel(enchants.getEnchantments().iterator().next().getKey().get().getValue());
		else
			return modelProvider.GetSpecialModel("multi");

	}
}
