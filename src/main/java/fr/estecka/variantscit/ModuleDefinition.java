package fr.estecka.variantscit;

import java.util.Map;
import java.util.Optional;
import net.minecraft.util.Identifier;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ModuleDefinition(Identifier type, String variantDirectory, Optional<Identifier> fallbackModel, Map<String,Identifier> specialModels)
{
	static public final MapCodec<ModuleDefinition> CODEC = RecordCodecBuilder.<ModuleDefinition>mapCodec(builder->builder
		.group(
			Identifier.CODEC.fieldOf("type").forGetter(ModuleDefinition::type),
			Codec.STRING.validate(ModuleDefinition::ValidatePath).fieldOf("variantsDir").forGetter(ModuleDefinition::variantDirectory),
			Identifier.CODEC.optionalFieldOf("fallback").forGetter(ModuleDefinition::fallbackModel),
			Codec.unboundedMap(Codec.STRING, Identifier.CODEC).fieldOf("special").orElse(ImmutableMap.<String,Identifier>of()).forGetter(ModuleDefinition::specialModels)
		)
		.apply(builder, ModuleDefinition::new)
	);

	static public DataResult<String> ValidatePath(String path){
		if (Identifier.isPathValid(path))
			return DataResult.success(path);
		else
			return DataResult.error(()->"Invalid character in directory name: "+path);
	}
}
