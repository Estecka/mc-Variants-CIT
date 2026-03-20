package fr.estecka.variantscit.reload;

import java.util.Map;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import fr.estecka.variantscit.modules.IBakedModule;

/**
 * Contains data that is relevant to resource-loading and debugging, but not for
 * rendering.
 */
public record MetaModule (
	Identifier id,
	int priority,
	Set<Item> targets,
	String modelPrefix,
	Map<EModuleHook, IBakedModule> bakedModules
){}
