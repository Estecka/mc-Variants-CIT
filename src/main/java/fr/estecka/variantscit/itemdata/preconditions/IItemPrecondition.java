package fr.estecka.variantscit.itemdata.preconditions;

import com.mojang.serialization.Codec;
import fr.estecka.variantscit.VCitRegistries;
import fr.estecka.variantscit.modules.cache.ECachePolicy;
import fr.estecka.variantscit.modules.cache.ICacheKey;
import net.minecraft.world.item.ItemStack;

public interface IItemPrecondition
extends ICacheKey.Cacheable
{
	static public final Codec<IItemPrecondition> CODEC = Codec.withAlternative(
		VCitRegistries.PRECONDITIONS.mapCodec.codec(),
		MatchesAllCondition.ARRAY_CODEC
	);

	boolean Matches(ItemStack stack);

	@Override
	default ECachePolicy GetCachePolicy() {
		return ECachePolicy.ALWAYS;
	}
}
