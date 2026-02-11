package fr.estecka.variantscit.neoforge;

import net.neoforged.fml.common.Mod;

import fr.estecka.variantscit.ExampleMod;

@Mod(ExampleMod.MOD_ID)
public final class ExampleModNeoForge {
    public ExampleModNeoForge() {
        // Run our common setup.
        ExampleMod.init();
    }
}
