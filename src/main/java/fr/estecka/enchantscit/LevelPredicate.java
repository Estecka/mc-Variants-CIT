package fr.estecka.enchantscit;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.item.ClampedModelPredicateProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

/**
 * Minecraft  seems  to fully  support (and use) unclamped  predicates, but only
 * exposes  the  methods to register clamped  ones... so in order to register an
 * unclamped  predicate, I still  need  to  implement the clamped interface, and
 * override the clamp operation.
 * 
 * See: {@link net.minecraft.client.item.ModelPredicateProviderRegistry#register}
 */
public class LevelPredicate
implements ClampedModelPredicateProvider
{
	@Override
	public float call(ItemStack stack, @Nullable ClientWorld world, LivingEntity entity, int seed) {
		ItemEnchantmentsComponent enchants = stack.get(DataComponentTypes.STORED_ENCHANTMENTS);
		if (enchants == null || enchants.isEmpty())
			return 0;
		else
			return enchants.getEnchantmentEntries().iterator().next().getIntValue();
	}
	
	@Override
	public float unclampedCall(ItemStack stack, @Nullable ClientWorld world, LivingEntity entity, int seed){
		return call(stack, world, entity, seed);
	}
}
