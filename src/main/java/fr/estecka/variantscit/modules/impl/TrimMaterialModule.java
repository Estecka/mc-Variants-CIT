package fr.estecka.variantscit.modules.impl;

import fr.estecka.variantscit.api.ISimpleCitModule;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

public class TrimMaterialModule
implements ISimpleCitModule
{
	@Override
	public ResourceLocation GetItemVariant(ItemStack stack){
		ArmorTrim trim = stack.get(DataComponents.TRIM);
		if (trim == null)
			return null;

		return trim.material().unwrapKey().get().location();
	}
}
