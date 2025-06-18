package fr.estecka.variantscit;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.reload.ModuleLoader;
import fr.estecka.variantscit.modules.*;


public class VariantsCitMod
implements ClientModInitializer
{
	static public final String MODID = "variants-cit";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	static public int reloadcount = 0;
	static public final EquippableCache EQUIPABLES = new EquippableCache();
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
	}

	static public void OnResourceReload(ModuleLoader.Result result){
		++reloadcount;
		EQUIPABLES.Clear();
		ITEM_MODULES  = result.itemModules;
		EQUIP_MODULES = result.equipModules;
	}

}
