package fr.estecka.variantscit.format.properties;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.format.EStringTransform;
import net.minecraft.item.ItemStack;

public record TransformableProperty<T extends IStringProperty>(T inner, EStringTransform[] transform)
implements IStringProperty
{
	static public <T extends IStringProperty> MapCodec<TransformableProperty<T>> CodecOf(MapCodec<T> inner){
		return RecordCodecBuilder.<TransformableProperty<T>>mapCodec(builder->
			builder.group(
				inner.forGetter(TransformableProperty::inner),
				EStringTransform.ARRAY_CODEC.fieldOf("transform").orElse(EStringTransform.EMPTY).forGetter(TransformableProperty::transform)
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
			return EStringTransform.Transform(transform, result);
		else
			return null;
	}
}
