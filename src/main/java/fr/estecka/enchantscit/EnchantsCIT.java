package fr.estecka.enchantscit;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin.DataLoader;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnchantsCit
implements ClientModInitializer, PreparableModelLoadingPlugin<Set<Identifier>>, DataLoader<Set<Identifier>>
{
	static public final String MODID = "enchants-cit";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	static public final Identifier CIT_MIXED   = Identifier.of(MODID, "item/mixed_enchanted_book");
	static public final Identifier CIT_DEFAULT = Identifier.ofVanilla("item/filled_painting");
	static public final String MODEL_PREFIX = "item/enchanted_book/";

	static public final ModelIdentifier MODEL_DEFAULT = ModelIdentifier.ofInventoryVariant(CIT_DEFAULT);
	static public final ModelIdentifier MODEL_MIXED   = ModelIdentifier.ofInventoryVariant(CIT_MIXED);


	static private @NotNull Map<Identifier, ModelIdentifier> REGISTERED_MODEL_IDS = new HashMap<>();

	static public ModelIdentifier OfVariant(Identifier variantId){
		return REGISTERED_MODEL_IDS.getOrDefault(variantId, MODEL_DEFAULT);
	}


	@Override
	public void onInitializeClient(){
		PreparableModelLoadingPlugin.register(this, this);
	}

	@Override
	public void onInitializeModelLoader(Set<Identifier> modelIds, ModelLoadingPlugin.Context pluginContext){
		pluginContext.addModels(CIT_MIXED);
		
		LOGGER.info("Found {} enchanted-book CITs", modelIds.size());
		pluginContext.addModels(modelIds);
		REGISTERED_MODEL_IDS = new HashMap<>();
		for (Identifier id : modelIds)
			REGISTERED_MODEL_IDS.put(id, ModelIdentifier.ofInventoryVariant(id));
	}

	@Override
	public CompletableFuture<Set<Identifier>> load(ResourceManager manager, Executor executor){
		return CompletableFuture.supplyAsync(()->FindCITs(manager), executor);
	}

	static private Set<Identifier> FindCITs(ResourceManager manager){
		Set<Identifier> modelIds = new HashSet<>();

		for (Identifier id : manager.findResources("models/"+MODEL_PREFIX, id -> id.getPath().endsWith(".json")).keySet()) {
			String path = id.getPath();
			path = path.substring("models/".length(), path.length()-".png".length());
			modelIds.add(Identifier.of(id.getNamespace(), path));
		}

		return modelIds;
	}
}
