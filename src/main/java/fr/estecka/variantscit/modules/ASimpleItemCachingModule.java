package fr.estecka.variantscit.modules;

import java.util.WeakHashMap;
import java.util.function.Predicate;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.api.ISimpleCitModule;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

abstract class ASimpleItemCachingModule
implements ISimpleCitModule
{
	final WeakHashMap<ItemStack, CacheEntry> cache = new WeakHashMap<>();

	protected record CacheEntry(@Nullable Identifier variant, Predicate<ItemStack> isDirty) {}

	@Override
	public final Identifier GetItemVariant(ItemStack stack){
		CacheEntry entry = this.cache.get(stack);

		if (entry == null || entry.isDirty.test(stack)){
			entry = new CacheEntry(this.RecomputeItemVariant(stack), this.GetValidator(stack));
			cache.put(stack, entry);
			VariantsCitMod.LOGGER.warn("Item Cache: [{}] {}", cache.size(), entry.variant);
		}

		return entry.variant;
	}

	/**
	 * Returns a predicate that checks whether the cache for this stack should
	 * be invalidated.
	 */
	public abstract Predicate<ItemStack> GetValidator(ItemStack stack);

	public abstract @Nullable Identifier RecomputeItemVariant(ItemStack stack);
}
