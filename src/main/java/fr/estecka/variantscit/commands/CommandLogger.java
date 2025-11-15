package fr.estecka.variantscit.commands;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

public record CommandLogger(
	CommandContext<FabricClientCommandSource> commandContext
)
{
	public void Info(String format, Object... args){
		String result = format;
		for (Object o : args)
			result = result.replaceFirst("\\{\\}", o.toString());
		this.Info(result);
	}

	public void Info(String message){
		this.Info(Text.literal(message));
	}

	public void Info(Text message){
		commandContext.getSource().sendFeedback(message);
	}

	public void Error(String message){
		this.Error(Text.literal(message));
	}

	public void Error(Text message){
		commandContext.getSource().sendError(message);
	}

}
