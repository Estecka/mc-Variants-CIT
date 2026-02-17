package fr.estecka.variantscit.format.properties;

import fr.estecka.variantscit.modules.cache.ICacheKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record ItemTypeProperty()
implements IStringProperty, ICacheKey
{
	static public final ItemTypeProperty UNIT = new ItemTypeProperty();

	@Override
	public Item GetReference(ItemStack stack) {
		return stack.getItem();
	}

	@Override
	public ICacheKey GetCacheKey() {
		return this;
	}

	@Override
	public String GetPropertyString(ItemStack stack){
		return stack.getItem().toString();
	}
}
