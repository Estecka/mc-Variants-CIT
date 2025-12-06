package fr.estecka.variantscit.reload;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.Identifier;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.VCitRegistries;

public record ModuleDefinition(
	@Deprecated Identifier type,
	UnbakedModule<?> parameters,
	List<EModuleContext> contexts,
	Optional<List<Identifier>> targets,
	int priority,
	String modelPrefix,
	boolean itemGen,
	Optional<Identifier> modelParent,
	List<Identifier> assetGen,
	Optional<Identifier> fallbackModel,
	Map<String,Identifier> specialModels
)
{
	static public final MapCodec<ModuleDefinition> CODEC = RecordCodecBuilder.<ModuleDefinition>mapCodec(builder->builder
		.group(
			Identifier.CODEC.fieldOf("type").forGetter(ModuleDefinition::type),
			VCitRegistries.MODULES.mapCodec.forGetter(ModuleDefinition::parameters),
			CodecUtil.OneOrMany(EModuleContext.CODEC).optionalFieldOf("context", List.of(EModuleContext.ITEM_MODEL)).forGetter(ModuleDefinition::contexts),
			CodecUtil.OneOrMany(Identifier.CODEC).optionalFieldOf("items").forGetter(ModuleDefinition::targets),
			Codec.INT.fieldOf("priority").orElse(0).forGetter(ModuleDefinition::priority),
			Codec.STRING.validate(ModuleDefinition::ValidatePath).fieldOf("modelPrefix").forGetter(ModuleDefinition::modelPrefix),
			Codec.BOOL.fieldOf("itemsFromModels").orElse(true).forGetter(ModuleDefinition::itemGen),
			Identifier.CODEC.optionalFieldOf("modelParent").forGetter(ModuleDefinition::fallbackModel),
			Identifier.CODEC.listOf().optionalFieldOf("assetGen", List.of()).forGetter(ModuleDefinition::assetGen),
			Identifier.CODEC.validate(ModuleDefinition::UnItemify).optionalFieldOf("fallback").forGetter(ModuleDefinition::fallbackModel),
			Codec.unboundedMap(Codec.STRING, Identifier.CODEC.validate(ModuleDefinition::UnItemify)).optionalFieldOf("special", ImmutableMap.<String,Identifier>of()).forGetter(ModuleDefinition::specialModels)
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

	static private DataResult<Identifier> UnItemify(Identifier original){
		return DataResult.success(Identifier.of(original.getNamespace(), UnItemify(original.getPath())));
	}

	static public DataResult<String> ValidatePath(String path){
		if (Identifier.isPathValid(path))
			return DataResult.success(UnItemify(path));
		else
			return DataResult.error(()->"Invalid character in path: "+path);
	}
}
