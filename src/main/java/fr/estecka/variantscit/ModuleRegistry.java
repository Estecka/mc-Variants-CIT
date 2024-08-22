package fr.estecka.variantscit;

import java.util.HashMap;
import java.util.Map;
import fr.estecka.variantscit.api.ICitModule;
import net.minecraft.util.Identifier;

public final class ModuleRegistry
{
	@FunctionalInterface
	static public interface ModuleFactory {
		VariantManager build(ModuleDefinition definition);
	}

	static private final Map<Identifier, ModuleFactory> MODULE_TYPES = new HashMap<>();

	static public void Register(Identifier type, ICitModule module){
		assert module != null;
		Register(type, build->new VariantManager(build, module));
	}

	static private void Register(Identifier type, ModuleFactory factory){
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
