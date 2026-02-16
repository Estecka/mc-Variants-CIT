package fr.estecka.variantscit.modules.impl;

import fr.estecka.variantscit.modules.libraries.ISimpleCitModule;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;

public class PotionTypeModule
implements ISimpleCitModule
{
	@Override
	public ResourceLocation GetItemVariant(ItemStack stack){
		PotionContents potionComponent = stack.get(DataComponents.POTION_CONTENTS);
		if (potionComponent == null || potionComponent.potion().isEmpty())
			return null;

		return potionComponent.potion().get().unwrapKey().get().location();
	}
}
