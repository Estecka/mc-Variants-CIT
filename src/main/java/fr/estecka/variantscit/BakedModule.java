package fr.estecka.variantscit;

import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.api.ICitModule;
import fr.estecka.variantscit.api.IVariantManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public record BakedModule(
	IVariantManager library,
	ICitModule logic
)
implements IItemModelProvider
{
	public @Nullable Identifier GetModelForItem(ItemStack stack){
		return logic.GetItemModel(stack, library);
	}
}
