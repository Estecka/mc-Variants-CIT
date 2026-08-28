package fr.estecka.variantscit.modules.libraries;

import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.commands.CommandLogger;
import fr.estecka.variantscit.modules.cache.ICacheKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public interface IGenericCitModule<L>
extends ICacheKey.Cacheable
{
	@Nullable Identifier GetItemModel(ItemStack stack, L library);

	default @Nullable Identifier Walkthrough(ItemStack stack, L library, CommandLogger logger){
		return this.GetItemModel(stack, library);
	}
}
