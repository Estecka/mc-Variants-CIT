package fr.estecka.variantscit.api;

import fr.estecka.variantscit.ModuleRegistry;
import java.util.Map;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;

public final class ModuleRegistrar
{
	/**
	 * @deprecated Modules may now  access the list  of special models  from the
	 * {@link IVariantManager} provided to {@link ICitModule#GetItemModel}.
	 */
	@Deprecated
	static public interface SpecialCitModuleFactory {
		ICitModule Build(Map<String, ModelIdentifier> specialModels);
	}

	/**
	 * @deprecated Modules may now be built directly from their codec.
	 */
	@Deprecated
	static public interface ParameterizedCitModuleFactory<T> {
		ICitModule Build(T customData);
	}

	/**
	 * @deprecated See {@link SpecialCitModuleFactory}
	 */
	@Deprecated
	static public interface ComplexCitModuleFactory<T> {
		ICitModule Build(Map<String, ModelIdentifier> specialModels, T customData);
	}


	/**
	 * @deprecated See {@link SpecialCitModuleFactory}
	 */
	@Deprecated
	static public <T> void Register(Identifier moduleId, ComplexCitModuleFactory<T> module, MapCodec<T> customDataCodec){
		ModuleRegistry.Register(moduleId, module, customDataCodec);
	}

	/**
	 * @deprecated See {@link ParameterizedCitModuleFactory}
	 */
	@Deprecated
	static public <T> void Register(Identifier moduleId, ParameterizedCitModuleFactory<T> module, MapCodec<T> customDataCodec){
		ModuleRegistry.Register(moduleId, module, customDataCodec);
	}

	static public void Register(Identifier moduleId, MapCodec<? extends ICitModule> moduleCodec){
		ModuleRegistry.Register(moduleId, moduleCodec);
	}


	/**
	 * @deprecated See {@link SpecialCitModuleFactory}
	 */
	@Deprecated
	static public void Register(Identifier moduleId, SpecialCitModuleFactory module){
		ModuleRegistry.Register(moduleId, module);
	}

	static public void Register(Identifier moduleId, ISimpleCitModule module){
		ModuleRegistry.Register(moduleId, module);
	}

	static public void Register(Identifier moduleId, ICitModule module){
		ModuleRegistry.Register(moduleId, module);
	}
}
