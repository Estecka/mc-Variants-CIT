package fr.estecka.variantscit.modules.cache;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import fr.estecka.variantscit.collections.BiMap;
import fr.estecka.variantscit.collections.TriMap;
import fr.estecka.variantscit.modules.IBakedModule;
import fr.estecka.variantscit.modules.IModuleWrapper;
import fr.estecka.variantscit.modules.ModuleList;
import fr.estecka.variantscit.reload.EModuleHook;
import net.minecraft.world.item.Item;

public abstract class CacheBuilder
{
	static public BiMap<EModuleHook,Item,IBakedModule> BuildAll(TriMap<EModuleHook,Item,Integer,List<IBakedModule>> sortedModules){
		BiMap<EModuleHook,Item,IBakedModule> result = new BiMap<>();

		for (EModuleHook hook : sortedModules.keySet())
		for (Item item : sortedModules.get(hook).keySet())
		{
			IBakedModule baked = BuildItem(sortedModules.get(hook, item));
			result.put(hook, item, baked);
		}

		return result;
	}

	static public IBakedModule BuildItem(Map<Integer, List<IBakedModule>> modules){
		ModuleList list = new ModuleList();
		
		modules.entrySet().stream()
			.sorted(Comparator.comparing(Map.Entry::getKey))
			.forEach(e->{
				list.addAll(0, BuildPriorityGroup(e.getValue()));
			});

		return list.UnwrapIfSingle();
	}

	static public List<IBakedModule> BuildPriorityGroup(List<IBakedModule> modules){
		List<IBakedModule> result = new ArrayList<>(modules);

		// Create group caches for modules with the same fingerprints.
		for (int ia=0; ia<result.size(); ++ia)
		for (int ib=result.size()-1; ia<ib; --ib)
		{
			IBakedModule a = result.get(ia);
			IBakedModule b = result.get(ib);

			if (a.GetCacheKeys().CompareTo(b.GetCacheKeys()).isEqual()){
				result.set(ia, CacheModules(a,b));
				result.remove(ib);
			}
		}

		// Create solo-caches for modules that require it.
		for (int i=0; i<result.size(); ++i){
			IBakedModule m = result.get(i);
			if (m.GetCachePolicy() == ECachePolicy.ALWAYS)
				result.set(i, new CacheModule(m));
		}

		return result;
	}

	/**
	 * Creates  a single  cache  that encompasses  all provided  modules, whilst
	 * minimizing the amount of wrap layers.
	 */
	static public CacheModule CacheModules(IBakedModule... modules){
		ModuleList result = new ModuleList();

		for (IBakedModule m : modules){
			if (m instanceof IModuleWrapper wrapper)
				result.addAll(wrapper.Unwrap());
			else
				result.add(m);
		}

		return new CacheModule(result.UnwrapIfSingle());
	}
}
