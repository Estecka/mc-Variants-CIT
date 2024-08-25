package fr.estecka.variantscit;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import net.fabricmc.fabric.impl.client.model.loading.ModelLoadingConstants;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ModuleDefinition(Identifier type, String modelPrefix, Optional<Identifier> fallbackModel, Map<String,Identifier> specialModels)
{
	static public final MapCodec<ModuleDefinition> CODEC = RecordCodecBuilder.<ModuleDefinition>mapCodec(builder->builder
		.group(
			Identifier.CODEC.fieldOf("type").forGetter(ModuleDefinition::type),
			Codec.STRING.validate(ModuleDefinition::ValidatePath).fieldOf("modelPrefix").forGetter(ModuleDefinition::modelPrefix),
			Identifier.CODEC.optionalFieldOf("fallback").forGetter(ModuleDefinition::fallbackModel),
			Codec.unboundedMap(Codec.STRING, Identifier.CODEC).fieldOf("special").orElse(ImmutableMap.<String,Identifier>of()).forGetter(ModuleDefinition::specialModels)
		)
		.apply(builder, ModuleDefinition::new)
	);

	static public DataResult<String> ValidatePath(String path){
		if (Identifier.isPathValid(path))
			return DataResult.success(path);
		else
			return DataResult.error(()->"Invalid character in path: "+path);
	}

	public @Nullable ModelIdentifier GetFallbackModelId(){
		return fallbackModel.map(ModelLoadingConstants::toResourceModelId).orElse(null);
	}

	public Map<String, @Nullable ModelIdentifier> GetSpecialModelIds(){
		Map<String, @Nullable ModelIdentifier> result = new HashMap<>();
		for (var entry : this.specialModels.entrySet())
			result.put(entry.getKey(), ModelLoadingConstants.toResourceModelId(entry.getValue()));
		return result;
	}
}
