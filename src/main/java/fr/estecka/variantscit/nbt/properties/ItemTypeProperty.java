package fr.estecka.variantscit.nbt.properties;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;

public class ItemTypeProperty
implements INbtProperty
{
	public int getPropertyHash(ItemStack stack){
		return stack.getItem().hashCode();
	}

	public NbtElement getPropertyNbt(ItemStack stack){
		return Item.ENTRY_CODEC.encodeStart(NbtOps.INSTANCE, stack.getRegistryEntry()).getOrThrow();
	}
}
