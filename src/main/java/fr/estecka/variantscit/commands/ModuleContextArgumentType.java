package fr.estecka.variantscit.commands;

import java.util.concurrent.CompletableFuture;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.estecka.variantscit.reload.EModuleContext;


public class ModuleContextArgumentType
implements ArgumentType<EModuleContext>
{
	static public ModuleContextArgumentType moduleContext(){
		return new ModuleContextArgumentType();
	}

	static public EModuleContext getModuleContext(CommandContext<?> context, String name) {
		return context.getArgument(name, EModuleContext.class);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		for (EModuleContext moduleContext : EModuleContext.values())
			builder.suggest(moduleContext.name);
		return builder.buildFuture();
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
