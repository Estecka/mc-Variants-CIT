package fr.estecka.variantscit.modules.impl;

import fr.estecka.variantscit.modules.libraries.IVariantCitModule;
import net.minecraft.core.component.DataComponents;

public class TrimPatternModule
{
	static public final IVariantCitModule UNIT = ASimpleMonoComponentModule.Of(
		DataComponents.TRIM,
		trim->trim.pattern().unwrapKey().get().identifier()
	);
}
