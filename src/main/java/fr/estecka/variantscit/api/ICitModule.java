package fr.estecka.variantscit.api;

import java.util.Map;
import org.jetbrains.annotations.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public interface ICitModule
extends IVariantProvider
{
	/**
	 * @param specialModel Provides the model identifiers that were configured
	 * for this module.
	 */
	public abstract void SetSpecialModels(Map<String, @Nullable Identifier> specialModels);

	/**
	 * Provides additional models that need  to be loaded; typically the list of
	 * all special  models, although  models that are  already  loaded via other
	 * means don't need to be provided.
	 */
	public default @Nullable Identifier[] GetSpecialModels(){
		return new Identifier[0];
	}

	/**
	 * Special logic  for overriding the variant-based models  in scenarios that
	 * are not handled by the mod. E.g: a unique model  for enchanted books with
	 * more than one enchantement.
	 * 
	 * If no special model apply to this item stack, this should return the
	 * value from {@param variantBasedModel}.
	 * 
	 * @param stack The item stack to evaluate the model for.
	 * @param variantBasedModel The provider for variant-based models
	 * @return The model ID , or null if the vanilla model should be used.
	 */
	public abstract @Nullable Identifier GetModelForItem(ItemStack stack, IVariantModelProvider variantBasedModel);
}
