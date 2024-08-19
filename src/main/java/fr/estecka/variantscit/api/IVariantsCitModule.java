package fr.estecka.variantscit.api;

import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public interface IVariantsCitModule
{

	/**
	 * Which items this module should run on.
	 * This is only evaluated during mod initialization.
	 */
	public abstract @NotNull Collection<Item> GetItemTypes();

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
	public abstract @NotNull String GetModelPath();

	/**
	 * Returns the identifer  of the item's variant. Items with no variants will
	 * fallback to the vanilla model. This will only be evaluated for items that
	 * do not have an exceptional model.
	 * 
	 * {@return} The variant's identifier, or null if the item has none.
	 */
	public abstract @Nullable Identifier GetItemVariant(ItemStack stack);

	/**
	 * The model to  use for items  that should be  using a  variant CIT, but no
	 * model was registered for this particular variant.
	 * 
	 * These models are not managed by the mod, and should be registered through
	 * Fabric API's {@link ModelLoadingPlugin}.
	 */
	public default @Nullable Identifier GetFallbackModel(){
		return null;
	}

	/**
	 * Should be  used for when an item must use a  models that is not tied to a
	 * specific variants. E.g: a unique model for enchanted books with more than
	 * one enchantement.
	 * 
	 * These models are not managed by the mod, and should be registered through
	 * Fabric API's {@link ModelLoadingPlugin}.
	 * 
	 * @return The  model id  to use, or null  if the  item  has no  exceptional
	 * conditions.
	 */
	public default @Nullable Identifier GetExceptionalModel(ItemStack stack){
		return null;
	}
}
