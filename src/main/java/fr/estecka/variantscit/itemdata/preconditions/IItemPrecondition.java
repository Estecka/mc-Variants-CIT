package fr.estecka.variantscit.itemdata.preconditions;

import fr.estecka.variantscit.modules.cache.ECachePolicy;
import fr.estecka.variantscit.modules.cache.ICacheKey;
import net.minecraft.world.item.ItemStack;

public interface IItemPrecondition
extends ICacheKey.Cacheable
{
	boolean Matches(ItemStack stack);

	@Override
	default ECachePolicy GetCachePolicy() {
		return ECachePolicy.ALWAYS;
	}
}
