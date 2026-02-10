package fr.estecka.variantscit.modules.impl;

import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.api.ICitModule;
import fr.estecka.variantscit.api.IVariantManager;
import fr.estecka.variantscit.commands.CommandLogger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record FallbackModule(ICitModule... innerQueue)
implements ICitModule
{
	@Override
	public @Nullable ResourceLocation GetItemModel(ItemStack stack, IVariantManager modelProvider) {
		for (ICitModule m : innerQueue){
			ResourceLocation result = m.GetItemModel(stack, modelProvider);
			if (result != null) return result;
		}

		return null;
	}

	@Override
	public @Nullable ResourceLocation Walkthrough(ItemStack stack, IVariantManager library, CommandLogger logger) {
		for (ICitModule m : innerQueue){
			ResourceLocation result = m.Walkthrough(stack, library, logger);
			if (result != null) return result;
		}

		return null;
	}
}
