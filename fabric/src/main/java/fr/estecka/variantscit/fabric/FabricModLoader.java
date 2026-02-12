package fr.estecka.variantscit.fabric;

import java.nio.file.Path;
import java.util.Optional;
import fr.estecka.variantscit.IModLoader;
import fr.estecka.variantscit.VariantsCitMod;
import net.fabricmc.loader.api.FabricLoader;

public class FabricModLoader
implements IModLoader
{
	@Override
	public void LetYourNameBeKnownToAll() {
		VariantsCitMod.LOGGER.error("I AM FABRIC");
	}

	@Override
	public Optional<Path> GetModFile(String path) {
		return FabricLoader.getInstance()
			.getModContainer(VariantsCitMod.MODID).get()
			.findPath(path)
			;
	}
}
