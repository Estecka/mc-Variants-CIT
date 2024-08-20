package fr.estecka.variantscit.api;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.Identifier;
import fr.estecka.variantscit.ModuleDefinition;

public class ModuleRegistry
{
	@FunctionalInterface
	static public interface ModuleFactory<T extends ACitModule> {
		T apply(ModuleDefinition definition);
	}

	static private Map<Identifier, ModuleFactory<?>> moduleTypes = new HashMap<>();

	static public void Register(Identifier type, ModuleFactory<?> factory){
		assert type != null;
		assert factory != null;
		if (moduleTypes.containsKey(type))
			throw new RuntimeException("Duplicate cit module registration for " + type.toString());

		moduleTypes.put(type, factory);
	}

	static public ACitModule CreateModule(ModuleDefinition definition)
	throws IllegalArgumentException
	{
		assert definition != null;
		Identifier type = definition.type();

		if (!moduleTypes.containsKey(type))
			throw new IllegalArgumentException("Unknown module type: " + type.toString());

		return moduleTypes.get(type).apply(definition);
	}
}
