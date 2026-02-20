package fr.estecka.variantscit;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.collections.BiMap;
import fr.estecka.variantscit.modules.IBakedModule;
import fr.estecka.variantscit.modules.cache.CacheBuilder;
import fr.estecka.variantscit.reload.EModuleContext;
import fr.estecka.variantscit.reload.MetaModule;
import fr.estecka.variantscit.reload.ModuleLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ModuleRepository
{
	private final BiMap<EModuleContext, Item, IBakedModule> modelResolver;
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

	public IBakedModule GetArchModule(EModuleContext context, Item item){
		return this.modelResolver.get(context, item);
	}

	public @Nullable ResourceLocation GetModelForItem(EModuleContext context, ItemStack stack) {
		// FIXME: This instantiates one empty map for every rendered item, every frame.
		@Nullable IBakedModule module = GetArchModule(context, stack.getItem());
		if (module == null)
			return null;
		else
			return module.GetModelForItem(stack);
	}

	public Set<Item> GetAvailableItem(EModuleContext context){
		return Set.copyOf(modelResolver.getOrDefault(context, Map.of()).keySet());
	}

	public Stream<ResourceLocation> GetAvailableModules(EModuleContext context){
		return metadata.entrySet().stream()
			.filter(meta -> meta.getValue().bakedModules().get(context) != null)
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
