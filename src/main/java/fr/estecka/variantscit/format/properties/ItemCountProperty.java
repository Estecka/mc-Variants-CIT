package fr.estecka.variantscit.format.properties;

import fr.estecka.variantscit.modules.cache.ICacheKey;
import net.minecraft.world.item.ItemStack;

public class ItemCountProperty
implements IStringProperty, ICacheKey
{
	static public final ItemCountProperty UNIT = new ItemCountProperty();

	@Override
	public int GetPropertyHash(ItemStack stack){
		return stack.getCount();
	}

	/**
	 * TODO:  Find a way  to not return  null. This is  the only  property  that
	 * returns null on valid properties.
	 */
	@Override
	public Object GetReference(ItemStack stack) {
		return null;
	}

	@Override
	public ItemCountProperty GetCacheKey() {
		return this;
	}

	@Override
	public String GetPropertyString(ItemStack stack){
		return String.valueOf(stack.getCount());
	}

	@Override
	public String toString() {
		return "#item_count";
	}
}
