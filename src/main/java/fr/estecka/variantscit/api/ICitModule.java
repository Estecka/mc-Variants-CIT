package fr.estecka.variantscit.api;

import org.jetbrains.annotations.Nullable;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public interface ICitModule
{
	/**
	 * @deprecated Will  become  exclusive  to {@link ISimpleCitModule}. You may
	 * now implement that logic directly inside {@link #GetItemModel}.
	 */
	@Deprecated
	public default @Nullable Identifier GetItemVariant(ItemStack stack){
		return null;
	}

	/**
	 * The main logic for changing an items model.
	 * 
	 * @param stack The item stack to evaluate the model for.
	 * @param modelProvider  The provider  for both  special  and  variant-based
	 * models.
	 * @return The model ID, or null if the vanilla model should be used.
	 */
	public abstract @Nullable ModelIdentifier GetItemModel(ItemStack stack, IVariantManager modelProvider);
}
