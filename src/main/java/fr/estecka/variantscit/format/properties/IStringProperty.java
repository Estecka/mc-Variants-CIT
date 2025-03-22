package fr.estecka.variantscit.format.properties;

import net.minecraft.item.ItemStack;

public interface IStringProperty
{
	int GetPropertyHash(ItemStack stack);
	String GetPropertyString(ItemStack stack);
}
