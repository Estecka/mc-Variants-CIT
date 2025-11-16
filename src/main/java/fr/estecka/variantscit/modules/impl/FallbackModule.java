package fr.estecka.variantscit.modules.impl;

import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.api.ICitModule;
import fr.estecka.variantscit.api.IVariantManager;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.ItemStack;

public record FallbackModule(ICitModule... innerQueue)
implements ICitModule
{
	@Override
	public @Nullable ModelIdentifier GetItemModel(ItemStack stack, IVariantManager modelProvider) {
		for (ICitModule m : innerQueue){
			ModelIdentifier result = m.GetItemModel(stack, modelProvider);
			if (result != null) return result;
		}

		return null;
	}
}
