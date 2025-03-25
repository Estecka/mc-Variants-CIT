package fr.estecka.variantscit.format.properties;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.format.ETransform;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public record ItemTypeProperty(ETransform[] transforms)
implements IStringProperty
{
	static public final MapCodec<ItemTypeProperty> MAP_CODEC = RecordCodecBuilder.mapCodec(instance->
		instance.group(
			ETransform.ARRAY_CODEC.fieldOf("transform").orElse(ETransform.EMPTY).forGetter(ItemTypeProperty::transforms)
		)
		.apply(instance, ItemTypeProperty::new)
	);

	@Override
	public int GetPropertyHash(ItemStack stack){
		return stack.getItem().hashCode();
	}

	@Override
	public Item GetReference(ItemStack stack) {
		return stack.getItem();
	}

	@Override
	public String GetPropertyString(ItemStack stack){
		return ETransform.Transform(transforms, stack.getItem().toString());
	}
}
