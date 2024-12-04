package fr.estecka.variantscit;

import java.util.HashMap;
import java.util.Map;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.api.ICitModule;
import net.minecraft.util.Identifier;

public final class ModuleRegistry
{
	@FunctionalInterface
	static private interface ModuleFactory {
		ICitModule build(JsonObject customData) throws IllegalStateException;
	}

	static private final Map<Identifier, ModuleFactory> MODULE_TYPES = new HashMap<>();

	static public void Register(Identifier type, MapCodec<? extends ICitModule> codec){
		assert codec != null;
		RegisterFactory(type, (json) -> {
			return codec.decoder().decode(JsonOps.INSTANCE, json).getOrThrow().getFirst();
		});
	}

	static public void Register(Identifier type, ICitModule module){
		assert module != null;
		RegisterFactory(type, (json)->module);
	}

	static private void RegisterFactory(Identifier type, ModuleFactory factory){
		assert type != null;
		assert factory != null;
		if (MODULE_TYPES.containsKey(type))
			throw new RuntimeException("Duplicate cit module registration for " + type.toString());

		MODULE_TYPES.put(type, factory);
	}

	static public ICitModule CreateModule(Identifier type, JsonObject customData)
	throws IllegalStateException
	{
		assert type != null;

		if (!MODULE_TYPES.containsKey(type))
			throw new IllegalStateException("Unknown module type: " + type.toString());

		return MODULE_TYPES.get(type).build(customData);
	}
}
