package fr.estecka.variantscit.format.properties;

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
	public int SourceHashcode() {
		return this.getClass().hashCode();
	}

	@Override
	public String GetPropertyString(ItemStack stack){
		return stack.getItem().toString();
	}
}
