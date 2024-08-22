package fr.estecka.variantscit;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin.DataLoader;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import fr.estecka.variantscit.modules.axolotl_bucket.AxolotlBucketModule;
import fr.estecka.variantscit.modules.enchanted_book.EnchantedBookModule;
import fr.estecka.variantscit.modules.enchanted_book.EnchantedBookLevelPredicate;
import fr.estecka.variantscit.modules.potion_effect.PotionEffectModule;
import fr.estecka.variantscit.modules.potion_effect.PotionLevelPredicate;
import fr.estecka.variantscit.modules.potion_effect.PotionTypeModule;


public class VariantsCitMod
implements ClientModInitializer, PreparableModelLoadingPlugin<Map<Item,VariantManager>>, DataLoader<Map<Item,VariantManager>>
{
	static public final String MODID = "variants-cit";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	static private Map<Item, VariantManager> MODULES = new HashMap<>();

	static public @Nullable VariantManager GetModule(Item itemType){
		return MODULES.get(itemType);
	}

	@Override
	public void onInitializeClient(){
		PreparableModelLoadingPlugin.register(this, this);
		ModuleRegistry.Register(Identifier.ofVanilla("stored_enchantments"), EnchantedBookModule::new);
		ModuleRegistry.Register(Identifier.ofVanilla("axolotl_variant"), AxolotlBucketModule::new);
		ModuleRegistry.Register(Identifier.ofVanilla("potion_effect"), PotionEffectModule::new);
		ModuleRegistry.Register(Identifier.ofVanilla("potion_type"), PotionTypeModule::new);

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

			pluginContext.addModels(module.GetAllModels());
			LOGGER.info("Loaded {} CITs for item {}", module.GetVariantCount(), itemId);
		}
	}

	@Override
	public CompletableFuture<Map<Item,VariantManager>> load(ResourceManager resourceManager, Executor executor){
		return CompletableFuture.supplyAsync(()->Reload(resourceManager), executor);
	}

	private Map<Item,VariantManager> Reload(ResourceManager manager){
		Map<Item,VariantManager> result = new HashMap<>();

		for (Map.Entry<Identifier, Resource> entry : manager.findResources("variant-cits/item", id->id.getPath().endsWith(".json")).entrySet()){
			Identifier resourceId = entry.getKey();
			Item item = ItemFromModule(resourceId);
			if (item == null)
				continue;

			JsonElement json;
			VariantManager module;
			try {
				json = JsonHelper.deserialize(entry.getValue().getReader());
			}
			catch (IOException e){
				VariantsCitMod.LOGGER.error("{}", e);
				continue;
			}

			var dataResult = ModuleDefinition.CODEC.decoder().decode(JsonOps.INSTANCE, json);
			if (dataResult.isError()){
				VariantsCitMod.LOGGER.error("Error in cit module {}: {}", resourceId, dataResult.error().get().message());
				continue;
			}

			try {
				module = ModuleRegistry.CreateModule(dataResult.getOrThrow().getFirst());
			}
			catch (IllegalArgumentException e){
				VariantsCitMod.LOGGER.error("Error building cit module of type {}: {}", dataResult.getPartialOrThrow().getFirst().type(), e);
				continue;
			}

			module.GetModelLoader().FindCITs(manager);
			result.put(item, module);
		}

		return result;
	}

	static private @Nullable Item ItemFromModule(Identifier resourceId){
		String path = resourceId.getPath();
		path = path.substring("variant-cits/item".length() + 1, path.length() - ".json".length());

		Identifier itemId = Identifier.of(resourceId.getNamespace(), path);

		if (Registries.ITEM.containsId(itemId))
			return Registries.ITEM.get(itemId);
		else
			return null;
	}
}
