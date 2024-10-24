package fr.estecka.variantscit;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.estecka.variantscit.modules.*;


public class VariantsCitMod
implements ClientModInitializer, PreparableModelLoadingPlugin<ModuleLoader.Result>
{
	static public final String MODID = "variants-cit";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	static public int reloadcount = 0;
	static private Map<Item, IItemModelProvider> MODULES = new HashMap<>();
	static private Map<ModelIdentifier, Identifier> AUTOGEN = new HashMap<>();

	static public @Nullable IItemModelProvider GetModule(Item itemType){
		return MODULES.get(itemType);
	}

	static public Map<ModelIdentifier, Identifier> GetModelsToCreate(){
		return Map.copyOf(AUTOGEN);
	}

	@Override
	public void onInitializeClient(){
		PreparableModelLoadingPlugin.register(new ModuleLoader(), this);

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
		ModuleRegistry.Register(Identifier.ofVanilla("stored_enchantments"), _0 -> {
			LOGGER.warn("Module name `stored_enchantments` (plural) is being deprecated. use `stored_enchantment` (singular) instead.");
			return new EnchantedBookModule();
		});

		ModelPredicateProviderRegistry.register(Items.ENCHANTED_BOOK, Identifier.ofVanilla("level"), new EnchantedBookLevelPredicate());
		var potionPredicate = new PotionLevelPredicate();
		ModelPredicateProviderRegistry.register(Items.POTION, Identifier.ofVanilla("amplifier"), potionPredicate);
		ModelPredicateProviderRegistry.register(Items.SPLASH_POTION, Identifier.ofVanilla("amplifier"), potionPredicate);
		ModelPredicateProviderRegistry.register(Items.LINGERING_POTION, Identifier.ofVanilla("amplifier"), potionPredicate);
	}

	@Override
	public void onInitializeModelLoader(ModuleLoader.Result result, ModelLoadingPlugin.Context pluginContext){
		++reloadcount;
		result.modelAggregator.modelsToLoad.stream().map(ModelIdentifier::id).forEach(pluginContext::addModels);

		for (var e : result.uniqueModules.entrySet())
			LOGGER.info("Found {} variants for CIT module {}", e.getValue().library().GetVariantCount(), e.getKey());

		AUTOGEN = result.modelAggregator.modelsToCreate;
		MODULES = new HashMap<>();
		for (var entry : result.modulesPerItem.entrySet()){
			MODULES.put(
				entry.getKey().value(),
				IItemModelProvider.OfList( entry.getValue().stream().map(meta->meta.bakedModule()).toList() )
			);
		}
	}

}
