package fr.estecka.variantscit.itemdata.extractors;

import java.util.Optional;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;
import fr.estecka.variantscit.itemdata.containers.RawDataContainer;
import fr.estecka.variantscit.itemdata.transforms.IDataTransform;
import fr.estecka.variantscit.itemdata.transforms.SuccessiveTransform;
import fr.estecka.variantscit.modules.cache.ICacheKey;

public record TransformableExtractor<T extends IDataExtractor>(T inner, IDataTransform transform, Optional<RawDataContainer<Tag>> fallback)
implements IDataExtractor
{
	static public <T extends IDataExtractor> MapCodec<TransformableExtractor<T>> CodecOf(MapCodec<T> innerCodec){
		return RecordCodecBuilder.<TransformableExtractor<T>>mapCodec(builder->
			builder.group(
				innerCodec.forGetter(TransformableExtractor::inner),
				SuccessiveTransform.CODEC.optionalFieldOf("transform", IDataTransform.NOOP).forGetter(TransformableExtractor::transform),
				RawDataContainer.LITTERAL_CODEC.optionalFieldOf("fallback").forGetter(TransformableExtractor::fallback)
			).apply(builder, TransformableExtractor::new)
		);
	}

	@Override
	public ICacheKey GetCacheKey() {
		return inner.GetCacheKey();
	}

	@Override
	public IDataContainer Extract(ItemStack stack) {
		IDataContainer result = inner.Extract(stack);

		if (result != null)
			result = transform.LooseTypedTransform(result);

		if (result == null)
			result = fallback.orElse(null);

		return result;
	}
}
