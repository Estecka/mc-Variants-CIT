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
import fr.estecka.variantscit.itemdata.extractors.impl.ItemComponentProperty;
import fr.estecka.variantscit.itemdata.transforms.IDataTransform;
import fr.estecka.variantscit.itemdata.transforms.impl.NumberCompareTransform;
import fr.estecka.variantscit.itemdata.transforms.impl.StringCompareTransform;
import fr.estecka.variantscit.modules.cache.ECachePolicy;
import fr.estecka.variantscit.modules.cache.ICacheKey;
import net.minecraft.world.item.ItemStack;

/**
 * FIXME: Dangerous circular dependency with {@link MatchesAllCondition#LITERAL_CODEC}.
 * As is, {@link #CODEC} **MUST** be placed after {@link #MONOSTRINGMAP_CODEC}
 * for the static initialization to succeed.
 */
public interface IItemPrecondition
extends ICacheKey.Cacheable
{
	static public final Codec<List<IItemPrecondition>> MONOSTRINGMAP_CODEC = 
		Codec.unboundedMap(
			CodecUtil.WithAlternative(
				VCitRegistries.ITEM_PROPERTIES.unitCodec,
				ItemComponentProperty.MONOSTRING_DECODER
			),
			CodecUtil.WithAlternatives(
				StringCompareTransform.LITERAL_CODEC,
				NumberCompareTransform.LITERAL_CODEC_EQUAL,
				VCitRegistries.TRANSFORMS.mapCodec.codec()
			)
		)
		.flatComapMap(IItemPrecondition::MonostringMapToList, CodecUtil::NoEncode)
		;

	static public final Codec<IItemPrecondition> CODEC = CodecUtil.WithAlternative(
		VCitRegistries.PRECONDITIONS.mapCodec.codec(),
		MatchesAllCondition.LITERAL_CODEC
	);

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
