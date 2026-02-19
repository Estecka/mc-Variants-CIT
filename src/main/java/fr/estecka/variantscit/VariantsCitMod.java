package fr.estecka.variantscit;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.reload.EModuleContext;
import fr.estecka.variantscit.reload.ModuleLoader;
import fr.estecka.variantscit.reload.MetaModule;
import fr.estecka.variantscit.commands.AssetGenCommands;
import fr.estecka.variantscit.commands.CacheCommands;
import fr.estecka.variantscit.commands.ModuleCommands;
import fr.estecka.variantscit.modules.IBakedModule;
import fr.estecka.variantscit.modules.cache.CacheBuilder;


public class VariantsCitMod
implements ClientModInitializer
{
	static public final String MODID = "variants-cit";
	public static final LabelledLogger LOGGER = new LabelledLogger();

	static public final EquippableCache EQUIPABLES = new EquippableCache();
	static private Map<Item, IBakedModule> ITEM_MODULES  = Map.of();
	static private Map<Item, IBakedModule> EQUIP_MODULES = Map.of();
	static private Map<ResourceLocation, MetaModule> META = Map.of();

	static public ResourceLocation Identifier(String path){
		return ResourceLocation.fromNamespaceAndPath(MODID, path);
	}

	static public @Nullable IBakedModule GetItemModule(Item itemType){
		return ITEM_MODULES.get(itemType);
	}
	static public @Nullable IBakedModule GetEquipmentModule(Item itemType){
		return EQUIP_MODULES.get(itemType);
	}
	static public Map<ResourceLocation,MetaModule> GetMeta(){
		return Map.copyOf(META);
	}
	static public IBakedModule GetModule(EModuleContext context, ResourceLocation id){
		MetaModule meta = META.get(id);
		if (meta == null)
			return null;
		else
			return meta.bakedModules().get(context);
	}
	static public Set<Item> GetItems(EModuleContext context){
		switch (context) {
			case ITEM_MODEL: return ITEM_MODULES .keySet();
			case EQUIPPABLE: return EQUIP_MODULES.keySet();
			default: throw new NotImplementedException();
		}
	}

	@Override
	public void onInitializeClient(){
		ModuleCommands.Register();
		AssetGenCommands.Register();
		CacheCommands.Register();
	}

	static public void OnResourceReload(ModuleLoader.Result result){
		EQUIPABLES.Clear();
		var cached = CacheBuilder.BuildAll(result.sortedModules);
		ITEM_MODULES  = cached.getOrDefault(EModuleContext.ITEM_MODEL, Map.of());
		EQUIP_MODULES = cached.getOrDefault(EModuleContext.EQUIPPABLE, Map.of());
		META = result.uniqueModules;
	}

}
