package fr.estecka.variantscit;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.reload.EModuleContext;
import fr.estecka.variantscit.reload.ModuleLoader;
import fr.estecka.variantscit.reload.MetaModule;
import fr.estecka.variantscit.commands.AssetGenCommands;
import fr.estecka.variantscit.commands.ModuleCommands;
import fr.estecka.variantscit.modules.IBakedModule;


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
		else {
			return switch (context) {
				default -> throw new IllegalArgumentException();
				case ITEM_MODEL -> meta.itemModule().orElse(null);
				case EQUIPPABLE -> meta.equipModule().orElse(null);
			};
		}
	}

	@Override
	public void onInitializeClient(){
		ModuleCommands.Register();
		AssetGenCommands.Register();
	}

	static public void OnResourceReload(ModuleLoader.Result result){
		EQUIPABLES.Clear();
		ITEM_MODULES  = result.itemModules;
		EQUIP_MODULES = result.equipModules;
		META = result.allModules;
	}

}
