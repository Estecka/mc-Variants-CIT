package fr.estecka.variantscit.modules.impl;

import fr.estecka.variantscit.modules.libraries.IVariantCitModule;
import net.minecraft.core.component.DataComponents;

public class PotionEffectModule
{
	static public final IVariantCitModule UNIT = ASimpleMonoComponentModule.Of(
		DataComponents.POTION_CONTENTS,
		potion -> potion.hasEffects() ?
			potion.getAllEffects().iterator().next().getEffect().unwrapKey().get().identifier() :
			null
	);
}
