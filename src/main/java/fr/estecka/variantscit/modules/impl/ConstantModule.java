package fr.estecka.variantscit.modules.impl;

import java.util.Map;
import java.util.Optional;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.commands.CommandLogger;
import fr.estecka.variantscit.commands.WalktroughLogger;
import fr.estecka.variantscit.modules.IBakedModule;
import fr.estecka.variantscit.modules.cache.CacheKeySet;
import fr.estecka.variantscit.modules.cache.ECachePolicy;
import fr.estecka.variantscit.modules.libraries.VariantLibrary;
import fr.estecka.variantscit.reload.IUnbakedModule;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record ConstantModule(
	ECachePolicy cachePolicy,
	Optional<ResourceLocation> modelId
)
implements IBakedModule, IUnbakedModule
{
	static private final Codec<ECachePolicy> POLICY_CODEC = CodecUtil.Enum(Codec.STRING, Map.of(
		"avoid",  ECachePolicy.AVOID,
		"always", ECachePolicy.ALWAYS
	));

	static public final MapCodec<ConstantModule> MAPCODEC = RecordCodecBuilder.mapCodec(builder->
		builder.group(
			POLICY_CODEC.optionalFieldOf("cachePolicy", ECachePolicy.AVOID).forGetter(ConstantModule::cachePolicy),
			ResourceLocation.CODEC.optionalFieldOf("modelId").forGetter(ConstantModule::modelId)
		)
		.apply(builder, ConstantModule::new)
	);

	// ## Baking

	@Override
	public IBakedModule Bake(VariantLibrary library) {
		return this;
	}

	@Override
	public boolean AcceptsVariant(ResourceLocation variantId) {
		return variantId.equals(this.modelId.orElse(null));
	}

	// ## Caching
	
	@Override
	public ECachePolicy GetCachePolicy() {
		return this.cachePolicy;
	}

	@Override
	public CacheKeySet GetCacheKeys() {
		return CacheKeySet.Of();
	}

	// ## Rendering

	@Override
	public ResourceLocation GetModelForItem(ItemStack stack) {
		return this.modelId.orElse(null);
	}

	// ## Debug

	@Override
	public void Summary(CommandLogger logger) {
		logger.Info("Cache policy: {}", CommandLogger.PackData(this.cachePolicy));
		this.Dump(logger);
	}
	
	@Override
	public ResourceLocation Walkthrough(WalktroughLogger logger, ItemStack stack) {
		this.Dump(logger);
		return this.GetModelForItem(stack);
	}
	
	@Override
	public void Dump(CommandLogger logger) {
		logger.Info("Model ID: {}", CommandLogger.PackData(this.modelId.orElse(null)));
	}
}
