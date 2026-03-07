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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ModuleRepository
{
	private final BiMap<EModuleHook, Item, IBakedModule> modelResolver;
	private final Map<ResourceLocation, MetaModule> metadata;
	private final IdentityHashMap<IBakedModule, ResourceLocation> moduleToId;

	ModuleRepository(){
		this.modelResolver = new BiMap<>();
		this.metadata = Map.of();
		this.moduleToId = new IdentityHashMap<>();
	}

	ModuleRepository(ModuleLoader.Result result){
		this.modelResolver = CacheBuilder.BuildAll(result.sortedModules);
		this.metadata = Map.copyOf(result.uniqueModules);
		this.moduleToId = new IdentityHashMap<>();

		for (ResourceLocation id : metadata.keySet())
		for (IBakedModule module : metadata.get(id).bakedModules().values())
		{
			moduleToId.put(module, id);
		}
	}

	public IBakedModule GetArchModule(EModuleHook hook, Item item){
		return this.modelResolver.get(hook, item);
	}

	public @Nullable ResourceLocation GetModelForItem(EModuleHook hook, ItemStack stack) {
		@Nullable IBakedModule module = GetArchModule(hook, stack.getItem());
		if (module == null)
			return null;
		else
			return module.GetModelForItem(stack);
	}

	public Set<Item> GetAvailableItem(EModuleHook hook){
		return Set.copyOf(modelResolver.getOrDefault(hook, Map.of()).keySet());
	}

	public Stream<ResourceLocation> GetAvailableModules(EModuleHook hook){
		return metadata.entrySet().stream()
			.filter(meta -> meta.getValue().bakedModules().get(hook) != null)
			.map(Map.Entry::getKey)
			;
	}

	public ResourceLocation GetId(IBakedModule module){
		return this.moduleToId.get(module);
	}

	public MetaModule GetMeta(ResourceLocation id){
		return this.metadata.get(id);
	}
}
