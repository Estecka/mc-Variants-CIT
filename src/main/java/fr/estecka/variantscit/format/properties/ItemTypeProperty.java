package fr.estecka.variantscit.format.properties;

import net.minecraft.item.ItemStack;

public class ItemTypeProperty
implements IStringProperty
{
	public int GetPropertyHash(ItemStack stack){
		return stack.getItem().hashCode();
	}

	public String GetPropertyString(ItemStack stack){
		return stack.getItem().toString();
	}
}
