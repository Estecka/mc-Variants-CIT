package fr.estecka.variantscit.modules;

import fr.estecka.variantscit.api.ISimpleCitModule;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class PotionEffectModule
implements ISimpleCitModule
{
	@Override
	public Identifier GetItemVariant(ItemStack stack){
		PotionContentsComponent potion = stack.get(DataComponentTypes.POTION_CONTENTS);

		if (potion == null || !potion.hasEffects())
			return null;

		return potion.getEffects().iterator().next().getEffectType().getKey().get().getValue();
	}
}
