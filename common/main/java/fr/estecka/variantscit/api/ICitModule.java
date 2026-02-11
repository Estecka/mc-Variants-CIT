package fr.estecka.variantscit.api;

import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.modules.libraries.IGenericCitModule;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public interface ICitModule
extends IGenericCitModule<IVariantManager>
{
	/**
	 * The main logic for changing an items model.
	 * 
	 * @param stack The item stack to evaluate the model for.
	 * @param modelProvider  The provider  for both  special  and  variant-based
	 * models.
	 * @return The model ID, or null if the vanilla model should be used.
	 */
	public abstract @Nullable Identifier GetItemModel(ItemStack stack, IVariantManager modelProvider);
}
