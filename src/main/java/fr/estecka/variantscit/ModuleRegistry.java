package fr.estecka.variantscit;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import fr.estecka.variantscit.api.ICitModule;
import fr.estecka.variantscit.api.ISimpleCitModule;
import net.minecraft.util.Identifier;

public final class ModuleRegistry
{
	@FunctionalInterface
	static public interface ManagerFactory {
		VariantManager build(ModuleDefinition definition);
	}

	static private final Map<Identifier, ManagerFactory> MODULE_TYPES = new HashMap<>();

	static public void Register(Identifier type, Supplier<ICitModule> moduleFactory){
		assert moduleFactory != null;
		Register(type, (ManagerFactory)config->new VariantManager(config, moduleFactory.get()));
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
