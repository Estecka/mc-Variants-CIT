package fr.estecka.variantscit;

import java.nio.file.Path;
import java.util.Optional;

public interface IModLoader {
	void LetYourNameBeKnownToAll();
	Optional<Path> GetModFile(String path);
}
