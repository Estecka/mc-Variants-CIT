package fr.estecka.variantscit;

import java.util.HashMap;
import java.util.Map;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.api.ICitModule;
import fr.estecka.variantscit.api.ModuleRegistrar.ComplexCitModuleFactory;
import fr.estecka.variantscit.api.ModuleRegistrar.ParameterizedCitModuleFactory;
import fr.estecka.variantscit.api.ModuleRegistrar.SpecialCitModuleFactory;
import net.minecraft.util.Identifier;

public final class ModuleRegistry
{
	@FunctionalInterface
	static private interface ModuleFactory {
		ICitModule build(ModuleDefinition definition, JsonObject customData) throws IllegalStateException;
	}

	static private final Map<Identifier, ModuleFactory> MODULE_TYPES = new HashMap<>();

	@Deprecated
	static public <T> void Register(Identifier type, ComplexCitModuleFactory<T> moduleFactory, MapCodec<T> codec){
		assert moduleFactory != null;
		RegisterManager(type, (config, json) -> {
			var data = codec.decoder().decode(JsonOps.INSTANCE, json);
			return moduleFactory.Build(config.GetSpecialModelIds(), data.getOrThrow().getFirst());
		});
	}

	@Deprecated
	static public <T> void Register(Identifier type, ParameterizedCitModuleFactory<T> moduleFactory, MapCodec<T> codec){
		assert moduleFactory != null;
		RegisterManager(type, (config, json) -> {
			var data = codec.decoder().decode(JsonOps.INSTANCE, json);
			return moduleFactory.Build(data.getOrThrow().getFirst());
		});
	}

	static public void Register(Identifier type, MapCodec<? extends ICitModule> codec){
		assert codec != null;
		RegisterManager(type, (config, json) -> {
			return codec.decoder().decode(JsonOps.INSTANCE, json).getOrThrow().getFirst();
		});
	}

	@Deprecated
	static public void Register(Identifier type, SpecialCitModuleFactory moduleFactory){
		assert moduleFactory != null;
		RegisterManager(type, (config,json)->moduleFactory.Build(config.GetSpecialModelIds()));
	}

	static public void Register(Identifier type, ICitModule module){
		assert module != null;
		RegisterManager(type, (config,json)-> module);
	}

	static private void RegisterManager(Identifier type, ModuleFactory factory){
		assert type != null;
		assert factory != null;
		if (MODULE_TYPES.containsKey(type))
			throw new RuntimeException("Duplicate cit module registration for " + type.toString());

		MODULE_TYPES.put(type, factory);
	}

	static public ICitModule CreateModule(ModuleDefinition definition, JsonObject customData)
	throws IllegalStateException
	{
		assert definition != null;
		Identifier type = definition.type();

		if (!MODULE_TYPES.containsKey(type))
			throw new IllegalStateException("Unknown module type: " + type.toString());

		return MODULE_TYPES.get(type).build(definition, customData);
	}
}
