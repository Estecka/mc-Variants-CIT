package fr.estecka.variantscit.api;

import org.jetbrains.annotations.Nullable;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public interface IVariantsCitModule
{
	/**
	 * Every model path is formatted as:
	 * `/assets/<namespace>/models/<path>/<name>.json`
	 * 
	 * Variants of the item will automatically be matched to the model with the
	 * corresponding `<namespace>` and `<name>`.
	 * 
	 * <path> should be unique to this modules.
	 * 
	 * * {@returns} the value of `<path>`
	 */
	public abstract String GetModelPath();

	/**
	 * Returns the identifer  of the item's variant. Items with no variants will
	 * fallback to the vanilla model. This will only be evaluated for items that
	 * do not have an exceptional model.
	 * 
	 * {@return} The identifier of the variant, or null if the item has no
	 */
	public abstract @Nullable Identifier GetItemVariant(ItemStack stack);

	/**
	 * The model to  use for items  that should be  using a  variant CIT, but no
	 * model was registered for this particular variant.
	 * 
	 * These models are not managed by the mod, and should be registered through
	 * Fabric API's {@link ModelLoadingPlugin}.
	 */
	public default @Nullable BakedModel GetFallbackModel(BakedModelManager manager){
		return null;
	}

	/**
	 * Should be use for when an item can use models that are not tied to a
	 * specific variants. E.g: a unique model for enchanted books with more than
	 * one enchantement.
	 * 
	 * These models are not managed by the mod, and should be registered through
	 * Fabric API's {@link ModelLoadingPlugin}.
	 * 
	 * @return The  model  to  use, or  null  if the  item  has  no  exceptional
	 * conditions
	 */
	public default @Nullable BakedModel GetExceptionalModel(ItemStack stack, BakedModelManager manager){
		return null;
	}
}
