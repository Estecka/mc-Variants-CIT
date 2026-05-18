package fr.estecka.variantscit.reload;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
	IDataTransform pathPredicate
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
			IDataTransform.CODEC.optionalFieldOf("pathes", IDataTransform.NOOP).forGetter(LibraryDefinition::pathPredicate)
			// ExtraCodecs.strictUnboundedMap(ResourceLocation.CODEC, ResourceLocation.CODEC).fieldOf("hardcoded").forGetter(LibraryDefinition::hardcoded)
		)
		.apply(builder, LibraryDefinition::new)
	);

	static public final MapCodec<LibraryDefinition> LEGACY_MAPCODEC = CodecUtil.LEGACY_ITEM_PATH
		.validate(CodecUtil.NonEmptyString("Model Prefix"))
		.fieldOf("modelPrefix")
		.xmap(
			prefix -> new LibraryDefinition(prefix, IDataTransform.NOOP, IDataTransform.NOOP),
			LibraryDefinition::modelPrefix
		)
		;

	/*
	static public final MapCodec<LibraryDefinition> LEGACY_MAPCODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			CodecUtil.IDENTIFIER_PATH.validate(LibraryDefinition::UnItemify).fieldOf("modelPrefix").forGetter(LibraryDefinition::modelPrefix),
			ResourceLocation.CODEC.validate(LibraryDefinition::UnItemify).optionalFieldOf("fallback").forGetter(CodecUtil.NoGetter("Legacy Fallback")),
			Codec.unboundedMap(CodecUtil.IDENTIFIER_PATH, ResourceLocation.CODEC.validate(LibraryDefinition::UnItemify)).optionalFieldOf("special", Map.<String,ResourceLocation>of()).forGetter(CodecUtil.NoGetter("Legacy Special"))
		)
		.apply(builder, LibraryDefinition::CreateLegacy)
	);
	*/

	static public final MapCodec<LibraryDefinition> MAP_CODEC = CodecUtil.MapWithAlternative(
		ADVANCED_MAPCODEC.fieldOf("assetLibrary"),
		LEGACY_MAPCODEC
	);


/******************************************************************************/
/* Codec Util                                                                 */
/******************************************************************************/


	/*
	static private LibraryDefinition CreateLegacy(String modelPrefix, Optional<ResourceLocation> fallback, Map<String,ResourceLocation> special){
		Map<ResourceLocation,ResourceLocation> hardcoded = new HashMap<>();

		if (fallback.isPresent())
			hardcoded.put(VariantsCitMod.Identifier("fallback"), fallback.get());

		for (var entry : special.entrySet()) {
			hardcoded.put(
				VariantsCitMod.Identifier(entry.getKey()).withPath(p->"special/"+p),
				entry.getValue()
			);
		}

		return new LibraryDefinition(
			modelPrefix,
			IDataTransform.NOOP,
			IDataTransform.NOOP,
			Map.copyOf(hardcoded)
		);
	}
	*/


/******************************************************************************/
/* Asset Aggregation                                                          */
/******************************************************************************/

	public Optional<ResourceLocation> AcceptsAsset(ResourceLocation assetId){
		if (!assetId.getPath().startsWith(modelPrefix))
			return Optional.empty();

		ResourceLocation variantId = assetId.withPath(path->path.substring(modelPrefix.length()));
		if (this.AcceptsVariant(variantId))
			return Optional.of(variantId);
		else
			return Optional.empty();
	}

	public boolean AcceptsVariant(ResourceLocation variantId){
		return IDataTransform.Test(namespacePredicate, variantId.getNamespace())
		    && IDataTransform.Test(pathPredicate, variantId.getPath())
		    ;
	}
}
