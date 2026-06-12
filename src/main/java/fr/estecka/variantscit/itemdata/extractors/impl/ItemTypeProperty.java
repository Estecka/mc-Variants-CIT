package fr.estecka.variantscit.itemdata.extractors.impl;

import fr.estecka.variantscit.itemdata.containers.IDataContainer;
import fr.estecka.variantscit.itemdata.containers.RawDataContainer;
import fr.estecka.variantscit.itemdata.extractors.IDataExtractor;
import fr.estecka.variantscit.modules.cache.ICacheKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ItemTypeProperty
implements IDataExtractor, ICacheKey
{
	static public final ItemTypeProperty UNIT = new ItemTypeProperty();

	private ItemTypeProperty(){}

	@Override
	public Item GetReference(ItemStack stack) {
		return stack.getItem();
	}

	@Override
	public ICacheKey GetCacheKey() {
		return this;
	}

	@Override
	public IDataContainer Extract(ItemStack stack) {
		return RawDataContainer.<String>OfNullable(stack.getItem().toString());
	}

	@Override
	public final String toString() {
		return "#item_type";
	}
}
