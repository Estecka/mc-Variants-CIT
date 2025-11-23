package fr.estecka.variantscit.modules.impl;

import fr.estecka.variantscit.api.ISimpleCitModule;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.InstrumentComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class GoatHornModule
implements ISimpleCitModule
{
	@Override
	public Identifier GetItemVariant(ItemStack stack){
		InstrumentComponent component = stack.get(DataComponentTypes.INSTRUMENT);
		if (component == null)
			return null;

		return component.instrument().getKey().get().getValue();
	}
}
