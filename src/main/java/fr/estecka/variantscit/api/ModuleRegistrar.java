package fr.estecka.variantscit.api;

import fr.estecka.variantscit.ModuleRegistry;
import java.util.Map;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;

public final class ModuleRegistrar
{
	static public interface SpecialCitModuleFactory {
		ICitModule Build(Map<String, ModelIdentifier> specialModels);
	}
	
	static public interface ComplexCitModuleFactory<T> {
		ICitModule Build(Map<String, ModelIdentifier> specialModels, T customData);
	}

	static public <T> void Register(Identifier moduleId, ComplexCitModuleFactory<T> module, MapCodec<T> customDataCodec){
		ModuleRegistry.Register(moduleId, module, customDataCodec);
	}

	static public void Register(Identifier moduleId, SpecialCitModuleFactory module){
		ModuleRegistry.Register(moduleId, module);
	}

	static public void Register(Identifier moduleId, ISimpleCitModule module){
		ModuleRegistry.Register(moduleId, module);
	}
}
