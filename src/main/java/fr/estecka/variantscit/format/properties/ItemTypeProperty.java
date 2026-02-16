package fr.estecka.variantscit.format.properties;

import fr.estecka.variantscit.modules.cache.MultiPropertyCache.ICacheablePropertySource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record ItemTypeProperty()
implements IStringProperty, ICacheablePropertySource
{
	@Override
	public int GetPropertyHash(ItemStack stack){
		return stack.getItem().hashCode();
	}

	@Override
	public Item GetReference(ItemStack stack) {
		return stack.getItem();
	}

	@Override
	public ICacheablePropertySource GetSource() {
		return this;
	}

	@Override
	public String GetPropertyString(ItemStack stack){
		return stack.getItem().toString();
	}
}
