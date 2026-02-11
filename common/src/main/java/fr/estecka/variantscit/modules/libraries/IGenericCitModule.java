package fr.estecka.variantscit.modules.libraries;

import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.commands.CommandLogger;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public interface IGenericCitModule<L>
{
	@Nullable Identifier GetItemModel(ItemStack stack, L library);

	default @Nullable Identifier Walkthrough(ItemStack stack, L library, CommandLogger logger){
		return this.GetItemModel(stack, library);
	}
}
