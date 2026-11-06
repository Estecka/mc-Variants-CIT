package fr.estecka.variantscit.itemdata.preconditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.util.CodecUtil;
import fr.estecka.variantscit.VCitRegistries;
import fr.estecka.variantscit.modules.cache.ECachePolicy;
import fr.estecka.variantscit.modules.cache.ICacheKey;
import net.minecraft.world.item.ItemStack;


public interface IItemPrecondition
extends ICacheKey.Cacheable
{
	static public MapCodec<IItemPrecondition> CANONICAL_MAPCODEC = VCitRegistries.PRECONDITIONS.mapCodec;

	static public final Codec<IItemPrecondition> CODEC = CodecUtil.WithAlternative(
		CANONICAL_MAPCODEC.codec(),
		ConditionList.MATCHALL_CODEC
	);

	boolean Matches(ItemStack stack);

	@Override
	default ECachePolicy GetCachePolicy() {
		return ECachePolicy.ALWAYS;
	}
}
