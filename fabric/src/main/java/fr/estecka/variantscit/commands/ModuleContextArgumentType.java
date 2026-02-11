package fr.estecka.variantscit.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.estecka.variantscit.reload.EModuleContext;


public class ModuleContextArgumentType
implements ArgumentType<EModuleContext>
{
	static public ModuleContextArgumentType moduleContext(){
		return new ModuleContextArgumentType();
	}

	public static EModuleContext getModuleContext(CommandContext<?> context, String name) {
		return context.getArgument(name, EModuleContext.class);
	}

	@Override
	public EModuleContext parse(StringReader reader)
	throws CommandSyntaxException
	{
		String literal = reader.readUnquotedString();

		return switch (literal) {
			case "item_model" -> EModuleContext.ITEM_MODEL;
			case "equippable" -> EModuleContext.EQUIPPABLE;
			default -> throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().createWithContext(reader, literal);
		};
	}
}
