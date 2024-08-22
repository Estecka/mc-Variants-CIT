package fr.estecka.variantscit.api;

import java.util.Map;
import org.jetbrains.annotations.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public interface ICitModule
{
	/**
	 * Returns the identifer of the item's variant, from which the model ID will
	 * be derived. Items with no variants will fallback to the vanilla model.
	 * 
	 * {@return} The variant's identifier, or null if the item has none.
	 */
	public abstract @Nullable Identifier GetItemVariant(ItemStack stack);

	/**
	 * @param specialModel  Provides the model identifiers  that were configured
	 * in the resource pack. It is up to you to only keep the models you need.
	 */
	public abstract void SetSpecialModels(Map<String, @Nullable Identifier> specialModels);

	/**
	 * Provides the list  of special models  managed by this module, so they can
	 * also be  loaded. Those should be  the same models  that were provided via
	 * {@link #SetSpecialModels}.
	 */
	public abstract @Nullable Identifier[] GetSpecialModels();

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
	public abstract @Nullable Identifier GetModelForItem(ItemStack stack, IVariantManager variantManager);
}
