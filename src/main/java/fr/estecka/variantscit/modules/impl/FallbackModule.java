package fr.estecka.variantscit.modules.impl;

import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.api.ICitModule;
import fr.estecka.variantscit.api.IVariantManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public record FallbackModule(ICitModule... innerQueue)
implements ICitModule
{
	@Override
	public @Nullable Identifier GetItemModel(ItemStack stack, IVariantManager modelProvider) {
		for (ICitModule m : innerQueue){
			Identifier result = m.GetItemModel(stack, modelProvider);
			if (result != null) return result;
		}

		return null;
	}
}
