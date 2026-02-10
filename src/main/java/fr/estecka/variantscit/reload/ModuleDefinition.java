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

public record ModuleDefinition(
	@Deprecated ResourceLocation type,
	UnbakedModule<?> parameters,
	List<EModuleContext> contexts,
	Optional<List<ResourceLocation>> targets,
	int priority,
	String modelPrefix,
	boolean itemGen,
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
			CodecUtil.OneOrMany(EModuleContext.CODEC).optionalFieldOf("context", List.of(EModuleContext.ITEM_MODEL)).forGetter(ModuleDefinition::contexts),
			CodecUtil.OneOrMany(ResourceLocation.CODEC).optionalFieldOf("items").forGetter(ModuleDefinition::targets),
			Codec.INT.fieldOf("priority").orElse(0).forGetter(ModuleDefinition::priority),
			Codec.STRING.validate(ModuleDefinition::ValidatePath).fieldOf("modelPrefix").forGetter(ModuleDefinition::modelPrefix),
			Codec.BOOL.fieldOf("itemsFromModels").orElse(true).forGetter(ModuleDefinition::itemGen),
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
