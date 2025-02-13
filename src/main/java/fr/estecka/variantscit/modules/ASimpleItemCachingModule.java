package fr.estecka.variantscit.modules;

import java.util.WeakHashMap;
import java.util.function.Predicate;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.api.ISimpleCitModule;
import net.minecraft.item.Item;
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
			entry = new CacheEntry(this.RecomputeItemVariant(stack), this.IsDirty(stack).and(ItemTypeValidator(stack)));
			cache.put(stack, entry);
			VariantsCitMod.LOGGER.warn("Item Cache: [{}] {}", cache.size(), entry.variant);
		}

		return entry.variant;
	}

	static private Predicate<ItemStack> ItemTypeValidator(ItemStack stack){
		final Item type =  stack.getItem();
		return futureStack -> futureStack.getItem() == type;
	}

	/**
	 * @param stack An item stack, in the state it was the last time its variant
	 * was computed.
	 * @return A predicate that checks whether an item stack's variant should be
	 * recomputed. Occasional  false-positives  are  tolerated; accuracy  can be
	 * traded off for performance.
	 */
	public abstract Predicate<ItemStack> IsDirty(ItemStack stack);

	public abstract @Nullable Identifier RecomputeItemVariant(ItemStack stack);
}
