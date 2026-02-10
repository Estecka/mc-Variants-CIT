package fr.estecka.variantscit.modules.impl;

import fr.estecka.variantscit.api.ISimpleCitModule;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.ItemStack;

public class GoatHornModule
implements ISimpleCitModule
{
	@Override
	public ResourceLocation GetItemVariant(ItemStack stack){
		Holder<Instrument> component = stack.get(DataComponents.INSTRUMENT);
		if (component == null)
			return null;

		return component.unwrapKey().get().location();
	}
}
