package fr.estecka.variantscit.neoforge;

import java.nio.file.Path;
import java.util.Optional;
import fr.estecka.variantscit.VariantsCitMod;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;

@Mod(VariantsCitNeoForge.FORGE_MODID)
public final class VariantsCitNeoForge
extends VariantsCitMod
{
	static public final String FORGE_MODID = "variantscit";

	public VariantsCitNeoForge() {
		VariantsCitMod.initialize(this);
	}
	
	@Override
	public void LetYourNameBeKnownToAll() {
		VariantsCitMod.LOGGER.error("I AM NEOFORGE");
	}

	@Override
	public Optional<Path> GetFile(String pathName) {
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
