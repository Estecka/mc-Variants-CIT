package fr.estecka.variantscit.api;

import fr.estecka.variantscit.ModuleRegistry;
import net.minecraft.util.Identifier;

public final class ModuleRegistrar
{
	static public void Register(Identifier moduleId, ICitModule module){
		ModuleRegistry.RegisterModule(moduleId, module);
	}

	static public void Register(Identifier moduleId, IVariantProvider module){
		ModuleRegistry.RegisterModule(moduleId, module);
	}
}
