package fr.estecka.variantscit.modules;

import fr.estecka.variantscit.UnclampedOverridePredicate;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class PotionLevelPredicate
implements UnclampedOverridePredicate
{
	@Override
	public float unclampedCall(ItemStack stack, ClientWorld world, LivingEntity entity, int seed) {
		PotionContentsComponent potion = stack.get(DataComponentTypes.POTION_CONTENTS);
		if (potion == null || !potion.hasEffects())
			return 0;
		else
			return potion.getEffects().iterator().next().getAmplifier();
	}
}
