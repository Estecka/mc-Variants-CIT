package fr.estecka.variantscit.modules;

import fr.estecka.variantscit.api.ISimpleCitModule;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Instrument;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class GoatHornModule
implements ISimpleCitModule
{
	@Override
	public Identifier GetItemVariant(ItemStack stack){
		RegistryEntry<Instrument> component = stack.get(DataComponentTypes.INSTRUMENT);
		if (component == null)
			return null;

		return component.getKey().get().getValue();
	}
}
