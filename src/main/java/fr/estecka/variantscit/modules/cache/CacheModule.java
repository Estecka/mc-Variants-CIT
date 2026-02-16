package fr.estecka.variantscit.modules.cache;

import java.util.Collection;
import fr.estecka.variantscit.modules.IBakedModule;
import fr.estecka.variantscit.modules.cache.MultiPropertyCache.ICacheablePropertySource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class CacheModule
implements IBakedModule
{
	private final MultiPropertyCache cache;
	private final IBakedModule submodule;

	public CacheModule(IBakedModule submodule){
		this.submodule = submodule;
		this.cache = null;
	}

	@Override
	public Collection<ICacheablePropertySource> GetCacheSources() {
		return this.submodule.GetCacheSources();
	}

	@Override
	public ResourceLocation GetModelForItem(ItemStack stack) {
		return this.cache.ComputeIfAbsent(stack, this::RecomputeItemModel);
	}

	private ResourceLocation RecomputeItemModel(ItemStack stack){
		return submodule.GetModelForItem(stack);
	}
}
