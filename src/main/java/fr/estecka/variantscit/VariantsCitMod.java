package fr.estecka.variantscit;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.render.item.model.ItemModelTypes;
import net.minecraft.client.render.item.property.numeric.NumericProperties;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.reload.ModuleLoader;
import fr.estecka.variantscit.reload.ModuleLoader.MetaModule;
import fr.estecka.variantscit.modules.*;
import fr.estecka.variantscit.properties.*;
import fr.estecka.variantscit.selectors.*;


public class VariantsCitMod
implements ClientModInitializer
{
	static public final String MODID = "variants-cit";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	static public int reloadcount = 0;
	static private Map<Item, IItemModelProvider> ITEM_MODULES  = new HashMap<>();
	static private Map<Item, IItemModelProvider> EQUIP_MODULES = new HashMap<>();

	static public @Nullable IItemModelProvider GetItemModule(Item itemType){
		return ITEM_MODULES.get(itemType);
	}
	static public @Nullable IItemModelProvider GetEquipmentModule(Item itemType){
		return EQUIP_MODULES.get(itemType);
	}

	@Override
	public void onInitializeClient(){
		ModuleRegistry.Register(Identifier.ofVanilla("axolotl_variant"), AxolotlBucketModule.CODEC);
		ModuleRegistry.Register(Identifier.ofVanilla("block_entity_data"), ComponentDataModule.CreateLegacyCodec(DataComponentTypes.BLOCK_ENTITY_DATA));
		ModuleRegistry.Register(Identifier.ofVanilla("bucket_entity_data"), ComponentDataModule.CreateLegacyCodec(DataComponentTypes.BUCKET_ENTITY_DATA));
		ModuleRegistry.Register(Identifier.ofVanilla("component_data"), ComponentDataModule.CODEC);
		ModuleRegistry.Register(Identifier.ofVanilla("component_format"), MultiComponentFormatModule.CODEC);
		ModuleRegistry.Register(Identifier.ofVanilla("custom_data"), ComponentDataModule.CreateLegacyCodec(DataComponentTypes.CUSTOM_DATA));
		ModuleRegistry.Register(Identifier.ofVanilla("custom_name"), CustomNameModule.CODEC);
		ModuleRegistry.Register(Identifier.ofVanilla("durability"), DurabilityModule.CODEC);
		ModuleRegistry.Register(Identifier.ofVanilla("enchantment"), EnchantedToolModule.CODEC);
		ModuleRegistry.Register(Identifier.ofVanilla("entity_data"), ComponentDataModule.CreateLegacyCodec(DataComponentTypes.ENTITY_DATA));
		ModuleRegistry.Register(Identifier.ofVanilla("instrument"), new GoatHornModule());
		ModuleRegistry.Register(Identifier.ofVanilla("item_count"), ItemCountModule.CODEC);
		ModuleRegistry.Register(Identifier.ofVanilla("jukebox_playable"), new MusicDiscModule());
		ModuleRegistry.Register(Identifier.ofVanilla("painting_variant"), new PaintingVariantModule());
		ModuleRegistry.Register(Identifier.ofVanilla("potion_effect"), new PotionEffectModule());
		ModuleRegistry.Register(Identifier.ofVanilla("potion_type"), new PotionTypeModule());
		ModuleRegistry.Register(Identifier.ofVanilla("stored_enchantment"), EnchantedBookModule.CODEC);
		ModuleRegistry.Register(Identifier.ofVanilla("stored_enchantments"), MapCodec.unit(() -> {
			LOGGER.warn("Module name `stored_enchantments` (plural) is being deprecated. use `stored_enchantment` (singular) instead.");
			return new EnchantedBookModule();
		}));
		ModuleRegistry.Register(Identifier.ofVanilla("trim"), new TrimModule());
		ModuleRegistry.Register(Identifier.ofVanilla("trim_pattern"), new TrimPatternModule());
		ModuleRegistry.Register(Identifier.ofVanilla("trim_material"), new TrimPatternModule());

		NumericProperties.ID_MAPPER.put(Identifier.of(MODID, "block_entity_data"), NbtNumberProperty.CreateCodec(DataComponentTypes.BLOCK_ENTITY_DATA));
		NumericProperties.ID_MAPPER.put(Identifier.of(MODID, "bucket_entity_data"), NbtNumberProperty.CreateCodec(DataComponentTypes.BUCKET_ENTITY_DATA));
		NumericProperties.ID_MAPPER.put(Identifier.of(MODID, "custom_data"), NbtNumberProperty.CreateCodec(DataComponentTypes.CUSTOM_DATA));
		NumericProperties.ID_MAPPER.put(Identifier.of(MODID, "entity_data"), NbtNumberProperty.CreateCodec(DataComponentTypes.ENTITY_DATA));
		NumericProperties.ID_MAPPER.put(Identifier.of(MODID, "stored_enchantment_level"), EnchantedBookLevelPredicate.CODEC);
		ItemModelTypes.ID_MAPPER.put(Identifier.ofVanilla("range_dispatch"), DynamicRangeDispatchUnbaked.CODEC);
	}

	static public void OnResourceReload(ModuleLoader.Result result){
		++reloadcount;

		Map<Item, List<BakedModule>> itemModules  = new HashMap<>();
		Map<Item, List<BakedModule>> equipModules = new HashMap<>();

		for (MetaModule meta : result.orderedModules)
		{
			VariantLibrary itemLib  = meta.itemLibrary();
			VariantLibrary equipLib = meta.equipLibrary();

			if (meta.targets().isEmpty()) {
				LOGGER.warn("Ignored VCIT module with no valid target {}", meta.id());
				continue;
			}

			// // Will prevent dry-run debugging
			// if (itemLib.isEmpty() && equipLib.isEmpty()){
			// 	LOGGER.warn("Ignored VCIT modules with no models {}", meta.id());
			// 	continue;
			// }

			int itemCount  = itemLib.GetVariantCount();
			int equipCount = equipLib.GetVariantCount();
			if (itemCount <= 0 && equipCount <= 0)
				LOGGER.warn("Found no variant for VCIT module {}", meta.id());
			if (itemCount > 0)
				LOGGER.info("Found {} item_model variants for VCIT module {}", itemCount, meta.id());
			if (equipCount > 0)
				LOGGER.info("Found {} equipment variants for CIT module {}",  equipCount, meta.id());


			for (Item itemType : meta.targets()){
				if (!itemLib .isEmpty()) itemModules .computeIfAbsent(itemType, __->new ArrayList<>()).add(new BakedModule(itemLib,  meta.logic()));
				if (!equipLib.isEmpty()) equipModules.computeIfAbsent(itemType, __->new ArrayList<>()).add(new BakedModule(equipLib, meta.logic()));
			}
		}

		ITEM_MODULES  = CombineModules(itemModules);
		EQUIP_MODULES = CombineModules(equipModules);
	}

	static private Map<Item, IItemModelProvider> CombineModules(Map<Item, List<BakedModule>> moduleListPerItem){
		Map<Item, IItemModelProvider> result = new HashMap<>();

		for (var entry : moduleListPerItem.entrySet()){
			result.put(
				entry.getKey(),
				IItemModelProvider.OfList( entry.getValue() )
			);
		}

		return result;
	}

}
