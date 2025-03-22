package fr.estecka.variantscit.format.properties;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.format.ETransform;
import net.minecraft.item.ItemStack;

public record ItemTypeProperty(ETransform[] transforms)
implements IStringProperty
{
	static public final Codec<ItemTypeProperty> CODEC = RecordCodecBuilder.create(instance->
		instance.group(
			ETransform.ARRAY_CODEC.fieldOf("transform").orElse(new ETransform[0]).forGetter(ItemTypeProperty::transforms)
		)
		.apply(instance, ItemTypeProperty::new)
	);
	

	public int GetPropertyHash(ItemStack stack){
		return stack.getItem().hashCode();
	}

	public String GetPropertyString(ItemStack stack){
		return stack.getItem().toString();
	}
}
