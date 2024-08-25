package fr.estecka.variantscit;

import java.util.HashMap;
import java.util.Map;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.api.ICitModule;
import fr.estecka.variantscit.api.ISimpleCitModule;
import fr.estecka.variantscit.api.ModuleRegistrar.ComplexCitModuleFactory;
import fr.estecka.variantscit.api.ModuleRegistrar.SpecialCitModuleFactory;
import net.minecraft.util.Identifier;

public final class ModuleRegistry
{
	@FunctionalInterface
	static private interface ManagerFactory {
		VariantManager build(ModuleDefinition definition, JsonObject customData) throws IllegalStateException;
	}

	static private final Map<Identifier, ManagerFactory> MODULE_TYPES = new HashMap<>();

	static public <T> void Register(Identifier type, ComplexCitModuleFactory<T> moduleFactory, MapCodec<T> codec){
		assert moduleFactory != null;
		Register(type, (ModuleDefinition config, JsonObject json) -> {
			var data = codec.fieldOf("parameters").decoder().decode(JsonOps.INSTANCE, json);
			ICitModule module = moduleFactory.Build(config.GetSpecialModelIds(), data.getOrThrow().getFirst());
			return new VariantManager(config, module);
		});
	}

	static public void Register(Identifier type, SpecialCitModuleFactory moduleFactory){
		assert moduleFactory != null;
		Register(type, (config,json)->new VariantManager(config, moduleFactory.Build(config.GetSpecialModelIds())));
	}

	static public void Register(Identifier type, ISimpleCitModule module){
		assert module != null;
		Register(type, (config,json)->new VariantManager(config, module));
	}

	static private void Register(Identifier type, ManagerFactory factory){
		assert type != null;
		assert factory != null;
		if (MODULE_TYPES.containsKey(type))
			throw new RuntimeException("Duplicate cit module registration for " + type.toString());

		MODULE_TYPES.put(type, factory);
	}

	static public VariantManager CreateManager(ModuleDefinition definition, JsonObject customData)
	throws IllegalArgumentException, IllegalStateException
	{
		assert definition != null;
		Identifier type = definition.type();

		if (!MODULE_TYPES.containsKey(type))
			throw new IllegalArgumentException("Unknown module type: " + type.toString());

		return MODULE_TYPES.get(type).build(definition, customData);
	}
}
