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
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.reload.ModuleLoader;
import fr.estecka.variantscit.modules.*;
import fr.estecka.variantscit.properties.*;
import fr.estecka.variantscit.selectors.*;


public class VariantsCitMod
implements ClientModInitializer
{
	static public final String MODID = "variants-cit";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	static public int reloadcount = 0;
	static private Map<Item, IItemModelProvider> MODULES = new HashMap<>();

	static public @Nullable IItemModelProvider GetModule(Item itemType){
		return MODULES.get(itemType);
	}

	@Override
	public void onInitializeClient(){
		ModuleRegistry.Register(Identifier.ofVanilla("axolotl_variant"), new AxolotlBucketModule());
		ModuleRegistry.Register(Identifier.ofVanilla("block_entity_data"), NbtStringModule.CreateCodec(DataComponentTypes.BLOCK_ENTITY_DATA));
		ModuleRegistry.Register(Identifier.ofVanilla("bucket_entity_data"), NbtStringModule.CreateCodec(DataComponentTypes.BUCKET_ENTITY_DATA));
		ModuleRegistry.Register(Identifier.ofVanilla("custom_data"), NbtStringModule.CreateCodec(DataComponentTypes.CUSTOM_DATA));
		ModuleRegistry.Register(Identifier.ofVanilla("custom_name"), CustomNameModule.CODEC);
		ModuleRegistry.Register(Identifier.ofVanilla("enchantment"), EnchantedToolModule.CODEC);
		ModuleRegistry.Register(Identifier.ofVanilla("entity_data"), NbtStringModule.CreateCodec(DataComponentTypes.ENTITY_DATA));
		ModuleRegistry.Register(Identifier.ofVanilla("instrument"), new GoatHornModule());
		ModuleRegistry.Register(Identifier.ofVanilla("jukebox_playable"), new MusicDiscModule());
		ModuleRegistry.Register(Identifier.ofVanilla("painting_variant"), new PaintingVariantModule());
		ModuleRegistry.Register(Identifier.ofVanilla("potion_effect"), new PotionEffectModule());
		ModuleRegistry.Register(Identifier.ofVanilla("potion_type"), new PotionTypeModule());
		ModuleRegistry.Register(Identifier.ofVanilla("stored_enchantment"), new EnchantedBookModule());
		ModuleRegistry.Register(Identifier.ofVanilla("stored_enchantments"), MapCodec.unit(()->{
			LOGGER.warn("Module name `stored_enchantments` (plural) is being deprecated. use `stored_enchantment` (singular) instead.");
			return new EnchantedBookModule();
		}));

		NumericProperties.ID_MAPPER.put(Identifier.of(MODID, "bucket_entity_number"), NbtNumberProperty.CreateCodec(DataComponentTypes.BUCKET_ENTITY_DATA));
		NumericProperties.ID_MAPPER.put(Identifier.of(MODID, "stored_enchantment_level"), EnchantedBookLevelPredicate.CODEC);
		ItemModelTypes.ID_MAPPER.put(Identifier.ofVanilla("range_dispatch"), DynamicRangeDispatchUnbaked.CODEC);
	}

	static public void OnResourceReload(ModuleLoader.Result result){
		++reloadcount;

		for (var e : result.uniqueModules.entrySet())
			LOGGER.info("Found {} variants for CIT module {}", e.getValue().library().GetVariantCount(), e.getKey());

		MODULES = new HashMap<>();
		for (var entry : result.modulesPerItem.entrySet()){
			MODULES.put(
				entry.getKey().value(),
				IItemModelProvider.OfList( entry.getValue().stream().map(meta->meta.bakedModule()).toList() )
			);
		}
	}

}
