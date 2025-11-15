package fr.estecka.variantscit.commands;

import net.minecraft.item.ItemStack;

public interface IDebuggableLibrary
{
	default void Summary(CommandLogger logger){
		logger.Error("This module type does not support `summary`");
	}

	default void Dump(CommandLogger logger){
		logger.Error("This module type does not support `dump`");
	}

	default void Walkthrough(CommandLogger logger, ItemStack stack) {
		logger.Error("This module type does not support `waktrough`");
	}
}
