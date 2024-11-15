package fr.estecka.variantscit;

import java.util.Map;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.api.IVariantManager;
import net.minecraft.util.Identifier;

public final class VariantLibrary
implements IVariantManager
{
	// @Deprecated
	// private ICitModule module;

	private final @Nullable Identifier fallbackModel;
	/**
	 * Maps variant IDs to model IDs.
	 */
	private final Map<Identifier, Identifier> variantModels;
	private final Map<String, Identifier> specialModels;

	public VariantLibrary(
		@Nullable Identifier fallbackModel,
		Map<Identifier, Identifier> variantModels,
		Map<String, Identifier> specialModels
	){
		this.fallbackModel = fallbackModel;
		this.variantModels = variantModels;
		this.specialModels = specialModels;
	}

	// @Deprecated
	// public void SetModule(ICitModule module){
	// 	this.module = module;
	// }

	// @Deprecated
	// @Override
	// public @Nullable ModelIdentifier GetModelVariantForItem(ItemStack stack){
	// 	return GetVariantModel(module.GetItemVariant(stack));
	// }

	@Override
	public boolean HasVariantModel(Identifier variant){
		return this.variantModels.containsKey(variant);
	}

	@Override
	public @Nullable Identifier GetVariantModel(Identifier variant){
		if (variant == null)
			return null;
		else
			return this.variantModels.getOrDefault(variant, this.fallbackModel);
	}

	@Override
	public @Nullable Identifier GetSpecialModel(String key){
		return this.specialModels.get(key);
	}

	public int GetVariantCount(){
		return this.variantModels.size();
	}
}
