package fr.estecka.variantscit.itemdata.extractors.impl;

import fr.estecka.variantscit.itemdata.containers.IDataContainer;
import fr.estecka.variantscit.itemdata.containers.RawDataContainer;
import fr.estecka.variantscit.itemdata.extractors.IDataExtractor;
import fr.estecka.variantscit.modules.cache.ICacheKey;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public record DisplayNameProperty()
implements IDataExtractor, ICacheKey
{
	static public final DisplayNameProperty UNIT = new DisplayNameProperty();

	@Override
	public ICacheKey GetCacheKey() {
		return this;
	}

	@Override
	public Object GetReference(ItemStack stack) {
		return stack.getHoverName();
	}

	@Override
	public IDataContainer Extract(ItemStack stack) {
		return RawDataContainer.<Component>OfNullable(stack.getHoverName());
	}

	@Override
	public final String toString() {
		return "#display_name";
	}
}
