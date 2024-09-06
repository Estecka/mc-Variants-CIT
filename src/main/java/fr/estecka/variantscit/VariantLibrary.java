package fr.estecka.variantscit;

import java.util.Map;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.api.ICitModule;
import fr.estecka.variantscit.api.IVariantManager;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public final class VariantLibrary
implements IVariantManager
{
	@Deprecated
	private ICitModule module;

	private final @Nullable ModelIdentifier fallbackModel;
	/**
	 * Maps variant IDs to model IDs.
	 */
	private final Map<Identifier, ModelIdentifier> variantModels;
	private final Map<String, ModelIdentifier> specialModels;

	public VariantLibrary(
		@Nullable ModelIdentifier fallbackModel,
		Map<Identifier, ModelIdentifier> variantModels,
		Map<String, ModelIdentifier> specialModels
	){
		this.fallbackModel = fallbackModel;
		this.variantModels = variantModels;
		this.specialModels = specialModels;
	}

	@Deprecated
	public void SetModule(ICitModule module){
		this.module = module;
	}

	@Deprecated
	@Override
	public @Nullable ModelIdentifier GetModelVariantForItem(ItemStack stack){
		return GetVariantModel(module.GetItemVariant(stack));
	}

	@Override
	public boolean HasVariantModel(Identifier variant){
		return this.variantModels.containsKey(variant);
	}

	@Override
	public @Nullable ModelIdentifier GetVariantModel(Identifier variant){
		if (variant == null)
			return null;
		else
			return this.variantModels.getOrDefault(variant, this.fallbackModel);
	}

	@Override
	public @Nullable ModelIdentifier GetSpecialModel(String key){
		return this.specialModels.get(key);
	}
}
