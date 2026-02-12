package fr.estecka.variantscit.neoforge;

import java.nio.file.Path;
import java.util.Optional;

import fr.estecka.variantscit.IModLoader;
import fr.estecka.variantscit.VariantsCitMod;
import net.neoforged.fml.ModLoadingContext;

public class NeoForgeModLoader
implements IModLoader
{
	@Override
	public void LetYourNameBeKnownToAll() {
		VariantsCitMod.LOGGER.error("I AM NEOFORGE");
	}

	@Override
	public Optional<Path> GetModFile(String pathName) {
		Path path = ModLoadingContext.get()
			.getActiveContainer()
			.getModInfo().getOwningFile().getFile()
			.findResource(pathName)
			;

		if (path.toFile().exists())
			return Optional.of(path);
		else
			return Optional.empty();
	}
}
