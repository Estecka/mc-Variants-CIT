package fr.estecka.variantscit.reload;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.VCitRegistries;
import fr.estecka.variantscit.assetgen.IAssetGenerator;
import fr.estecka.variantscit.itemdata.preconditions.IItemPrecondition;


public record ModuleDefinition(
	@Deprecated ResourceLocation type,
	IUnbakedModule parameters,
	List<EModuleHook> hooks,
	Optional<List<ResourceLocation>> targets,
	Optional<IItemPrecondition> precondition,
	int priority,
	String modelPrefix,
	Optional<ResourceLocation> modelParent,
	Optional<IAssetGenerator> assetGen,
	Optional<ResourceLocation> fallbackModel,
	Map<String,ResourceLocation> specialModels
)
{
	static private final MapCodec<String> PREFIX_CODEC = CodecUtil.MapWithAlternative(
		CodecUtil.LEGACY_ITEM_PATH.fieldOf("modelPrefix").validate(CodecUtil.NonEmptyString("Model Prefix")),
		Codec.BOOL.fieldOf("forceAllowEmptyPrefix").flatXmap(
			allowed -> allowed ? DataResult.success("") : DataResult.error(()->"Model Prefix cannot be empty."),
			prefix -> DataResult.success(prefix.isEmpty())
		)
	);

	static public final MapCodec<ModuleDefinition> CODEC = RecordCodecBuilder.<ModuleDefinition>mapCodec(builder->builder
		.group(
			ResourceLocation.CODEC.fieldOf("type").forGetter(ModuleDefinition::type),
			VCitRegistries.MODULES.mapCodec.forGetter(ModuleDefinition::parameters),
			CodecUtil.WithAlias(CodecUtil.OneOrMany(EModuleHook.CODEC), "hook", "context").orElse(List.of(EModuleHook.ITEM_MODEL)).forGetter(ModuleDefinition::hooks),
			CodecUtil.OneOrMany(ResourceLocation.CODEC).optionalFieldOf("items").forGetter(ModuleDefinition::targets),
			IItemPrecondition.CODEC.optionalFieldOf("precondition").forGetter(ModuleDefinition::precondition),
			Codec.INT.fieldOf("priority").orElse(0).forGetter(ModuleDefinition::priority),
			PREFIX_CODEC.forGetter(ModuleDefinition::modelPrefix),
			ResourceLocation.CODEC.optionalFieldOf("modelParent").forGetter(ModuleDefinition::modelParent),
			IAssetGenerator.CODEC.optionalFieldOf("assetGen").forGetter(ModuleDefinition::assetGen),
			ResourceLocation.CODEC.validate(CodecUtil::UnItemify).optionalFieldOf("fallback").forGetter(ModuleDefinition::fallbackModel),
			Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC.validate(CodecUtil::UnItemify)).optionalFieldOf("special", ImmutableMap.<String,ResourceLocation>of()).forGetter(ModuleDefinition::specialModels)
		)
		.apply(builder, ModuleDefinition::new)
	);
}
