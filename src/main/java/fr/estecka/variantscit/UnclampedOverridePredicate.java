package fr.estecka.variantscit;

import org.jetbrains.annotations.Nullable;
import net.minecraft.client.item.ClampedModelPredicateProvider;
import net.minecraft.client.world.ClientWorld;
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
public interface UnclampedOverridePredicate
extends ClampedModelPredicateProvider
{

	@Override
	public default float call(ItemStack stack, @Nullable ClientWorld world, LivingEntity entity, int seed) {
		return unclampedCall(stack, world, entity, seed);
	}
}
