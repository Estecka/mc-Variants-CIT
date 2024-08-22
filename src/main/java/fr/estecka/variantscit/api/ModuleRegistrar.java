package fr.estecka.variantscit.api;

import fr.estecka.variantscit.ModuleRegistry;
import com.google.common.base.Supplier;
import net.minecraft.util.Identifier;

public final class ModuleRegistrar
{
	static public void Register(Identifier moduleId, Supplier<ICitModule> module){
		ModuleRegistry.Register(moduleId, module);
	}

	static public void Register(Identifier moduleId, ISimpleCitModule module){
		ModuleRegistry.Register(moduleId, module);
	}
}
