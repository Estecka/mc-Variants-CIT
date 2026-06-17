package fr.estecka.variantscit.commands;

import org.jetbrains.annotations.Nullable;
import com.mojang.brigadier.context.CommandContext;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;


public class CommandLogger
{
	public final CommandContext<FabricClientCommandSource> commandContext;

	public CommandLogger(CommandContext<FabricClientCommandSource> commandContext){
		this.commandContext = commandContext;
	}

/******************************************************************************/
/* # Generic logging                                                          */
/******************************************************************************/

	static public MutableComponent TextOf(Object obj){
		if (obj instanceof Component text)
			return text.copy();
		else if (obj instanceof IDataContainer data)
			return data.toText();
		else
			return Component.literal(String.valueOf(obj));
	}

	static public MutableComponent TextFormat(ChatFormatting style, String format, Object... args){
		MutableComponent result = Component.empty().withStyle(style);

		String remainder = format;
		int i = 0;
		int argPos;
		while (i < args.length && 0 <= (argPos=remainder.indexOf("{}"))){
			result.append(remainder.substring(0, argPos));
			result.append(TextOf(args[i]));

			++i;
			remainder = remainder.substring(argPos + 2);
		}

		if (!remainder.isEmpty())
			result.append(remainder);

		return result;
	}

	public MutableComponent TextFormat(String format, Object... args){
		return TextFormat(ChatFormatting.RESET, format, args);
	}


	public void Info(ChatFormatting formatting, String format, Object... args){
		this.Info(TextFormat(formatting, format, args));
	}

	public void Info(String format, Object... args){
		this.Info(TextFormat(ChatFormatting.RESET, format, args));
	}

	public void Info(String message){
		this.Info(Component.literal(message));
	}

	public void Info(Component message){
		commandContext.getSource().sendFeedback(message);
	}


	public void Error(ChatFormatting formatting, String format, Object... args){
		this.Error(TextFormat(formatting, format, args));
	}

	public void Error(String format, Object... args){
		this.Error(TextFormat(ChatFormatting.RESET, format, args));
	}

	public void Error(String message){
		this.Error(Component.literal(message));
	}

	public void Error(Component message){
		commandContext.getSource().sendError(message);
	}


/******************************************************************************/
/* # Preformatted                                                             */
/******************************************************************************/

	static public MutableComponent ItemData(@Nullable Object variant){
		return ItemData(variant, "null");
	}

	static public MutableComponent ItemData(@Nullable Object variant, String fallback){
		if (variant == null)
			return Component.literal(fallback).withStyle(ChatFormatting.RED);
		else
			return TextOf(variant).withStyle(ChatFormatting.AQUA);
	}

	static public MutableComponent PackData(Object variant){
		return TextOf(variant).withStyle(ChatFormatting.YELLOW);
	}
}
