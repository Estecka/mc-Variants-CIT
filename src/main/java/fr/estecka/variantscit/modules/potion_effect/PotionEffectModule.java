package fr.estecka.variantscit.modules.potion_effect;

import fr.estecka.variantscit.ModuleDefinition;
import fr.estecka.variantscit.api.ACitModule;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class PotionEffectModule
extends ACitModule
{
	public PotionEffectModule(ModuleDefinition definition){
		super(definition);
	}

	@Override
	public Identifier GetItemVariant(ItemStack stack){
		PotionContentsComponent potion = stack.get(DataComponentTypes.POTION_CONTENTS);

		if (potion == null || !potion.hasEffects())
			return null;

		return potion.getEffects().iterator().next().getEffectType().getKey().get().getValue();
	}
}
