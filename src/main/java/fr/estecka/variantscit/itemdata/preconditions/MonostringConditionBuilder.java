package fr.estecka.variantscit.itemdata.preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.itemdata.extractors.IDataExtractor;
import fr.estecka.variantscit.itemdata.extractors.TransformableExtractor;
import fr.estecka.variantscit.itemdata.transforms.IDataTransform;
import fr.estecka.variantscit.itemdata.transforms.SuccessiveTransform;
import fr.estecka.variantscit.itemdata.transforms.impl.NumberCompareTransform;
import fr.estecka.variantscit.itemdata.transforms.impl.StringCompareTransform;
import net.minecraft.util.ExtraCodecs;

public abstract class MonostringConditionBuilder
{

	static private final Codec<IDataTransform> TRANSFORM_CODEC = CodecUtil.WithAlternatives(
		StringCompareTransform.LITERAL_CODEC,
		NumberCompareTransform.LITERAL_CODEC_EQUAL,
		SuccessiveTransform.CODEC
	);

	static public final Codec<List<IItemPrecondition>> MAP_CODEC = ExtraCodecs
		.strictUnboundedMap(Codec.STRING, Codec.PASSTHROUGH)
		.flatXmap(MonostringConditionBuilder::ParseMap, CodecUtil::NoEncode)
		;

	static public DataResult<List<IItemPrecondition>> ParseMap(Map<String,Dynamic<?>> map){
		List<IItemPrecondition> result = new ArrayList<>();

		for (var entry : map.entrySet()) {
			var entryResult = ParseEntry(entry.getKey(), entry.getValue());
			if (entryResult.isError())
				return entryResult.map(_0->null);
			else
				result.add(entryResult.getOrThrow());
		}

		return DataResult.success(result);
	}

	static public DataResult<IItemPrecondition> ParseEntry(String key, Dynamic<?> dynamic){
		DataResult<IItemPrecondition> result;

		boolean negated = key.startsWith("!");
		if (negated)
			key = key.substring(1);

		if (key.equals("matches_any"))
			result = ConditionList.MATCHANY_CODEC.parse(dynamic);
		else if (key.equals("matches_all"))
			result = ConditionList.MATCHALL_CODEC.parse(dynamic);
		else {
			DataResult<IDataTransform> transform = TRANSFORM_CODEC.parse(dynamic);
			DataResult<IDataExtractor> extractor = CodecUtil.ParseString(IDataExtractor.MONOSTRING_DECODER, key).map(Function.identity());
			if (transform.isError())
				result = transform.map(_0->null);
			if (extractor.isError())
				result = extractor.map(_0->null);
			else
				result = DataResult.success(new TransformableExtractor<IDataExtractor>(extractor.getOrThrow(), transform.getOrThrow(), Optional.empty()));
		}

		return negated ? result.map(NegativeCondition::new) : result;
	}
}
