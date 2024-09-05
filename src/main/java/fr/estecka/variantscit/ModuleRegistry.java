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
	static private interface ManagerFactory {
		VariantManager build(ModuleDefinition definition, JsonObject customData) throws IllegalStateException;
	}

	static private final Map<Identifier, ManagerFactory> MODULE_TYPES = new HashMap<>();

	@Deprecated
	static public <T> void Register(Identifier type, ComplexCitModuleFactory<T> moduleFactory, MapCodec<T> codec){
		assert moduleFactory != null;
		RegisterManager(type, (config, json) -> {
			var data = codec.decoder().decode(JsonOps.INSTANCE, json);
			ICitModule module = moduleFactory.Build(config.GetSpecialModelIds(), data.getOrThrow().getFirst());
			return new VariantManager(config, module);
		});
	}

	@Deprecated
	static public <T> void Register(Identifier type, ParameterizedCitModuleFactory<T> moduleFactory, MapCodec<T> codec){
		assert moduleFactory != null;
		RegisterManager(type, (config, json) -> {
			var data = codec.decoder().decode(JsonOps.INSTANCE, json);
			ICitModule module = moduleFactory.Build(data.getOrThrow().getFirst());
			return new VariantManager(config, module);
		});
	}

	static public void Register(Identifier type, MapCodec<? extends ICitModule> codec){
		assert codec != null;
		RegisterManager(type, (config, json) -> {
			ICitModule module = codec.decoder().decode(JsonOps.INSTANCE, json).getOrThrow().getFirst();
			return new VariantManager(config, module);
		});
	}

	@Deprecated
	static public void Register(Identifier type, SpecialCitModuleFactory moduleFactory){
		assert moduleFactory != null;
		RegisterManager(type, (config,json)->new VariantManager(config, moduleFactory.Build(config.GetSpecialModelIds())));
	}

	static public void Register(Identifier type, ICitModule module){
		assert module != null;
		RegisterManager(type, (config,json)->new VariantManager(config, module));
	}

	static private void RegisterManager(Identifier type, ManagerFactory factory){
		assert type != null;
		assert factory != null;
		if (MODULE_TYPES.containsKey(type))
			throw new RuntimeException("Duplicate cit module registration for " + type.toString());

		MODULE_TYPES.put(type, factory);
	}

	static public VariantManager CreateManager(ModuleDefinition definition, JsonObject customData)
	throws IllegalStateException
	{
		assert definition != null;
		Identifier type = definition.type();

		if (!MODULE_TYPES.containsKey(type))
			throw new IllegalStateException("Unknown module type: " + type.toString());

		return MODULE_TYPES.get(type).build(definition, customData);
	}
}
