package fr.estecka.variantscit.modules.impl;

import fr.estecka.variantscit.modules.libraries.ISimpleCitModule;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;

public class PotionEffectModule
implements ISimpleCitModule
{
	@Override
	public ResourceLocation GetItemVariant(ItemStack stack){
		PotionContents potion = stack.get(DataComponents.POTION_CONTENTS);

		if (potion == null || !potion.hasEffects())
			return null;

		return potion.getAllEffects().iterator().next().getEffect().unwrapKey().get().location();
	}
}
