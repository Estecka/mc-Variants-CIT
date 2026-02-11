package fr.estecka.variantscit.fabric;

import net.fabricmc.api.ClientModInitializer;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.commands.AssetGenCommands;
import fr.estecka.variantscit.commands.ModuleCommands;


public class VariantsCitFabric
implements ClientModInitializer
{
	@Override
	public void onInitializeClient(){
		VariantsCitMod.onInitializeClient();

		ModuleCommands.Register();
		AssetGenCommands.Register();
	}
}
