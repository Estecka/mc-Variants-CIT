package fr.estecka.variantscit.modules.cache;

import java.util.Collection;
import fr.estecka.variantscit.modules.IBakedModule;
import fr.estecka.variantscit.modules.cache.MultiPropertyCache.ICachableItemProperty;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class CacheModule
implements ICacheableModule
{
	private final MultiPropertyCache cache;
	private final ICacheableModule[] submodules;

	public CacheModule(Collection<ICacheableModule> submodules){
		this.submodules = submodules.toArray(ICacheableModule[]::new);
		this.cache = null;
	}

	@Override
	public Iterable<ICachableItemProperty> GetProperties() {
		return this.cache.GetProperties();
	}

	@Override
	public ResourceLocation GetModelForItem(ItemStack stack) {
		return this.cache.ComputeIfAbsent(stack, this::RecomputeItemModel);
	}

	private ResourceLocation RecomputeItemModel(ItemStack stack){
		for (IBakedModule m : this.submodules){
			ResourceLocation result = m.GetModelForItem(stack);
			if (result != null) return result;
		}

		return null;
	}
}
