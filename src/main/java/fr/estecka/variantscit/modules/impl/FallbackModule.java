package fr.estecka.variantscit.modules.impl;

import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.modules.libraries.IVariantCitModule;
import fr.estecka.variantscit.modules.libraries.IVariantLibrary;
import fr.estecka.variantscit.commands.CommandLogger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record FallbackModule(IVariantCitModule... innerQueue)
implements IVariantCitModule
{
	@Override
	public @Nullable ResourceLocation GetItemModel(ItemStack stack, IVariantLibrary modelProvider) {
		for (IVariantCitModule m : innerQueue){
			ResourceLocation result = m.GetItemModel(stack, modelProvider);
			if (result != null) return result;
		}

		return null;
	}

	@Override
	public @Nullable ResourceLocation Walkthrough(ItemStack stack, IVariantLibrary library, CommandLogger logger) {
		for (IVariantCitModule m : innerQueue){
			ResourceLocation result = m.Walkthrough(stack, library, logger);
			if (result != null) return result;
		}

		return null;
	}
}
