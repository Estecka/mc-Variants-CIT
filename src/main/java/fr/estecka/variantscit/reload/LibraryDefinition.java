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
import net.minecraft.resources.ResourceLocation;


public record LibraryDefinition(
	Optional<String> modelPrefix,
	IDataTransform namespacePredicate,
	IDataTransform pathPredicate,
	Map<ResourceLocation,ResourceLocation> hardcodedList
)
{
	static private final Codec<IDataTransform> TRANSFORM_CODEC = CodecUtil.WithAlternatives(
		StringCompareTransform.LITERAL_CODEC,
		SuccessiveTransform.CODEC
	);

	static private final MapCodec<Optional<String>> PREFIX_CODEC = CodecUtil.MapWithAlternative(
		CodecUtil.LEGACY_ITEM_PATH.validate(CodecUtil.NonEmptyString("Model Prefix")).optionalFieldOf("modelPrefix"),
		Codec.BOOL.fieldOf("forceAllowEmptyPrefix").flatXmap(
			allowed -> allowed ? DataResult.success(Optional.of("")) : DataResult.error(()->"Model Prefix cannot be empty."),
			prefix -> DataResult.success(prefix.isPresent() && prefix.get().isEmpty())
		)
	);

	static private final Codec<Map<ResourceLocation,ResourceLocation>> LEGACY_SPECIAL_CODEC = Codec.unboundedMap(
		CodecUtil.IDENTIFIER_PATH.flatXmap(path -> ResourceLocation.read("variants-cit:special/"+path), CodecUtil.NoGetter("Legacy Special")),
		ResourceLocation.CODEC.validate(CodecUtil::UnItemify)
	);

	static private final Codec<Map<ResourceLocation,ResourceLocation>> MODELLIST_CODEC = CodecUtil.WithAlternatives(
		Codec.unboundedMap(ResourceLocation.CODEC, ResourceLocation.CODEC),
		ResourceLocation.CODEC.listOf().xmap(LibraryDefinition::FromArray, map->List.copyOf(map.keySet()))
	);

	static private final MapCodec<Map<ResourceLocation,ResourceLocation>> HARDCODED_CODEC = RecordCodecBuilder.<Map<ResourceLocation,ResourceLocation>>mapCodec(builder->builder
		.group(
			ResourceLocation.CODEC.validate(CodecUtil::UnItemify).optionalFieldOf("fallback").forGetter(CodecUtil.NoGetter("Legacy Fallback")),
			LEGACY_SPECIAL_CODEC.optionalFieldOf("special", Map.of()).forGetter(CodecUtil.NoGetter("Legacy Special")),
			MODELLIST_CODEC.optionalFieldOf("modelList", Map.of()).forGetter(Function.identity())
		)
		.apply(builder, LibraryDefinition::LegacyHardcoded)
	);

	static public final MapCodec<LibraryDefinition> MAP_CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			PREFIX_CODEC.forGetter(LibraryDefinition::modelPrefix),
			TRANSFORM_CODEC.optionalFieldOf("modelNamespace", IDataTransform.NOOP).forGetter(LibraryDefinition::namespacePredicate),
			IDataTransform.CODEC.optionalFieldOf("modelPathes", IDataTransform.NOOP).forGetter(LibraryDefinition::pathPredicate),
			HARDCODED_CODEC.forGetter(LibraryDefinition::hardcodedList)
		)
		.apply(builder, LibraryDefinition::new)
	);


/******************************************************************************/
/* ModelList Constructors                                                     */
/******************************************************************************/

	static private Map<ResourceLocation,ResourceLocation> FromArray(List<ResourceLocation> list){
		Map<ResourceLocation,ResourceLocation> hardcoded = new HashMap<>();
		for (ResourceLocation id : list)
			hardcoded.put(id, id);
		return hardcoded;
	}

	static private Map<ResourceLocation,ResourceLocation> LegacyHardcoded(
		Optional<ResourceLocation> fallback,
		Map<ResourceLocation,ResourceLocation> special,
		Map<ResourceLocation,ResourceLocation> hardcoded
	){
		hardcoded = new HashMap<>(hardcoded);
		hardcoded.putAll(special);

		if (fallback.isPresent())
			hardcoded.put(VariantsCitMod.Identifier("fallback"), fallback.get());

		return Map.copyOf(hardcoded);
	}


/******************************************************************************/
/* Asset Aggregation                                                          */
/******************************************************************************/

	/**
	 * @return If the library  accepts this assets, returns any variant ID it is
	 * associated with. Otherwise, returns an empty set.
	 */
	public Set<ResourceLocation> GetVariantIds(ResourceLocation assetId){
		Set<ResourceLocation> result = new HashSet<>();
		if (modelPrefix.isPresent() && !assetId.getNamespace().equals(VariantsCitMod.MODID) && assetId.getPath().startsWith(modelPrefix.get())){
			ResourceLocation variantId = assetId.withPath(path->path.substring(modelPrefix.get().length()));
			if (!this.hardcodedList.containsKey(variantId) && this.AcceptsVariant(variantId))
				result.add(variantId);
		}

		for (var entry : hardcodedList.entrySet()) {
			if (entry.getValue().equals(assetId))
				result.add(entry.getKey());
		}

		return result;
	}

	public boolean AcceptsVariant(ResourceLocation variantId){
		return IDataTransform.Test(namespacePredicate, variantId.getNamespace())
		    && IDataTransform.Test(pathPredicate, variantId.getPath())
		    ;
	}

	public ResourceLocation GetModelId(ResourceLocation variantId){
		if (!this.AcceptsVariant(variantId))
			return null;

		ResourceLocation modelId = this.hardcodedList.get(variantId);
		if (modelId == null && modelPrefix.isPresent() && this.AcceptsVariant(variantId))
			modelId = variantId.withPrefix(this.modelPrefix.get());

		return modelId;
	}
}
