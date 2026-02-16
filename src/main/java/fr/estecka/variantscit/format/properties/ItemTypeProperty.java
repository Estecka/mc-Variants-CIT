package fr.estecka.variantscit.format.properties;

import fr.estecka.variantscit.modules.cache.MultiPropertyCache.ICachableItemProperty;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record ItemTypeProperty()
implements IStringProperty
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
	public boolean SameSourceAs(ICachableItemProperty other) {
		return this.getClass().equals(other.getClass());
	}

	@Override
	public String GetPropertyString(ItemStack stack){
		return stack.getItem().toString();
	}
}
