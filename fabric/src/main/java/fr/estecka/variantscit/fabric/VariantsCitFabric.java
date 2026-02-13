package fr.estecka.variantscit.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Path;
import java.util.Optional;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.commands.AssetGenCommands;
import fr.estecka.variantscit.commands.ModuleCommands;


public class VariantsCitFabric
extends VariantsCitMod
implements ClientModInitializer
{
	@Override
	public void onInitializeClient(){
		VariantsCitMod.initialize(this);

		ModuleCommands.Register();
		AssetGenCommands.Register();

		var pathes = FabricLoader.getInstance()
			.getModContainer(MODID).get()
			.getRootPaths()
			;

		LOGGER.info("Path count {}", pathes.size());
		for (Path p : pathes)
			LOGGER.info("- {}", p);
	}

	@Override
	public void LetYourNameBeKnownToAll() {
		VariantsCitMod.LOGGER.error("I AM FABRIC");
	}

	@Override
	public Optional<Path> GetFile(String path) {
		return FabricLoader.getInstance()
			.getModContainer(VariantsCitMod.MODID).get()
			.findPath(path)
			;
	}
}
