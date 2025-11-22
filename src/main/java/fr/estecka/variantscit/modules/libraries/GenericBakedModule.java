package fr.estecka.variantscit.modules.libraries;

import fr.estecka.variantscit.modules.IBakedModule;
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
