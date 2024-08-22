package fr.estecka.variantscit.modules.enchanted_book;

import fr.estecka.variantscit.api.ICitModule;
import fr.estecka.variantscit.api.IVariantModelProvider;
import java.util.Map;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class EnchantedBookModule
implements ICitModule
{
	private Identifier citMulti;

	@Override
	public void SetSpecialModels(Map<String,Identifier> models){
		citMulti = models.get("multi");
	}

	@Override
	public Identifier[] GetSpecialModels(){
		return new Identifier[]{ citMulti };
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
	public Identifier GetModelForItem(ItemStack stack, IVariantModelProvider variant){
		ItemEnchantmentsComponent enchants = stack.get(DataComponentTypes.STORED_ENCHANTMENTS);
		if (enchants != null && enchants.getSize() > 1)
			return citMulti;
		else
			return variant.GetModelVariantForItem(stack);
	}
}
