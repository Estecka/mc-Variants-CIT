package fr.estecka.variantscit.commands;

import java.util.HashMap;
import java.util.Map;
import fr.estecka.variantscit.modulebakers.IBakedModule;
import fr.estecka.variantscit.reload.EModuleContext;
import net.minecraft.util.Identifier;

public class CommandUtil
{
	static public final Map<EModuleContext, Map<Identifier, IBakedModule>> modules = Map.of(
		EModuleContext.ITEM_MODEL, new HashMap<>(),
		EModuleContext.EQUIPPABLE, new HashMap<>()
	);
}
