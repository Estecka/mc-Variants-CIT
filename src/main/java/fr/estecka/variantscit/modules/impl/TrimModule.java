package fr.estecka.variantscit.modules.impl;

import fr.estecka.variantscit.modules.libraries.IVariantCitModule;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;

public class TrimModule
{
	static public final IVariantCitModule UNIT = ASimpleMonoComponentModule.Of(
		DataComponents.TRIM,
		trim -> {
			ResourceLocation pattern  = trim.pattern ().unwrapKey().get().location();
			ResourceLocation material = trim.material().unwrapKey().get().location();

			return pattern.withSuffix("_" + material.getPath());
		}
	);
}
