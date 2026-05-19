package fr.estecka.variantscit.reload;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import net.minecraft.util.ExtraCodecs;

/**
 * TODO: Fallback and special models are not included in this definition at this
 * time.
 */
public record LibraryDefinition(
	String modelPrefix,
	IDataTransform namespacePredicate,
	IDataTransform pathPredicate,
	Map<ResourceLocation,ResourceLocation> hardcoded
)
{
	static private final Codec<IDataTransform> TRANSFORM_CODEC = CodecUtil.WithAlternatives(
		StringCompareTransform.LITERAL_CODEC,
		SuccessiveTransform.CODEC
	);

	static private final MapCodec<String> PREFIX_CODEC = CodecUtil.MapWithAlternative(
		CodecUtil.IDENTIFIER_PATH.validate(CodecUtil.NonEmptyString("Model Prefix")).fieldOf("modelPrefix"),
		Codec.BOOL.fieldOf("forceAllowEmptyPrefix").<String>flatXmap(
			allowed -> allowed ? DataResult.success("") : DataResult.error(()->"Model Prefix cannot be empty."),
			prefix -> DataResult.success(prefix.isEmpty())
		)
	);

	static public final MapCodec<LibraryDefinition> ADVANCED_MAPCODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			PREFIX_CODEC.forGetter(LibraryDefinition::modelPrefix),
			TRANSFORM_CODEC.optionalFieldOf("namespace", IDataTransform.NOOP).forGetter(LibraryDefinition::namespacePredicate),
			IDataTransform.CODEC.optionalFieldOf("variantPathes", IDataTransform.NOOP).forGetter(LibraryDefinition::pathPredicate),
			ExtraCodecs.strictUnboundedMap(ResourceLocation.CODEC, ResourceLocation.CODEC).fieldOf("hardcoded").forGetter(LibraryDefinition::hardcoded)
		)
		.apply(builder, LibraryDefinition::new)
	);

	static public final Codec<LibraryDefinition> ARRAY_CODEC = ResourceLocation.CODEC
		.listOf()
		.xmap(LibraryDefinition::FromArray, CodecUtil.NoGetter("Library Array form"))
		;

	static public final MapCodec<LibraryDefinition> ROOT_MAPCODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			CodecUtil.LEGACY_ITEM_PATH.validate(CodecUtil.NonEmptyString("Model Prefix")).fieldOf("modelPrefix").forGetter(LibraryDefinition::modelPrefix),
			ResourceLocation.CODEC.validate(CodecUtil::UnItemify).optionalFieldOf("fallback").forGetter(CodecUtil.NoGetter("Legacy Fallback")),
			Codec.unboundedMap(
				CodecUtil.IDENTIFIER_PATH.flatXmap(path -> ResourceLocation.read("variants-cit:special/"+path), CodecUtil.NoGetter("Legacy Special")),
				ResourceLocation.CODEC.validate(CodecUtil::UnItemify)
			).optionalFieldOf("special", Map.of()).forGetter(CodecUtil.NoGetter("Legacy Special"))
		)
		.apply(builder, LibraryDefinition::FromLegacy)
	);

	static public final MapCodec<LibraryDefinition> MAP_CODEC = CodecUtil.MapWithAlternatives(
		ADVANCED_MAPCODEC.fieldOf("assetLibrary"),
		ARRAY_CODEC.fieldOf("assetLibrary"),
		ROOT_MAPCODEC
	);


/******************************************************************************/
/* Constructors                                                               */
/******************************************************************************/

	static private LibraryDefinition FromArray(List<ResourceLocation> list){
		Map<ResourceLocation,ResourceLocation> hardcoded = new HashMap<>();
		for (ResourceLocation id : list)
			hardcoded.put(id, id);
		return new LibraryDefinition("__noprefix__", IDataTransform.NULL, IDataTransform.NULL, hardcoded);
	}

	static private LibraryDefinition FromLegacy(String modelPrefix, Optional<ResourceLocation> fallback, Map<ResourceLocation,ResourceLocation> special){
		Map<ResourceLocation,ResourceLocation> hardcoded = new HashMap<>(special);

		if (fallback.isPresent())
			hardcoded.put(VariantsCitMod.Identifier("fallback"), fallback.get());

		return new LibraryDefinition(
			modelPrefix,
			IDataTransform.NOOP,
			IDataTransform.NOOP,
			Map.copyOf(hardcoded)
		);
	}


/******************************************************************************/
/* Asset Aggregation                                                          */
/******************************************************************************/

	/**
	 * @return If the library accepts this assets, returns any variant ID it is
	 * associated with. Ohterwise, returns an empty set.
	 */
	public Set<ResourceLocation> GetVariantIds(ResourceLocation assetId){
		Set<ResourceLocation> result = new HashSet<>();
		if (assetId.getPath().startsWith(modelPrefix)){
			ResourceLocation variantId = assetId.withPath(path->path.substring(modelPrefix.length()));
			if (!this.hardcoded.containsKey(variantId) && this.AcceptsVariant(variantId))
				result.add(variantId);
		}

		for (var entry : hardcoded.entrySet()) {
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
}
