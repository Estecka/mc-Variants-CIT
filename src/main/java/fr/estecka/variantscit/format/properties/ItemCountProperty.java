package fr.estecka.variantscit.format.properties;

import net.minecraft.item.ItemStack;

public class ItemCountProperty
implements IStringProperty
{
	public int GetPropertyHash(ItemStack stack){
		return stack.getCount();
	}

	public String GetPropertyString(ItemStack stack){
		return String.valueOf(stack.getCount());
	}
}
