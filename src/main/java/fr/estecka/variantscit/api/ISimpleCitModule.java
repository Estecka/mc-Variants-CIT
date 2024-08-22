package fr.estecka.variantscit.api;

import java.util.Map;
import org.jetbrains.annotations.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

@FunctionalInterface
public interface ISimpleCitModule
extends ICitModule
{
	public abstract @Nullable Identifier GetItemVariant(ItemStack stack);

	@Override
	public default void SetSpecialModels(Map<String, @Nullable Identifier> specialModels){
	}

	@Override
	public default @Nullable Identifier[] GetSpecialModels(){
		return new Identifier[0];
	}

	@Override
	public default @Nullable Identifier GetModelForItem(ItemStack stack, IVariantManager variantBasedModel){
		return variantBasedModel.GetModelVariantForItem(stack);
	}
}
