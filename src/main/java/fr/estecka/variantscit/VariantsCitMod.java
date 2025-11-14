package fr.estecka.variantscit;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.render.item.model.ItemModelTypes;
import net.minecraft.client.render.item.property.numeric.NumericProperties;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.reload.EModuleContext;
import fr.estecka.variantscit.reload.ModuleLoader;
import fr.estecka.variantscit.reload.ModuleLoader.MetaModule;
import fr.estecka.variantscit.commands.CommandUtil;
import fr.estecka.variantscit.commands.DumpCommand;
import fr.estecka.variantscit.modulebakers.IBakedModule;
import fr.estecka.variantscit.properties.*;
import fr.estecka.variantscit.selectors.*;


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
		NumericProperties.ID_MAPPER.put(Identifier.of(MODID, "block_entity_data"), NbtNumberProperty.CreateCodec(DataComponentTypes.BLOCK_ENTITY_DATA));
		NumericProperties.ID_MAPPER.put(Identifier.of(MODID, "bucket_entity_data"), NbtNumberProperty.CreateCodec(DataComponentTypes.BUCKET_ENTITY_DATA));
		NumericProperties.ID_MAPPER.put(Identifier.of(MODID, "custom_data"), NbtNumberProperty.CreateCodec(DataComponentTypes.CUSTOM_DATA));
		NumericProperties.ID_MAPPER.put(Identifier.of(MODID, "entity_data"), NbtNumberProperty.CreateCodec(DataComponentTypes.ENTITY_DATA));
		NumericProperties.ID_MAPPER.put(Identifier.of(MODID, "stored_enchantment_level"), EnchantedBookLevelPredicate.CODEC);
		ItemModelTypes.ID_MAPPER.put(Identifier.ofVanilla("range_dispatch"), DynamicRangeDispatchUnbaked.CODEC);
		
		DumpCommand.Register();
	}

	static public void OnResourceReload(ModuleLoader.Result result){
		++reloadcount;
		EQUIPABLES.Clear();
		ITEM_MODULES  = result.itemModules;
		EQUIP_MODULES = result.equipModules;

		CommandUtil.modules.get(EModuleContext.EQUIPPABLE).clear();
		CommandUtil.modules.get(EModuleContext.ITEM_MODEL).clear();
		for (var entry : result.allModules.entrySet()){
			Identifier id = entry.getKey();
			MetaModule meta = entry.getValue();
			if (meta.itemModule ().isPresent()) CommandUtil.modules.get(EModuleContext.ITEM_MODEL).put(id, meta.itemModule ().get());
			if (meta.equipModule().isPresent()) CommandUtil.modules.get(EModuleContext.EQUIPPABLE).put(id, meta.equipModule().get());
		}
	}

}
