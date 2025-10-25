package fr.estecka.variantscit;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.reload.ModuleLoader;
import fr.estecka.variantscit.modulebakers.IBakedModule;


public class VariantsCitMod
implements ClientModInitializer
{
	static public final String MODID = "variants-cit";
	public static final LabelledLogger LOGGER = new LabelledLogger();

	static public int reloadcount = 0;
	static public final EquippableCache EQUIPABLES = new EquippableCache();
	static private Map<Item, IBakedModule> ITEM_MODULES  = new HashMap<>();
	static private Map<Item, IBakedModule> EQUIP_MODULES = new HashMap<>();

	static public @Nullable IBakedModule GetItemModule(Item itemType){
		return ITEM_MODULES.get(itemType);
	}
	static public @Nullable IBakedModule GetEquipmentModule(Item itemType){
		return EQUIP_MODULES.get(itemType);
	}

	@Override
	public void onInitializeClient(){
	}

	static public void OnResourceReload(ModuleLoader.Result result){
		++reloadcount;
		EQUIPABLES.Clear();
		ITEM_MODULES  = result.itemModules;
		EQUIP_MODULES = result.equipModules;
	}

}
