package fr.estecka.variantscit.nbt.properties;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;

public class ItemCountProperty
implements INbtProperty
{
	public int getPropertyHash(ItemStack stack){
		return stack.getCount();
	}

	public NbtElement getPropertyNbt(ItemStack stack){
		return NbtInt.of(stack.getCount());
	}
}
