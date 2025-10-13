package fr.estecka.variantscit.modulebakers;

import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.api.ICitModule;
import fr.estecka.variantscit.api.IVariantManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public record SimpleBakedModule(
	IVariantManager library,
	ICitModule logic
)
implements IBakedModule
{
	public @Nullable Identifier GetModelForItem(ItemStack stack){
		return logic.GetItemModel(stack, library);
	}
}
