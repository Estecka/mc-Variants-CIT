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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.estecka.variantscit.api.IVariantsCitModule;


public class VariantsCitMod
implements ClientModInitializer
{
	static public final String MODID = "variants-cit";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	static private final Map<Item, ModelRegistry> MODULES = new HashMap<>();

	@Override
	public void onInitializeClient(){
		RegisterModules();

		for (ModelRegistry mod : MODULES.values())
			PreparableModelLoadingPlugin.register(mod, mod);
	}

	static private void RegisterModules(){
		List<IVariantsCitModule> declaredModules = FabricLoader.getInstance().getEntrypoints(MODID, IVariantsCitModule.class);
		Set<String> foundPathes = new HashSet<>();

		for (IVariantsCitModule mod : declaredModules)
		{
			if (foundPathes.contains(mod.GetModelPath()))
				LOGGER.warn("Multiple CIT modules are registered for the same path at `models/{}`", mod.GetModelPath());
			else
				foundPathes.add(mod.GetModelPath());

			ModelRegistry registry = new ModelRegistry(mod);
			for (Item itemType : mod.GetItemTypes())
			if  (MODULES.containsKey(itemType))
				LOGGER.error("Attempted multiple CIT modules registration for item type {}. Only one module will be kept.");
			else
				MODULES.put(itemType, registry);
		}
	}
}
