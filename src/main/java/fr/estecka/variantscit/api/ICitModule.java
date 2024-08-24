package fr.estecka.variantscit.api;

import org.jetbrains.annotations.Nullable;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public interface ICitModule
{
	/**
	 * Identifies  the item's variant, from which the  model ID will be derived.
	 * Items with no variants will fallback to the vanilla model.
	 * 
	 * @return The variant's identifier, or null if the item has none.
	 */
	public abstract @Nullable Identifier GetItemVariant(ItemStack stack);

	/**
	 * Special logic  for overriding the variant-based models  in scenarios that
	 * are not handled by the mod. E.g: a unique model  for enchanted books with
	 * more than one enchantement.
	 * 
	 * If no  special model  apply to  this item stack,  this should  return the
	 * value from the {@param variantManager}.
	 * 
	 * @param stack The item stack to evaluate the model for.
	 * @param variantManager The provider for variant-based models
	 * @return The model ID , or null if the vanilla model should be used.
	 */
	public abstract @Nullable ModelIdentifier GetItemModel(ItemStack stack, IVariantManager variantManager);
}
