package fr.estecka.variantscit;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.estecka.variantscit.api.ACitModule;


public class VariantsCitMod
implements ClientModInitializer
{
	static public final String MODID = "variants-cit";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	static private final Map<Item, ACitModule> MODULES = new HashMap<>();

	static public @Nullable ACitModule GetModule(Item itemType){
		return MODULES.get(itemType);
	}

	@Override
	public void onInitializeClient(){
		RegisterModules();

		for (ACitModule mod : MODULES.values()){
			CitLoader loader = mod.GetModelLoader();
			PreparableModelLoadingPlugin.register(loader, loader);
		}
	}

	static private void RegisterModules(){
		List<ACitModule> declaredModules = FabricLoader.getInstance().getEntrypoints(MODID, ACitModule.class);
		Set<String> foundPathes = new HashSet<>();

		for (ACitModule module : declaredModules)
		{
			if (foundPathes.contains(module.modelsPath))
				LOGGER.warn("Multiple CIT modules are registered for the same path at `models/{}`", module.modelsPath);
			else
				foundPathes.add(module.modelsPath);

			for (Item itemType : module.validItems)
			if  (MODULES.containsKey(itemType))
				LOGGER.error("Attempted multiple CIT modules registration for item type {}. Only one module will be kept.");
			else
				MODULES.put(itemType, module);
		}
	}
}
