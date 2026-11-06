package fr.estecka.variantscit.modules.impl;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.modules.cache.CacheKeySet;
import fr.estecka.variantscit.modules.cache.ECachePolicy;
import fr.estecka.variantscit.modules.libraries.ISimpleCitModule;
import fr.estecka.variantscit.modules.libraries.IVariantLibrary;
import fr.estecka.variantscit.util.CodecUtil;
import fr.estecka.variantscit.commands.CommandLogger;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;
import fr.estecka.variantscit.itemdata.extractors.IDataExtractor;
import fr.estecka.variantscit.itemdata.extractors.TransformableExtractor;
import fr.estecka.variantscit.itemdata.transforms.impl.LogTransform;


public record ComponentDataModule<P extends IDataExtractor>(
	P itemProperty,
	Optional<String> namespace
)
implements ISimpleCitModule
{
	static public final MapCodec<ComponentDataModule<?>> MAPCODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			IDataExtractor.MAPCODEC.forGetter(o->o.itemProperty),
			CodecUtil.IDENTIFIER_NAMESPACE.optionalFieldOf("namespace").forGetter(o->o.namespace)
		)
		.apply(builder, ComponentDataModule::new)
	);

	@Override
	public CacheKeySet GetCacheKeys() {
		return itemProperty.GetCacheKeys();
	}

	@Override
	public ECachePolicy GetCachePolicy() {
		return ECachePolicy.ALWAYS;
	}

	@Override
	public boolean AcceptsVariant(Identifier variantId) {
		return namespace.isEmpty() || variantId.getNamespace().equals(namespace.get());
	}

	@Override
	public @Nullable Identifier GetItemVariant(ItemStack stack) {
		IDataContainer result = this.itemProperty.Extract(stack);
		if (result == null)
			return null;
		else if (namespace.isEmpty())
			return result.asIdentifier();
		else if (result.asString() instanceof String string)
			return Identifier.tryBuild(namespace.get(), string);
		else
			return null;
	}

	@Override
	public @Nullable Identifier Walkthrough(ItemStack stack, IVariantLibrary library, CommandLogger logger) {
		IDataContainer raw = TransformableExtractor.Unwrap(this.itemProperty).Extract(stack);
		logger.Info("Raw data: {}",    CommandLogger.ItemData(raw, "Missing or invalid"));

		IDataContainer transformed = LogTransform.WithLogger(logger, ()->itemProperty.Extract(stack));
		logger.Info("Transformed: {}", CommandLogger.ItemData(transformed));

		if (namespace.isPresent() && transformed.asString() instanceof String path)
			logger.Info("Namespaced: {}", CommandLogger.ItemData(namespace.get()+":"+path));

		return this.GetItemModel(stack, library);
	}
}
