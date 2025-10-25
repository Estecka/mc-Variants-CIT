package fr.estecka.variantscit.modulebakers;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public record GenericBakedModule<L>(L library, IGenericCitModule<L> logic)
implements IBakedModule
{
	@Override
	public Identifier GetModelForItem(ItemStack stack) {
		return logic.GetItemModel(stack, library);
	}
}
