package fr.estecka.variantscit.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import fr.estecka.variantscit.CitLoader;
import fr.estecka.variantscit.ModuleDefinition;

public abstract class ACitModule
implements ModelLoadingPlugin
{
	/**
	 * Every variant model has a path formatted as:
	 * `/assets/<namespace>/models/<path>/<name>.json`,
	 * where  `<namespace>`  and  `<name>`  are  automatically  matched  to  the
	 * corresponding variant ID.
	 * 
	 * This is the value of the <path> component.
	 */
	public final String variantsDir;

	public final @Nullable Identifier fallbackModel;

	/**
	 * Maps variant IDs to model IDs.
	 */
	private @NotNull Map<Identifier, Identifier> existingModels = new HashMap<>();


	public ACitModule(ModuleDefinition definition){
		this.variantsDir = definition.variantDirectory();
		this.fallbackModel = definition.fallbackModel().orElse(null);
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
	 * Can be overriden  to provide special  models for situations  that are not
	 * handled by  the mod. E.g: a unique model  for enchanted  books  with more 
	 * than one enchantement.
	 * 
	 * @return The model id, or null if the vanilla model should be used.
	 */
	public @Nullable Identifier GetModelForItem(ItemStack stack){
		Identifier modelId = this.existingModels.get(GetItemVariant(stack));
		return (modelId != null) ? modelId : this.fallbackModel;
	}

	@MustBeInvokedByOverriders
	public void onInitializeModelLoader(ModelLoadingPlugin.Context pluginContext){
		pluginContext.addModels(existingModels.values());
	}

	public final CitLoader GetModelLoader(){
		return new CitLoader(this.variantsDir, map->{this.existingModels = map;});
	}
}
