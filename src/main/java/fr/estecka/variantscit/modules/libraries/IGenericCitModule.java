package fr.estecka.variantscit.modules.libraries;

import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.commands.CommandLogger;
import fr.estecka.variantscit.modules.cache.ICacheSourceProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public interface IGenericCitModule<L>
extends ICacheSourceProvider
{
	@Nullable ResourceLocation GetItemModel(ItemStack stack, L library);

	default @Nullable ResourceLocation Walkthrough(ItemStack stack, L library, CommandLogger logger){
		return this.GetItemModel(stack, library);
	}
}
