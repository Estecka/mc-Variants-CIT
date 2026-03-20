package fr.estecka.variantscit.modules.libraries;

import org.jetbrains.annotations.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public interface IVariantCitModule
extends IGenericCitModule<IVariantLibrary>
{
	/**
	 * The main logic for changing an items model.
	 * 
	 * @param stack The item stack to evaluate the model for.
	 * @param modelProvider  The provider  for both  special  and  variant-based
	 * models.
	 * @return The model ID, or null if the vanilla model should be used.
	 */
	public abstract @Nullable ResourceLocation GetItemModel(ItemStack stack, IVariantLibrary modelProvider);
}
