package fr.estecka.variantscit.format.properties;

import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.format.EStringTransform;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public record ItemTypeProperty(EStringTransform[] transforms)
implements IStringProperty
{
	static public final MapCodec<ItemTypeProperty> MAP_CODEC = EStringTransform.ARRAY_CODEC.fieldOf("transform").orElse(EStringTransform.EMPTY).xmap(ItemTypeProperty::new, ItemTypeProperty::transforms);

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
		return EStringTransform.Transform(transforms, stack.getItem().toString());
	}
}
