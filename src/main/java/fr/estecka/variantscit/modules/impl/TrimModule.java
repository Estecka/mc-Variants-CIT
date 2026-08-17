package fr.estecka.variantscit.modules.impl;

import fr.estecka.variantscit.modules.libraries.IVariantCitModule;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;

public class TrimModule
{
	static public final IVariantCitModule UNIT = ASimpleMonoComponentModule.Of(
		DataComponents.TRIM,
		trim -> {
			Identifier pattern  = trim.pattern ().unwrapKey().get().identifier();
			Identifier material = trim.material().unwrapKey().get().identifier();

			return pattern.withSuffix("_" + material.getPath());
		}
	);
}
