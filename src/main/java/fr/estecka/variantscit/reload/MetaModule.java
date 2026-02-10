package fr.estecka.variantscit.reload;

import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import fr.estecka.variantscit.modules.IBakedModule;

/**
 * Contains data that is relevant to resource-loading and debugging, but not for
 * rendering.
 */
public record MetaModule (
	ResourceLocation id,
	int priority,
	Set<Item> targets,
	String modelPrefix,
	Optional<IBakedModule> itemModule,
	Optional<IBakedModule> equipModule
){}
