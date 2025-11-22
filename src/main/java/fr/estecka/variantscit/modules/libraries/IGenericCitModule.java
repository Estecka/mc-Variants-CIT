package fr.estecka.variantscit.modules.libraries;

import org.jetbrains.annotations.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public interface IGenericCitModule<L>
{
	@Nullable Identifier GetItemModel(ItemStack stack, L library);
}
