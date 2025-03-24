package fr.estecka.variantscit.format.properties;

import net.minecraft.item.ItemStack;

public class ItemCountProperty
implements IStringProperty
{
	@Override
	public int GetPropertyHash(ItemStack stack){
		return stack.getCount();
	}

	@Override
	public Object GetReference(ItemStack stack) {
		return null;
	}

	@Override
	public String GetPropertyString(ItemStack stack){
		return String.valueOf(stack.getCount());
	}
}
