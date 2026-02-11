package fr.estecka.variantscit.api;

import fr.estecka.variantscit.VCitRegistries;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.Identifier;

public final class ModuleRegistrar
{
	static public void Register(Identifier moduleId, MapCodec<? extends ICitModule> moduleCodec){
		VCitRegistries.RegisterSimpleModule(moduleId, moduleCodec);
	}

	static public void Register(Identifier moduleId, ISimpleCitModule module){
		VCitRegistries.RegisterSimpleModule(moduleId, module);
	}

	static public void Register(Identifier moduleId, ICitModule module){
		VCitRegistries.RegisterSimpleModule(moduleId, module);
	}
}
