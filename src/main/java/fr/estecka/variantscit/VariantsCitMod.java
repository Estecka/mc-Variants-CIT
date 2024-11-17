package fr.estecka.variantscit;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.mixin.NumericPropertiesAccessor;
import fr.estecka.variantscit.modules.*;
import fr.estecka.variantscit.reload.ModuleLoader;


public class VariantsCitMod
implements ClientModInitializer
{
	static public final String MODID = "variants-cit";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	static public int reloadcount = 0;
	static private Map<Item, IItemModelProvider> MODULES = new HashMap<>();
	static private Map<Identifier, Identifier> MODEL_AUTOGEN = Map.of();
	static private Set<Identifier> ITEM_AUTOGEN = Set.of();

	static public @Nullable IItemModelProvider GetModule(Item itemType){
		return MODULES.get(itemType);
	}

	static public Map<Identifier, Identifier> GetModelsToCreate(){
		return Map.copyOf(MODEL_AUTOGEN);
	}
	static public Set<Identifier> GetItemsToCreate(){
		return Set.copyOf(ITEM_AUTOGEN);
	}

	@Override
	public void onInitializeClient(){
		ModuleRegistry.Register(Identifier.ofVanilla("axolotl_variant"), new AxolotlBucketModule());
		ModuleRegistry.Register(Identifier.ofVanilla("custom_data"), CustomDataModule.CODEC);
		ModuleRegistry.Register(Identifier.ofVanilla("custom_name"), CustomNameModule.CODEC);
		ModuleRegistry.Register(Identifier.ofVanilla("enchantment"), EnchantedToolModule.CODEC);
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

		NumericPropertiesAccessor.ID_MAPPER().put(EnchantedBookLevelPredicate.ID, EnchantedBookLevelPredicate.CODEC);
	}

	// @Override
	public void initialize(ModuleLoader.Result result/*, ModelLoadingPlugin.Context pluginContext*/){
		++reloadcount;

		for (var e : result.uniqueModules.entrySet())
			LOGGER.info("Found {} variants for CIT module {}", e.getValue().library().GetVariantCount(), e.getKey());

		MODEL_AUTOGEN = result.modelAggregator.modelsToCreate;
		ITEM_AUTOGEN = result.modelAggregator.itemsToCreate;

		MODULES = new HashMap<>();
		for (var entry : result.modulesPerItem.entrySet()){
			MODULES.put(
				entry.getKey().value(),
				IItemModelProvider.OfList( entry.getValue().stream().map(meta->meta.bakedModule()).toList() )
			);
		}
	}

}
