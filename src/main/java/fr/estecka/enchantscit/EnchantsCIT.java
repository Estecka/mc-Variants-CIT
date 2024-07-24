package fr.estecka.enchantscit;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin.DataLoader;
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

	static public final Identifier CIT_MIXED = Identifier.of(MODID, "item/mixed_enchanted_book");
	static public final String MODEL_PREFIX = "item/enchanted_book";
	

	/**
	 * Maps Enchantment identifier to the corresponding Model Id
	 */
	static private @NotNull Map<Identifier, Identifier> REGISTERED_MODEL_IDS = new HashMap<>();

	static public Identifier OfVariant(Identifier variantId){
		return REGISTERED_MODEL_IDS.get(variantId);
	}


	@Override
	public void onInitializeClient(){
		PreparableModelLoadingPlugin.register(this, this);
	}

	@Override
	public void onInitializeModelLoader(Set<Identifier> variantIds, ModelLoadingPlugin.Context pluginContext){
		pluginContext.addModels(CIT_MIXED);
		
		LOGGER.info("Found {} enchanted-book CITs", variantIds.size());
		REGISTERED_MODEL_IDS = new HashMap<>();
		for (Identifier id : variantIds){
			Identifier model = id.withPrefixedPath(MODEL_PREFIX+"/");
			REGISTERED_MODEL_IDS.put(id, model);
			pluginContext.addModels(model);
		}
	}

	@Override
	public CompletableFuture<Set<Identifier>> load(ResourceManager manager, Executor executor){
		return CompletableFuture.supplyAsync(()->FindCITs(manager), executor);
	}

	static private Set<Identifier> FindCITs(ResourceManager manager){
		Set<Identifier> variantIds = new HashSet<>();

		String folder = "models/" + MODEL_PREFIX;

		for (Identifier id : manager.findResources(folder, id -> id.getPath().endsWith(".json")).keySet()) {
			String path = id.getPath();
			path = path.substring(folder.length()+1, path.length()-".json".length());
			variantIds.add(Identifier.of(id.getNamespace(), path));
		}

		return variantIds;
	}
}
