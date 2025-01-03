package fr.estecka.variantscit.modules;

import fr.estecka.variantscit.api.ISimpleCitModule;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.util.Identifier;

public class TrimPatternModule
implements ISimpleCitModule
{
	@Override
	public Identifier GetItemVariant(ItemStack stack){
		ArmorTrim trim = stack.get(DataComponentTypes.TRIM);
		if (trim == null)
			return null;

		return trim.getPattern().getKey().get().getValue();
	}
}
