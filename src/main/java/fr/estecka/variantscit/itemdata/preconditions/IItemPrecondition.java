package fr.estecka.variantscit.itemdata.preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.mojang.serialization.Codec;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.VCitRegistries;
import fr.estecka.variantscit.itemdata.extractors.IDataExtractor;
import fr.estecka.variantscit.itemdata.extractors.TransformableExtractor;
import fr.estecka.variantscit.itemdata.transforms.IDataTransform;
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

	static public final Codec<List<IItemPrecondition>> MONOSTRINGMAP_CODEC = Codec.unboundedMap(IDataExtractor.MONOSTRING_CODEC, VCitRegistries.TRANSFORMS.codec)
		.flatComapMap(IItemPrecondition::MonostringMapToList, CodecUtil::NoEncode)
		;

	static private List<IItemPrecondition> MonostringMapToList(Map<IDataExtractor, IDataTransform> map){
		List<IItemPrecondition> result = new ArrayList<>();
		for (var entry : map.entrySet())
			result.add(new TransformableExtractor<>(entry.getKey(), entry.getValue(), Optional.empty()));
		return result;
	}

	boolean Matches(ItemStack stack);

	@Override
	default ECachePolicy GetCachePolicy() {
		return ECachePolicy.ALWAYS;
	}
}
