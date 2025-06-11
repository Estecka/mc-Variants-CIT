package fr.estecka.variantscit.format.properties;

import java.util.Optional;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.format.IStringTransform;
import net.minecraft.item.ItemStack;

public record TransformableProperty<T extends IStringProperty>(T inner, IStringTransform[] transform, Optional<String> fallback)
implements IStringProperty
{
	static public <T extends IStringProperty> MapCodec<TransformableProperty<T>> CodecOf(MapCodec<T> inner){
		return RecordCodecBuilder.<TransformableProperty<T>>mapCodec(builder->
			builder.group(
				inner.forGetter(TransformableProperty::inner),
				IStringTransform.ARRAY_CODEC.optionalFieldOf("transform", IStringTransform.AUTO).forGetter(TransformableProperty::transform),
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
	public String GetPropertyString(ItemStack stack) {
		String result = inner.GetPropertyString(stack);

		if (result != null)
			result = IStringTransform.Transform(transform, result);

		if (result == null)
			result = fallback.orElse(null);

		return result;
	}
}
