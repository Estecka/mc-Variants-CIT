package fr.estecka.variantscit.reload;

import java.util.Optional;
import java.util.Set;
import fr.estecka.variantscit.modules.libraries.VariantLibrary;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

/**
 * Contains data that is relevant to resource-loading and debugging, but not for
 * rendering.
 */
public record MetaModule (
	Identifier id,
	int priority,
	Set<Item> targets,
	Optional<VariantLibrary> itemLibrary,
	Optional<VariantLibrary> equipLibrary,
	UnbakedModule<?> parameters
){}
