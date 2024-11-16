package fr.estecka.variantscit;

import net.fabricmc.api.ClientModInitializer;
// import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
// import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
// import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.estecka.variantscit.mixin.NumericPropertiesAccessor;
import fr.estecka.variantscit.modules.*;
import fr.estecka.variantscit.reload.ModuleLoader;


public class VariantsCitMod
implements ClientModInitializer
//, PreparableModelLoadingPlugin<ModuleLoader.Result>
{
	static public final String MODID = "variants-cit";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	static public int reloadcount = 0;
	static private Map<Item, IItemModelProvider> MODULES = new HashMap<>();
	static private Map<Identifier, Identifier> AUTOGEN = new HashMap<>();

	static public @Nullable IItemModelProvider GetModule(Item itemType){
		return MODULES.get(itemType);
	}

	static public Map<Identifier, Identifier> GetModelsToCreate(){
		return Map.copyOf(AUTOGEN);
	}

	/**
	 * For some reason, Minecraft strips the "item/" off of item models.
	 */
	@Deprecated
	static public ModelIdentifier ModelIdFromResource(Identifier id){
		String path = id.getPath();
		if (path.startsWith("item/"))
			path = path.substring("item/".length());
		return new ModelIdentifier(id.withPath(path), "inventory");
	}

	@Override
	public void onInitializeClient(){
		// PreparableModelLoadingPlugin.register(new ModuleLoader(), this);

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
		// ModuleRegistry.Register(Identifier.ofVanilla("stored_enchantments"), _0 -> {
		// 	LOGGER.warn("Module name `stored_enchantments` (plural) is being deprecated. use `stored_enchantment` (singular) instead.");
		// 	return new EnchantedBookModule();
		// });

		NumericPropertiesAccessor.ID_MAPPER().put(EnchantedBookLevelPredicate.ID, EnchantedBookLevelPredicate.CODEC);
	}

	/**
	 * TODO: Find entry point
	 */
	// @Override
	public void initialize(ModuleLoader.Result result/*, ModelLoadingPlugin.Context pluginContext*/){
		++reloadcount;
		// result.modelAggregator.modelsToLoad.stream().map(ModelIdentifier::id).forEach(pluginContext::addModels);

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
