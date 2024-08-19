package fr.estecka.variantscit.api;

import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.google.common.collect.ImmutableSet;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import fr.estecka.variantscit.CitLoader;

public abstract class ACitModule
{
	/**
	 * Which items this module  should run on. This is only evaluated during mod
	 * initialization.
	 */
	public final ImmutableSet<Item> validItems;

	/**
	 * Every model path is formatted as:
	 * `/assets/<namespace>/models/<path>/<name>.json`,
	 * where variants of an item will automatically be matched to the model with
	 * the corresponding `<namespace>` and `<name>`.
	 * 
	 * This is the value of the <path> component.
	 */
	public final String modelsPath;

	/**
	 * Maps variant IDs to model IDs.
	 */
	private @NotNull Map<Identifier, Identifier> existingModels = new HashMap<>();


	public ACitModule(String modelsPath, Item... validItems){
		this.modelsPath = modelsPath;
		this.validItems = ImmutableSet.copyOf(validItems);
	}

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
	public @Nullable Identifier GetFallbackModel(){
		return null;
	}

	/**
	 * Can be  overriden  to provide special  models for  exceptional conditions
	 * that are not handled by the  mod. E.g: a unique model for enchanted books
	 * with more than one enchantement.
	 * 
	 * @return The model id, or null if the vanilla model should be used.
	 */
	public @Nullable Identifier GetModelForItem(ItemStack stack){
		Identifier modelId = this.existingModels.get(GetItemVariant(stack));
		return (modelId != null) ? modelId : this.GetFallbackModel();
	}

	public final CitLoader GetModelLoader(){
		return new CitLoader(this.modelsPath, map->{this.existingModels = map;});
	}
}
