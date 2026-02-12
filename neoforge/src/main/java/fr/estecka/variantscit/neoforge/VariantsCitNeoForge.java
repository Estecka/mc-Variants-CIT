package fr.estecka.variantscit.neoforge;

import fr.estecka.variantscit.VariantsCitMod;
import net.neoforged.fml.common.Mod;

@Mod("variantscit")
public final class VariantsCitNeoForge {
	public VariantsCitNeoForge() {
		VariantsCitMod.initialize(new NeoForgeModLoader());
	}
}
