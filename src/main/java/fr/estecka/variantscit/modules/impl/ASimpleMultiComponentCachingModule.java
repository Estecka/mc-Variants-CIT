package fr.estecka.variantscit.modules.impl;

import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.modules.libraries.IVariantLibrary;
import fr.estecka.variantscit.commands.CommandLogger;
import fr.estecka.variantscit.format.properties.IStringProperty;

abstract class ASimpleMultiComponentCachingModule
extends AMultiComponentCachingModule
{
	protected ASimpleMultiComponentCachingModule(boolean debug, Stream<IStringProperty> properties){
		super(debug, properties);
	}

	public abstract @Nullable ResourceLocation RecomputeItemVariant(ItemStack stack);

	@Override
	public @Nullable ResourceLocation RecomputeItemModel(ItemStack stack, IVariantLibrary library) {
		ResourceLocation variant = this.RecomputeItemVariant(stack);
		if (debug)
			VariantsCitMod.LOGGER.info("[ASimpleMultiComponent] Variant Id: {}", variant);
		return library.GetVariantModel(variant);
	}

	@Override
	public @Nullable ResourceLocation Walkthrough(ItemStack stack, IVariantLibrary library, CommandLogger logger) {
		return this.RecomputeItemModel(stack, library);
	}
}
