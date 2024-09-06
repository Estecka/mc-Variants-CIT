package fr.estecka.variantscit;

import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.api.ICitModule;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.ItemStack;

public record BakedModule(
	VariantLibrary library,
	ICitModule logic
)
implements IItemModelProvider
{
	public @Nullable ModelIdentifier GetModelForItem(ItemStack stack){
		return logic.GetItemModel(stack, library);
	}
}
