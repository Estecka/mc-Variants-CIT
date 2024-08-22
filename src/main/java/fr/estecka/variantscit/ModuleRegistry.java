package fr.estecka.variantscit;

import java.util.HashMap;
import java.util.Map;
import fr.estecka.variantscit.api.ICitModule;
import fr.estecka.variantscit.api.ISimpleCitModule;
import net.minecraft.util.Identifier;

public final class ModuleRegistry
{
	@FunctionalInterface
	static public interface ModuleFactory {
		VariantManager build(ModuleDefinition definition);
	}

	static private final Map<Identifier, ModuleFactory> MODULE_TYPES = new HashMap<>();

	static public void RegisterModule(Identifier type, ICitModule module){
		RegisterFactory(type, build->new VariantManager(build, module));
	}

	static public void RegisterModule(Identifier type, ISimpleCitModule module){
		RegisterFactory(type, build->new VariantManager(build, module));
	}

	static public void RegisterFactory(Identifier type, ModuleFactory factory){
		assert type != null;
		assert factory != null;
		if (MODULE_TYPES.containsKey(type))
			throw new RuntimeException("Duplicate cit module registration for " + type.toString());

		MODULE_TYPES.put(type, factory);
	}

	static public VariantManager CreateModule(ModuleDefinition definition)
	throws IllegalArgumentException
	{
		assert definition != null;
		Identifier type = definition.type();

		if (!MODULE_TYPES.containsKey(type))
			throw new IllegalArgumentException("Unknown module type: " + type.toString());

		return MODULE_TYPES.get(type).build(definition);
	}
}
