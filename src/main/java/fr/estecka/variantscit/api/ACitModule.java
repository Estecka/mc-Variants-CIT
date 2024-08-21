package fr.estecka.variantscit.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import fr.estecka.variantscit.CitLoader;
import fr.estecka.variantscit.ModuleDefinition;

public abstract class ACitModule
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
	private Map<Identifier, Identifier> variantModels = new HashMap<>();


	public ACitModule(ModuleDefinition definition){
		this.variantsDir = definition.variantDirectory();
		this.fallbackModel = definition.fallbackModel().orElse(null);
	}

	/**
	 * Returns the identifer of the item's variant, from which the model ID will
	 * be derived. Items with no variants will fallback to the vanilla model.
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
		Identifier modelId = this.variantModels.get(GetItemVariant(stack));
		return (modelId != null) ? modelId : this.fallbackModel;
	}

	/**
	 * Override to provide additional models that need to be loaded.
	 */
	public Identifier[] GetSpecialModels(){
		return new Identifier[0];
	}

	public final int GetVariantCount(){
		return this.variantModels.size();
	}

	public final Set<Identifier> GetAllModels(){
		Set<Identifier> result = new HashSet<>();
		result.addAll(this.variantModels.values());
		for (Identifier id : this.GetSpecialModels())
		if  (id != null)
			result.add(id);

		return result;
	}

	public final CitLoader GetModelLoader(){
		return new CitLoader(this.variantsDir, map->{this.variantModels = map;});
	}
}
