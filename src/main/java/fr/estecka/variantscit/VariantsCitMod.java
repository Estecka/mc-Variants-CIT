package fr.estecka.variantscit;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.estecka.variantscit.modules.*;


public class VariantsCitMod
implements ClientModInitializer, PreparableModelLoadingPlugin<Map<Item,VariantManager>>
{
	static public final String MODID = "variants-cit";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	static private Map<Item, VariantManager> MODULES = new HashMap<>();

	static public @Nullable VariantManager GetModule(Item itemType){
		return MODULES.get(itemType);
	}

	@Override
	public void onInitializeClient(){
		PreparableModelLoadingPlugin.register(new ModuleLoader(), this);

		ModuleRegistry.Register(Identifier.ofVanilla("axolotl_variant"), new AxolotlBucketModule());
		ModuleRegistry.Register(Identifier.ofVanilla("custom_data"), CustomVariantModule::new, CustomVariantModule.CODEC);
		ModuleRegistry.Register(Identifier.ofVanilla("instrument"), new GoatHornModule());
		ModuleRegistry.Register(Identifier.ofVanilla("jukebox_playable"), new MusicDiscModule());
		ModuleRegistry.Register(Identifier.ofVanilla("potion_effect"), new PotionEffectModule());
		ModuleRegistry.Register(Identifier.ofVanilla("potion_type"), new PotionTypeModule());
		ModuleRegistry.Register(Identifier.ofVanilla("stored_enchantments"), EnchantedBookModule::new);

		ModelPredicateProviderRegistry.register(Items.ENCHANTED_BOOK, Identifier.ofVanilla("level"), new EnchantedBookLevelPredicate());
		var potionPredicate = new PotionLevelPredicate();
		ModelPredicateProviderRegistry.register(Items.POTION, Identifier.ofVanilla("amplifier"), potionPredicate);
		ModelPredicateProviderRegistry.register(Items.SPLASH_POTION, Identifier.ofVanilla("amplifier"), potionPredicate);
		ModelPredicateProviderRegistry.register(Items.LINGERING_POTION, Identifier.ofVanilla("amplifier"), potionPredicate);
	}

	@Override
	public void onInitializeModelLoader(Map<Item,VariantManager> modules, ModelLoadingPlugin.Context pluginContext){
		MODULES = modules;
		for (var entry : MODULES.entrySet()){
			VariantManager module = entry.getValue();
			Identifier itemId = Registries.ITEM.getId(entry.getKey());

			module.GetAllModels().stream().map(ModelIdentifier::id).forEach(pluginContext::addModels);
			LOGGER.info("Loaded {} CITs for item {}", module.GetVariantCount(), itemId);
		}
	}

}
