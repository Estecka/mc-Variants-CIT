package fr.estecka.variantscit.format.properties;

import fr.estecka.variantscit.modules.cache.MultiPropertyCache.ICacheablePropertySource;
import net.minecraft.world.item.ItemStack;

public class ItemCountProperty
implements IStringProperty, ICacheablePropertySource
{
	static public final ItemCountProperty UNIT = new ItemCountProperty();

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
	public ItemCountProperty GetSource() {
		return this;
	}

	@Override
	public String GetPropertyString(ItemStack stack){
		return String.valueOf(stack.getCount());
	}
}
