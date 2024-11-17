package fr.estecka.variantscit.api;

import fr.estecka.variantscit.ModuleRegistry;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.Identifier;

public final class ModuleRegistrar
{
	static public void Register(Identifier moduleId, MapCodec<? extends ICitModule> moduleCodec){
		ModuleRegistry.Register(moduleId, moduleCodec);
	}

	static public void Register(Identifier moduleId, ISimpleCitModule module){
		ModuleRegistry.Register(moduleId, module);
	}

	static public void Register(Identifier moduleId, ICitModule module){
		ModuleRegistry.Register(moduleId, module);
	}
}
