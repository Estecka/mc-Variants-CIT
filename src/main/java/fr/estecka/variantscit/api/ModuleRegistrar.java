package fr.estecka.variantscit.api;

import fr.estecka.variantscit.VCitRegistries;
import net.minecraft.resources.ResourceLocation;
import com.mojang.serialization.MapCodec;

public final class ModuleRegistrar
{
	static public void Register(ResourceLocation moduleId, MapCodec<? extends ICitModule> moduleCodec){
		VCitRegistries.RegisterSimpleModule(moduleId, moduleCodec);
	}

	static public void Register(ResourceLocation moduleId, ISimpleCitModule module){
		VCitRegistries.RegisterSimpleModule(moduleId, module);
	}

	static public void Register(ResourceLocation moduleId, ICitModule module){
		VCitRegistries.RegisterSimpleModule(moduleId, module);
	}
}
