package fr.estecka.variantscit.modules;

import fr.estecka.variantscit.api.ICitModule;
import fr.estecka.variantscit.api.IVariantManager;
import java.util.Map;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class EnchantedBookModule
implements ICitModule
{
	private final ModelIdentifier citMulti;

	public EnchantedBookModule(Map<String,ModelIdentifier> models){
		citMulti = models.get("multi");
	}

	@Override
	public Identifier GetItemVariant(ItemStack stack){
		ItemEnchantmentsComponent enchants = stack.get(DataComponentTypes.STORED_ENCHANTMENTS);
		if (enchants != null && enchants.getSize() == 1)
			return enchants.getEnchantments().iterator().next().getKey().get().getValue();
		else
			return null;
	}

	@Override
	public ModelIdentifier GetItemModel(ItemStack stack, IVariantManager variant){
		ItemEnchantmentsComponent enchants = stack.get(DataComponentTypes.STORED_ENCHANTMENTS);
		if (enchants != null && enchants.getSize() > 1)
			return citMulti;
		else
			return variant.GetModelVariantForItem(stack);
	}
}
