package fr.estecka.variantscit.modules;

import java.util.ArrayList;
import java.util.Collection;
import fr.estecka.variantscit.modules.cache.ECachePolicy;
import fr.estecka.variantscit.modules.cache.CacheKeySet;
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
	public CacheKeySet GetCacheKeys() {
		return CacheKeySet.OfCacheables(this);
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

	@Override
	public ECachePolicy GetCachePolicy() {
		return ECachePolicy.UNWRAP;
	}
}
