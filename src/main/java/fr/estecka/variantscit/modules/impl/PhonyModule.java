package fr.estecka.variantscit.modules.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.util.CodecUtil;
import fr.estecka.variantscit.commands.CommandLogger;
import fr.estecka.variantscit.commands.WalktroughLogger;
import fr.estecka.variantscit.itemdata.transforms.IDataTransform;
import fr.estecka.variantscit.modules.IBakedModule;
import fr.estecka.variantscit.modules.cache.CacheKeySet;
import fr.estecka.variantscit.modules.cache.ComponentCacheKey;
import fr.estecka.variantscit.modules.cache.ECachePolicy;
import fr.estecka.variantscit.modules.libraries.VariantLibrary;
import fr.estecka.variantscit.reload.IUnbakedModule;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record PhonyModule(
	ECachePolicy cachePolicy,
	IDataTransform acceptedVariant,
	List<Identifier> intrinsicModels,
	List<DataComponentType<?>> dependencies,
	Optional<Identifier> returnedModel
)
implements IBakedModule,IUnbakedModule
{

	static private final Codec<ECachePolicy> POLICY_CODEC = CodecUtil.Enum(Codec.STRING, Map.of(
		"avoid",  ECachePolicy.AVOID,
		"always", ECachePolicy.ALWAYS
	));

	static public final MapCodec<PhonyModule> UNBAKED_MAPCODEC = RecordCodecBuilder.mapCodec(builder->
		builder.group(
			POLICY_CODEC.optionalFieldOf("cachePolicy", ECachePolicy.AVOID).forGetter(PhonyModule::cachePolicy),
			IDataTransform.CODEC.optionalFieldOf("acceptsVariant", IDataTransform.NULL).forGetter(PhonyModule::acceptedVariant),
			Identifier.CODEC.listOf().optionalFieldOf("intrinsicModels", List.of()).forGetter(PhonyModule::intrinsicModels),
			BuiltInRegistries.DATA_COMPONENT_TYPE.byNameCodec().listOf().optionalFieldOf("componentTypes", List.of()).forGetter(PhonyModule::dependencies),
			Identifier.CODEC.optionalFieldOf("returnedModel").forGetter(PhonyModule::returnedModel)
		)
		.apply(builder, PhonyModule::new)
	);

	// ## Baking

	@Override
	public boolean AcceptsVariant(Identifier variantId) {
		return returnedModel.map(id -> id.equals(variantId))
			.orElse(IDataTransform.Test(acceptedVariant, variantId))
			;
	}

	@Override
	public IBakedModule Bake(VariantLibrary library) {
		return this;
	}

	@Override
	public Set<Identifier> GetIntrinsicModels() {
		return Set.copyOf(intrinsicModels);
	}

	// ## Caching
	
	@Override
	public ECachePolicy GetCachePolicy() {
		return this.cachePolicy;
	}

	@Override
	public CacheKeySet GetCacheKeys() {
		return ComponentCacheKey.KeysOf(this.dependencies);
	}

	// ## Rendering

	@Override
	public Identifier GetModelForItem(ItemStack stack) {
		return this.returnedModel.orElse(null);
	}

	// ## Debug

	@Override
	public void Summary(CommandLogger logger) {
		logger.Info("Cache policy: {}", CommandLogger.PackData(this.cachePolicy));
		logger.Info("Returned Model: {}", CommandLogger.PackData(this.returnedModel.orElse(null)));
		if (!this.intrinsicModels.isEmpty()){
			logger.Info("Intrinsic Models: ");
			for (Identifier modelId : this.intrinsicModels)
				logger.Info(" • {}", CommandLogger.PackData(modelId));
		}
	}

	@Override
	public Identifier Walkthrough(WalktroughLogger logger, ItemStack stack) {
		this.Summary(logger);
		return this.GetModelForItem(stack);
	}
}
