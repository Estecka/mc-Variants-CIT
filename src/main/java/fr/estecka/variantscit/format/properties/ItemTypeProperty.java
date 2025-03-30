package fr.estecka.variantscit.format.properties;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public record ItemTypeProperty()
implements IStringProperty
{
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
		return stack.getItem().toString();
	}
}
