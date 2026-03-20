package fr.estecka.variantscit.commands;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

public class CommandUtil
{
	static protected int Error(CommandContext<FabricClientCommandSource> context, String message){
		context.getSource().sendError(Component.literal(message));
		return -1;
	}

	static protected int Info(CommandContext<FabricClientCommandSource> context, String message){
		context.getSource().sendFeedback(Component.literal(message));
		return 0;
	}

	static protected int Success(CommandContext<FabricClientCommandSource> context, String message){
		context.getSource().sendFeedback(Component.literal(message));
		return 1;
	}
}
