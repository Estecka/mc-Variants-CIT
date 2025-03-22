package fr.estecka.variantscit.nbt.properties;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;

public interface INbtProperty
{
	int getPropertyHash(ItemStack stack);
	NbtElement getPropertyNbt(ItemStack stack);
}
