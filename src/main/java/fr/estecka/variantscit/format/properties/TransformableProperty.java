package fr.estecka.variantscit.format.properties;

import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.format.IStringTransform;
import fr.estecka.variantscit.format.transforms.SuccessiveTransform;
import fr.estecka.variantscit.modules.cache.MultiPropertyCache.ICachableItemProperty;

public record TransformableProperty<T extends IStringProperty>(T inner, IStringTransform transform, Optional<String> fallback)
implements IStringProperty
{
	static public <T extends IStringProperty> MapCodec<TransformableProperty<T>> CodecOf(MapCodec<T> inner){
		return RecordCodecBuilder.<TransformableProperty<T>>mapCodec(builder->
			builder.group(
				inner.forGetter(TransformableProperty::inner),
				SuccessiveTransform.CODEC.optionalFieldOf("transform", IStringTransform.NOOP).forGetter(TransformableProperty::transform),
				Codec.STRING.optionalFieldOf("fallback").forGetter(TransformableProperty::fallback)
			).apply(builder, TransformableProperty::new)
		);
	}

	@Override
	public int GetPropertyHash(ItemStack stack) {
		return inner.GetPropertyHash(stack);
	}

	@Override
	public Object GetReference(ItemStack stack) {
		return inner.GetReference(stack);
	}

	@Override
	public boolean SameSourceAs(ICachableItemProperty other) {
		return inner.SameSourceAs(other);
	}

	@Override
	public String GetPropertyString(ItemStack stack) {
		String result = inner.GetPropertyString(stack);

		if (result != null)
			result = transform.apply(result);

		if (result == null)
			result = fallback.orElse(null);

		return result;
	}

	static public IStringProperty GetRaw(IStringProperty property){
		while (property instanceof TransformableProperty<?> transformable)
			property = transformable.inner;
		return property;
	}
}
