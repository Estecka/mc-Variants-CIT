package fr.estecka.variantscit.itemdata.preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.VCitRegistries;
import fr.estecka.variantscit.itemdata.extractors.IDataExtractor;
import fr.estecka.variantscit.itemdata.extractors.TransformableExtractor;
import fr.estecka.variantscit.itemdata.extractors.impl.ItemComponentProperty;
import fr.estecka.variantscit.itemdata.transforms.IDataTransform;
import fr.estecka.variantscit.itemdata.transforms.SuccessiveTransform;
import fr.estecka.variantscit.itemdata.transforms.impl.NumberCompareTransform;
import fr.estecka.variantscit.itemdata.transforms.impl.StringCompareTransform;
import fr.estecka.variantscit.modules.cache.ECachePolicy;
import fr.estecka.variantscit.modules.cache.ICacheKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;


public interface IItemPrecondition
extends ICacheKey.Cacheable
{
	static public final Codec<IItemPrecondition> CODEC = CodecUtil.WithAlternative(
		VCitRegistries.PRECONDITIONS.mapCodec.codec(),
		ConditionList.MATCHALL_CODEC
	);

	static public final Codec<List<IItemPrecondition>> PLAIN_MONOSTRINGMAP_CODEC = 
		Codec.unboundedMap(
			CodecUtil.WithAlternative(
				VCitRegistries.ITEM_PROPERTIES.unitCodec,
				ItemComponentProperty.MONOSTRING_DECODER
			),
			CodecUtil.WithAlternatives(
				StringCompareTransform.LITERAL_CODEC,
				NumberCompareTransform.LITERAL_CODEC_EQUAL,
				SuccessiveTransform.CODEC
			)
		)
		.flatComapMap(IItemPrecondition::MonostringMapToList, CodecUtil::NoEncode)
		;

	static public final Codec<List<IItemPrecondition>> MONOSTRINGMAP_CODEC = RecordCodecBuilder.create(builder->
		builder.group(
			ConditionList.MATCHANY_CODEC.optionalFieldOf("matches_any").forGetter(CodecUtil.NoGetter("IItemPrecondition")),
			ConditionList.MATCHALL_CODEC.optionalFieldOf("matches_all").forGetter(CodecUtil.NoGetter("IItemPrecondition")),
			MapCodec.assumeMapUnsafe(CompoundTag.CODEC)
				.xmap(IItemPrecondition::RemoveReservedKeys, Function.identity())
				.flatXmap(nbt->PLAIN_MONOSTRINGMAP_CODEC.parse(NbtOps.INSTANCE, nbt), CodecUtil::NoEncode)
				.forGetter(Function.identity())
		)
		.apply(builder, (any, all, other)->{
			List<IItemPrecondition> result = new ArrayList<>();
			result.addAll(other);
			any.ifPresent(result::add);
			all.ifPresent(result::add);
			return result;
		})
	);

	static private List<IItemPrecondition> MonostringMapToList(Map<IDataExtractor, IDataTransform> map){
		List<IItemPrecondition> result = new ArrayList<>();
		for (var entry : map.entrySet())
			result.add(new TransformableExtractor<>(entry.getKey(), entry.getValue(), Optional.empty()));
		return result;
	}

	static private CompoundTag RemoveReservedKeys(CompoundTag map){
		map.remove("matches_any");
		map.remove("matches_all");
		return map;
	}

	boolean Matches(ItemStack stack);

	@Override
	default ECachePolicy GetCachePolicy() {
		return ECachePolicy.ALWAYS;
	}
}
