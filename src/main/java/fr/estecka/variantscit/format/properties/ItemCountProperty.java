package fr.estecka.variantscit.format.properties;

import net.minecraft.world.item.ItemStack;

public class ItemCountProperty
implements IStringProperty
{
	@Override
	public int GetPropertyHash(ItemStack stack){
		return stack.getCount();
	}

	// TODO: Find a way to not return null. This is the only property that returns null on valid properties.
	@Override
	public Object GetReference(ItemStack stack) {
		return null;
	}

	@Override
	public int SourceHashcode() {
		return this.getClass().hashCode();
	}

	@Override
	public String GetPropertyString(ItemStack stack){
		return String.valueOf(stack.getCount());
	}
}
