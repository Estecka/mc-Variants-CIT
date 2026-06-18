package fr.estecka.variantscit;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.collections.BiMap;
import fr.estecka.variantscit.modules.IBakedModule;
import fr.estecka.variantscit.modules.cache.CacheBuilder;
import fr.estecka.variantscit.reload.EModuleHook;
import fr.estecka.variantscit.reload.MetaModule;
import fr.estecka.variantscit.reload.ModuleLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ModuleRepository
{
	private final BiMap<EModuleHook, Item, IBakedModule> archModules;
	private final Map<Identifier, MetaModule> metadata;
	private final IdentityHashMap<IBakedModule, Identifier> moduleToId;

	ModuleRepository(){
		this.archModules = new BiMap<>();
		this.metadata = Map.of();
		this.moduleToId = new IdentityHashMap<>();
	}

	ModuleRepository(ModuleLoader.Result result){
		this.archModules = CacheBuilder.BuildAll(result.sortedModules);
		this.metadata = Map.copyOf(result.uniqueModules);
		this.moduleToId = new IdentityHashMap<>();

		for (Identifier id : metadata.keySet())
		for (IBakedModule module : metadata.get(id).bakedModules().values())
		{
			moduleToId.put(module, id);
		}
	}

	public IBakedModule GetArchModule(EModuleHook hook, Item item){
		return this.archModules.get(hook, item);
	}

	public @Nullable Identifier GetModelForItem(EModuleHook hook, ItemStack stack) {
		@Nullable IBakedModule module = GetArchModule(hook, stack.getItem());
		if (module == null)
			return null;
		else
			return module.GetModelForItem(stack);
	}

	public Set<Item> GetAvailableItems(EModuleHook hook){
		return Set.copyOf(archModules.getOrDefault(hook, Map.of()).keySet());
	}

	public Stream<Identifier> GetAvailableModules(EModuleHook hook){
		return metadata.entrySet().stream()
			.filter(meta -> meta.getValue().bakedModules().get(hook) != null)
			.map(Map.Entry::getKey)
			;
	}

	public Identifier GetId(IBakedModule module){
		return this.moduleToId.get(module);
	}

	public MetaModule GetMeta(Identifier id){
		return this.metadata.get(id);
	}
}
