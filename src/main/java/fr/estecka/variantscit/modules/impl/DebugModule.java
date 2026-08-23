package fr.estecka.variantscit.modules.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.commands.CommandLogger;
import fr.estecka.variantscit.commands.WalktroughLogger;
import fr.estecka.variantscit.itemdata.transforms.IDataTransform;
import fr.estecka.variantscit.modules.IBakedModule;
import fr.estecka.variantscit.modules.cache.CacheKeySet;
import fr.estecka.variantscit.modules.cache.ECachePolicy;
import fr.estecka.variantscit.modules.libraries.VariantLibrary;
import fr.estecka.variantscit.reload.IUnbakedModule;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record DebugModule(
	VariantLibrary library,
	Unbaked params
)
implements IBakedModule
{
	static private record Unbaked(
		ECachePolicy cachePolicy,
		IDataTransform acceptedVariant,
		List<Identifier> intrinsicModels,
		Optional<Identifier> returnedModel
	)
	implements IUnbakedModule
	{
		@Override
		public boolean AcceptsVariant(Identifier variantId) {
			return returnedModel.map(id -> id.equals(variantId))
			    .orElse(IDataTransform.Test(acceptedVariant, variantId))
			    ;
		}

		@Override
		public IBakedModule Bake(VariantLibrary library) {
			return new DebugModule(library, this);
		}

		@Override
		public Set<Identifier> GetIntrinsicModels() {
			return Set.copyOf(intrinsicModels);
		}
	}

	static private final Codec<ECachePolicy> POLICY_CODEC = CodecUtil.Enum(Codec.STRING, Map.of(
		"avoid",  ECachePolicy.AVOID,
		"always", ECachePolicy.ALWAYS
	));

	static public final MapCodec<Unbaked> UNBAKED_MAPCODEC = RecordCodecBuilder.mapCodec(builder->
		builder.group(
			POLICY_CODEC.optionalFieldOf("cachePolicy", ECachePolicy.AVOID).forGetter(Unbaked::cachePolicy),
			IDataTransform.CODEC.optionalFieldOf("acceptsVariant", IDataTransform.NULL).forGetter(Unbaked::acceptedVariant),
			Identifier.CODEC.listOf().optionalFieldOf("intrinsicModels", List.of()).forGetter(Unbaked::intrinsicModels),
			Identifier.CODEC.optionalFieldOf("returnedModel").forGetter(Unbaked::returnedModel)
		)
		.apply(builder, Unbaked::new)
	);

	// ## Caching
	
	@Override
	public ECachePolicy GetCachePolicy() {
		return params.cachePolicy;
	}

	@Override
	public CacheKeySet GetCacheKeys() {
		return CacheKeySet.Of();
	}

	// ## Rendering

	@Override
	public Identifier GetModelForItem(ItemStack stack) {
		return params.returnedModel.orElse(null);
	}

	// ## Debug

	@Override
	public void Summary(CommandLogger logger) {
		logger.Info("Cache policy: {}", CommandLogger.PackData(params.cachePolicy));
		logger.Info("Returned Model: {}", CommandLogger.PackData(params.returnedModel.orElse(null)));
		if (!params.intrinsicModels.isEmpty()){
			logger.Info("Intrinsic Models: ");
			for (Identifier modelId : params.intrinsicModels)
				logger.Info(" • {}", CommandLogger.PackData(modelId));
		}
	}

	@Override
	public Identifier Walkthrough(WalktroughLogger logger, ItemStack stack) {
		this.Summary(logger);
		return this.GetModelForItem(stack);
	}
}
