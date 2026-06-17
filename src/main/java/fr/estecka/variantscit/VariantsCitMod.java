package fr.estecka.variantscit;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.resources.ResourceLocation;
import fr.estecka.variantscit.reload.ModuleLoader;
import fr.estecka.variantscit.commands.AssetGenCommands;
import fr.estecka.variantscit.commands.CacheCommands;
import fr.estecka.variantscit.commands.ModuleCommands;
import fr.estecka.variantscit.commands.ModuleTreeCommands;


public class VariantsCitMod
implements ClientModInitializer
{
	static public final String MODID = "variants-cit";
	public static final LabelledLogger LOGGER = new LabelledLogger();

	static public final EquippableCache EQUIPABLES = new EquippableCache();
	static private ModuleRepository MODULES = new ModuleRepository();

	static public ResourceLocation Identifier(String path){
		return ResourceLocation.fromNamespaceAndPath(MODID, path);
	}

	static public ModuleRepository GetModules(){
		return MODULES;
	}

	@Override
	public void onInitializeClient(){
		ModuleCommands.Register();
		AssetGenCommands.Register();
		CacheCommands.Register();
		ModuleTreeCommands.Register();
	}

	static public void OnResourceReload(ModuleLoader.Result result){
		EQUIPABLES.Clear();
		MODULES = new ModuleRepository(result);
	}

}
