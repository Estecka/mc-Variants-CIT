package fr.estecka.variantscit.modules.impl;

import fr.estecka.variantscit.modules.libraries.IVariantCitModule;
import net.minecraft.core.component.DataComponents;

public class PotionTypeModule
{
	static public final IVariantCitModule UNIT = ASimpleMonoComponentModule.Of(
		DataComponents.POTION_CONTENTS,
		potion -> potion.potion().isEmpty() ? 
			null :
			potion.potion().get().unwrapKey().get().location()
	);
}
