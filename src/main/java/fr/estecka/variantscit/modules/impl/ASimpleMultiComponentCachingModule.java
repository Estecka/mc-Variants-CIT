package fr.estecka.variantscit.modules.impl;

import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.api.IVariantManager;
import fr.estecka.variantscit.format.properties.IStringProperty;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

abstract class ASimpleMultiComponentCachingModule
extends AMultiComponentCachingModule
{
	protected ASimpleMultiComponentCachingModule(boolean debug, Stream<IStringProperty> properties){
		super(debug, properties);
	}

	public abstract @Nullable Identifier RecomputeItemVariant(ItemStack stack);

	@Override
	public @Nullable Identifier RecomputeItemModel(ItemStack stack, IVariantManager library) {
		Identifier variant = this.RecomputeItemVariant(stack);
		if (debug)
			VariantsCitMod.LOGGER.info("[ASimpleMultiComponent] Variant Id: {}", variant);
		return library.GetVariantModel(variant);
	}
}
