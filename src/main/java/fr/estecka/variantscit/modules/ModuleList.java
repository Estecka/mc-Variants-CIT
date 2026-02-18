package fr.estecka.variantscit.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import fr.estecka.variantscit.modules.cache.ICacheKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * TODO: Unwrap inner lists
 */
public class ModuleList
extends ArrayList<IBakedModule>
implements IBakedModule
{
	public ModuleList(Collection<? extends IBakedModule> submodules){
		super(submodules);
	}

	@Override
	public ResourceLocation GetModelForItem(ItemStack stack) {
		for(IBakedModule m : this){
			ResourceLocation result = m.GetModelForItem(stack);
			if (result != null) return result;
		}
		return null;
	}

	@Override
	public Collection<ICacheKey> GetCacheKeys() {
		Set<ICacheKey> set = new HashSet<>();
		for(IBakedModule m : this)
			set.addAll(m.GetCacheKeys());
		return set;
	}

	@Override
	public boolean add(IBakedModule module) {
		if (module instanceof ModuleList list)
			return this.addAll(list);
		else
			return super.add(module);
	}

	@Override
	public boolean addAll(Collection<? extends IBakedModule> list) {
		boolean r = false;
		for (IBakedModule module : list)
			r |= this.add(module);
		return r;
	}
}
