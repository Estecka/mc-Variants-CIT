package fr.estecka.variantscit;

import java.util.HashMap;
import java.util.Map;
import fr.estecka.variantscit.api.ISimpleCitModule;
import fr.estecka.variantscit.api.ModuleRegistrar.ModuleFactory;
import net.minecraft.util.Identifier;

public final class ModuleRegistry
{
	@FunctionalInterface
	static private interface ManagerFactory {
		VariantManager build(ModuleDefinition definition);
	}

	static private final Map<Identifier, ManagerFactory> MODULE_TYPES = new HashMap<>();

	static public void Register(Identifier type, ModuleFactory moduleFactory){
		assert moduleFactory != null;
		Register(type, (ManagerFactory)config->new VariantManager(config, moduleFactory.Build(config.GetSpecialModelIds())));
	}

	static public void Register(Identifier type, ISimpleCitModule module){
		assert module != null;
		Register(type, (ManagerFactory)config->new VariantManager(config, module));
	}

	static private void Register(Identifier type, ManagerFactory factory){
		assert type != null;
		assert factory != null;
		if (MODULE_TYPES.containsKey(type))
			throw new RuntimeException("Duplicate cit module registration for " + type.toString());

		MODULE_TYPES.put(type, factory);
	}

	static public VariantManager CreateManager(ModuleDefinition definition)
	throws IllegalArgumentException
	{
		assert definition != null;
		Identifier type = definition.type();

		if (!MODULE_TYPES.containsKey(type))
			throw new IllegalArgumentException("Unknown module type: " + type.toString());

		return MODULE_TYPES.get(type).build(definition);
	}
}
