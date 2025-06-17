package fr.estecka.variantscit.modules;

import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.api.IVariantManager;
import fr.estecka.variantscit.format.properties.IStringProperty;
import net.minecraft.client.util.ModelIdentifier;
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
	public @Nullable ModelIdentifier RecomputeItemModel(ItemStack stack, IVariantManager library) {
		Identifier variant = this.RecomputeItemVariant(stack);
		return library.GetVariantModel(variant);
	}
}
