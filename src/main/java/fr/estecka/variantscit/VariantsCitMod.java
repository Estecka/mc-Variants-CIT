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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import fr.estecka.variantscit.api.ACitModule;
import fr.estecka.variantscit.api.ModuleRegistry;
import fr.estecka.variantscit.modules.axolotl_bucket.AxolotlBucketModule;
import fr.estecka.variantscit.modules.enchanted_book.EnchantedBookModule;
import fr.estecka.variantscit.modules.enchanted_book.EnchantedBookLevelPredicate;
import fr.estecka.variantscit.modules.potion_effect.PotionEffectModule;
import fr.estecka.variantscit.modules.potion_effect.PotionLevelPredicate;


public class VariantsCitMod
implements ClientModInitializer, PreparableModelLoadingPlugin<Map<Item,ACitModule>>, DataLoader<Map<Item,ACitModule>>
{
	static public final String MODID = "variants-cit";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	static private Map<Item, ACitModule> MODULES = new HashMap<>();

	static public @Nullable ACitModule GetModule(Item itemType){
		return MODULES.get(itemType);
	}

	@Override
	public void onInitializeClient(){
		PreparableModelLoadingPlugin.register(this, this);
		ModuleRegistry.Register(Identifier.ofVanilla("stored_enchantments"), EnchantedBookModule::new);
		ModuleRegistry.Register(Identifier.ofVanilla("axolotl_variant"), AxolotlBucketModule::new);
		ModuleRegistry.Register(Identifier.ofVanilla("potion_effect"), PotionEffectModule::new);

		ModelPredicateProviderRegistry.register(Items.ENCHANTED_BOOK, Identifier.ofVanilla("level"), new EnchantedBookLevelPredicate());
		var potionPredicate = new PotionLevelPredicate();
		ModelPredicateProviderRegistry.register(Items.POTION, Identifier.ofVanilla("level"), potionPredicate);
		ModelPredicateProviderRegistry.register(Items.SPLASH_POTION, Identifier.ofVanilla("level"), potionPredicate);
		ModelPredicateProviderRegistry.register(Items.LINGERING_POTION, Identifier.ofVanilla("level"), potionPredicate);
	}

	@Override
	public void onInitializeModelLoader(Map<Item,ACitModule> modules, ModelLoadingPlugin.Context pluginContext){
		MODULES = modules;
		for (var entry : MODULES.entrySet()){
			ACitModule module = entry.getValue();
			Identifier itemId = Registries.ITEM.getId(entry.getKey());

			module.onInitializeModelLoader(pluginContext);
			LOGGER.info("Loaded {} CITs for item {}", module.GetModelCount(), itemId);
		}
	}

	@Override
	public CompletableFuture<Map<Item,ACitModule>> load(ResourceManager resourceManager, Executor executor){
		return CompletableFuture.supplyAsync(()->Reload(resourceManager), executor);
	}

	private Map<Item,ACitModule> Reload(ResourceManager manager){
		Map<Item,ACitModule> result = new HashMap<>();

		for (Map.Entry<Identifier, Resource> entry : manager.findResources("variant-cits/item", id->id.getPath().endsWith(".json")).entrySet()){
			Identifier resourceId = entry.getKey();
			Item item = ItemFromModule(resourceId);
			if (item == null)
				continue;

			JsonElement json;
			ACitModule module;
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
