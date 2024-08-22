package fr.estecka.variantscit.api;

import fr.estecka.variantscit.ModuleRegistry;
import java.util.Map;
import net.minecraft.util.Identifier;

public final class ModuleRegistrar
{
	static public interface ModuleFactory {
		ICitModule Build(Map<String, Identifier> specialModels);
	}

	static public void Register(Identifier moduleId, ModuleFactory module){
		ModuleRegistry.Register(moduleId, module);
	}

	static public void Register(Identifier moduleId, ISimpleCitModule module){
		ModuleRegistry.Register(moduleId, module);
	}
}
