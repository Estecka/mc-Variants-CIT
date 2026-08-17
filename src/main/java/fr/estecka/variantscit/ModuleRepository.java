package fr.estecka.variantscit;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.DataResult;
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
	private final Map<Identifier, String> moduleErrors;
	private final IdentityHashMap<IBakedModule, Identifier> moduleToId;

	ModuleRepository(){
		this.archModules = new BiMap<>();
		this.metadata = Map.of();
		this.moduleErrors = Map.of();
		this.moduleToId = new IdentityHashMap<>();
	}

	ModuleRepository(ModuleLoader.Result result){
		this.archModules = CacheBuilder.BuildAll(result.sortedModules);
		this.metadata = Map.copyOf(result.uniqueModules);
		this.moduleErrors = Map.copyOf(result.moduleErrors);
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
		var errors = moduleErrors.entrySet().stream();
		var valid = metadata.entrySet().stream()
			.filter(meta -> meta.getValue().bakedModules().get(hook) != null)
			;

		return Stream.concat(valid, errors)
			.map(Map.Entry::getKey)
			;
	}

	public Identifier GetId(IBakedModule module){
		return this.moduleToId.get(module);
	}

	public DataResult<MetaModule> GetMeta(Identifier id){
		var r = this.metadata.get(id);
		if (r != null)
			return DataResult.success(r);

		String err = this.moduleErrors.get(id);
		if (err != null)
			return DataResult.error(()->"The module "+id+" is invalid and could not be loaded:\n\n"+err);

		return DataResult.error(()->"No such module: "+id);
	}
}
