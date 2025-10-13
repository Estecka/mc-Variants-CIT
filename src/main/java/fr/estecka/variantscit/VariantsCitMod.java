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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.estecka.variantscit.reload.ModuleLoader;
import fr.estecka.variantscit.modulebakers.IBakedModule;
import fr.estecka.variantscit.properties.*;
import fr.estecka.variantscit.selectors.*;


public class VariantsCitMod
implements ClientModInitializer
{
	static public final String MODID = "variants-cit";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

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
	}

	static public void OnResourceReload(ModuleLoader.Result result){
		++reloadcount;
		EQUIPABLES.Clear();
		ITEM_MODULES  = result.itemModules;
		EQUIP_MODULES = result.equipModules;
	}

}
