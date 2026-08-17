package fr.estecka.variantscit.reload;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.itemdata.transforms.IDataTransform;
import fr.estecka.variantscit.itemdata.transforms.SuccessiveTransform;
import fr.estecka.variantscit.itemdata.transforms.impl.StringCompareTransform;
import net.minecraft.resources.Identifier;


public record LibraryDefinition(
	Optional<String> modelPrefix,
	IDataTransform namespacePredicate,
	IDataTransform pathPredicate,
	Map<Identifier,Identifier> hardcodedList
)
{
	static private final Codec<IDataTransform> NAMESPACE_TRANSFORM_CODEC = CodecUtil.WithAlternatives(
		StringCompareTransform.LITERAL_CODEC,
		SuccessiveTransform.CODEC
	);

	static private final MapCodec<Optional<String>> PREFIX_CODEC = CodecUtil.MapWithAlternative(
		CodecUtil.LEGACY_ITEM_PATH.validate(CodecUtil.NonEmptyString("Model Prefix")).optionalFieldOf("modelPrefix"),
		Codec.BOOL.fieldOf("forceAllowEmptyPrefix").orElse(false).flatXmap(
			allowed -> allowed ? DataResult.success(Optional.of("")) : DataResult.error(()->"No waiver for empty Model Prefix."),
			prefix -> DataResult.success(prefix.isPresent() && prefix.get().isEmpty())
		)
	);

	static private final Codec<Map<Identifier,Identifier>> LEGACY_SPECIAL_CODEC = Codec.unboundedMap(
		CodecUtil.IDENTIFIER_PATH.flatXmap(path -> Identifier.read("variants-cit:special/"+path), CodecUtil.NoGetter("Legacy Special")),
		Identifier.CODEC.validate(CodecUtil::UnItemify)
	);

	static private final Codec<Map<Identifier,Identifier>> MODELLIST_CODEC = CodecUtil.WithAlternatives(
		Codec.unboundedMap(Identifier.CODEC, Identifier.CODEC),
		Identifier.CODEC.listOf().xmap(LibraryDefinition::FromArray, map->List.copyOf(map.keySet()))
	)
	.validate(LibraryDefinition::DisallowIntrinsic)
	;

	static private final MapCodec<Map<Identifier,Identifier>> HARDCODED_CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			Identifier.CODEC.validate(CodecUtil::UnItemify).optionalFieldOf("fallback").forGetter(CodecUtil.NoGetter("Legacy Fallback")),
			LEGACY_SPECIAL_CODEC.optionalFieldOf("special", Map.of()).forGetter(CodecUtil.NoGetter("Legacy Special")),
			MODELLIST_CODEC.optionalFieldOf("modelList", Map.of()).forGetter(Function.identity())
		)
		.apply(builder, LibraryDefinition::LegacyHardcoded)
	);

	static public final MapCodec<LibraryDefinition> MAP_CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			PREFIX_CODEC.forGetter(LibraryDefinition::modelPrefix),
			NAMESPACE_TRANSFORM_CODEC.optionalFieldOf("modelNamespace", IDataTransform.NOOP).forGetter(LibraryDefinition::namespacePredicate),
			IDataTransform.CODEC.optionalFieldOf("modelPathes", IDataTransform.NOOP).forGetter(LibraryDefinition::pathPredicate),
			HARDCODED_CODEC.forGetter(LibraryDefinition::hardcodedList)
		)
		.apply(builder, LibraryDefinition::new)
	);


/******************************************************************************/
/* ModelList Constructors                                                     */
/******************************************************************************/

	static private Map<Identifier,Identifier> FromArray(List<Identifier> list){
		Map<Identifier,Identifier> hardcoded = new HashMap<>();
		for (Identifier id : list)
			hardcoded.put(id, id);
		return hardcoded;
	}

	static private Map<Identifier,Identifier> LegacyHardcoded(
		Optional<Identifier> fallback,
		Map<Identifier,Identifier> special,
		Map<Identifier,Identifier> hardcoded
	){
		hardcoded = new HashMap<>(hardcoded);
		hardcoded.putAll(special);

		if (fallback.isPresent())
			hardcoded.put(VariantsCitMod.Identifier("fallback"), fallback.get());

		return Map.copyOf(hardcoded);
	}

	static private DataResult<Map<Identifier,Identifier>> DisallowIntrinsic(Map<Identifier,Identifier> hardcodedList){
		for (Identifier variantId : hardcodedList.keySet()){
			if (variantId.getNamespace().equals(VariantsCitMod.MODID) && variantId.getPath().startsWith("intrinsic/"))
				return DataResult.error(()->"Hardcoded model list may not override intrinsic models.");
		}

		return DataResult.success(hardcodedList);
	}


/******************************************************************************/
/* Asset Aggregation                                                          */
/******************************************************************************/

	/**
	 * @return If the library  accepts this assets, returns  every variant ID it
	 * is associated with. Otherwise, returns an empty set.
	 */
	public Set<Identifier> GetVariantIds(Identifier assetId){
		Set<Identifier> result = new HashSet<>();
		if (modelPrefix.isPresent() && !assetId.getNamespace().equals(VariantsCitMod.MODID) && assetId.getPath().startsWith(modelPrefix.get())){
			Identifier variantId = assetId.withPath(path->path.substring(modelPrefix.get().length()));
			if (!this.hardcodedList.containsKey(variantId) && this.AcceptsVariant(variantId))
				result.add(variantId);
		}

		for (var entry : hardcodedList.entrySet()) {
			if (entry.getValue().equals(assetId))
				result.add(entry.getKey());
		}

		return result;
	}

	public boolean AcceptsVariant(Identifier variantId){
		return !variantId.getNamespace().equals(VariantsCitMod.MODID)
		    && IDataTransform.Test(namespacePredicate, variantId.getNamespace())
		    && IDataTransform.Test(pathPredicate, variantId.getPath())
		    ;
	}

	public Identifier GetModelId(Identifier variantId){
		Identifier modelId = this.hardcodedList.get(variantId);
		if (modelId != null)
			return modelId;

		if (modelPrefix.isPresent() && this.AcceptsVariant(variantId))
			return variantId.withPrefix(this.modelPrefix.get());

		else
			return null;
	}
}
