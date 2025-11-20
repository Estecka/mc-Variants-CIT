package fr.estecka.variantscit.modules;

import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.api.ICitModule;
import fr.estecka.variantscit.api.IVariantManager;
import fr.estecka.variantscit.commands.CommandLogger;
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

	@Override
	public @Nullable Identifier Walkthrough(ItemStack stack, IVariantManager library, CommandLogger logger) {
		for (ICitModule m : innerQueue){
			Identifier result = m.Walkthrough(stack, library, logger);
			if (result != null) return result;
		}

		return null;
	}
}
