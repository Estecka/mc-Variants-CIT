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
import fr.estecka.variantscit.itemdata.preconditions.MatchesAllCondition;


public record ModuleDefinition(
	@Deprecated ResourceLocation type,
	UnbakedModule<?> parameters,
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
	static public final MapCodec<ModuleDefinition> CODEC = RecordCodecBuilder.<ModuleDefinition>mapCodec(builder->builder
		.group(
			ResourceLocation.CODEC.fieldOf("type").forGetter(ModuleDefinition::type),
			VCitRegistries.MODULES.mapCodec.forGetter(ModuleDefinition::parameters),
			CodecUtil.WithAlias(CodecUtil.OneOrMany(EModuleHook.CODEC), "hook", "context").orElse(List.of(EModuleHook.ITEM_MODEL)).forGetter(ModuleDefinition::hooks),
			CodecUtil.OneOrMany(ResourceLocation.CODEC).optionalFieldOf("items").forGetter(ModuleDefinition::targets),
			CodecUtil.WithAlternatives(VCitRegistries.PRECONDITIONS.codec, MatchesAllCondition.LITERAL_CODEC).optionalFieldOf("precondition").forGetter(ModuleDefinition::precondition),
			Codec.INT.fieldOf("priority").orElse(0).forGetter(ModuleDefinition::priority),
			Codec.STRING.validate(ModuleDefinition::ValidatePath).fieldOf("modelPrefix").forGetter(ModuleDefinition::modelPrefix),
			ResourceLocation.CODEC.optionalFieldOf("modelParent").forGetter(ModuleDefinition::fallbackModel),
			IAssetGenerator.CODEC.optionalFieldOf("assetGen").forGetter(ModuleDefinition::assetGen),
			ResourceLocation.CODEC.validate(ModuleDefinition::UnItemify).optionalFieldOf("fallback").forGetter(ModuleDefinition::fallbackModel),
			Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC.validate(ModuleDefinition::UnItemify)).optionalFieldOf("special", ImmutableMap.<String,ResourceLocation>of()).forGetter(ModuleDefinition::specialModels)
		)
		.apply(builder, ModuleDefinition::new)
	);

	static private String UnItemify(String modelPrefix){
		if (modelPrefix.startsWith("item/")){
			// VariantsCitMod.LOGGER.warn("Stripped leading \"item/\" from model path: \"{}\"", modelPrefix);
			modelPrefix = modelPrefix.substring("item/".length());
		}
		return modelPrefix;
	}

	static private DataResult<ResourceLocation> UnItemify(ResourceLocation original){
		return DataResult.success(ResourceLocation.fromNamespaceAndPath(original.getNamespace(), UnItemify(original.getPath())));
	}

	static public DataResult<String> ValidatePath(String path){
		if (ResourceLocation.isValidPath(path))
			return DataResult.success(UnItemify(path));
		else
			return DataResult.error(()->"Invalid character in path: "+path);
	}
}
