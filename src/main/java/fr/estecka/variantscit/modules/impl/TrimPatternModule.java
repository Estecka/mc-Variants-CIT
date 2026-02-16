package fr.estecka.variantscit.modules.impl;

import fr.estecka.variantscit.modules.libraries.ISimpleCitModule;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

public class TrimPatternModule
implements ISimpleCitModule
{
	@Override
	public ResourceLocation GetItemVariant(ItemStack stack){
		ArmorTrim trim = stack.get(DataComponents.TRIM);
		if (trim == null)
			return null;

		return trim.pattern().unwrapKey().get().location();
	}
}
