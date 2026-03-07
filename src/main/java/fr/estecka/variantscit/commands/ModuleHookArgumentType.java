package fr.estecka.variantscit.commands;

import java.util.concurrent.CompletableFuture;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.estecka.variantscit.reload.EModuleHook;


public class ModuleHookArgumentType
implements ArgumentType<EModuleHook>
{
	static public ModuleHookArgumentType moduleHook(){
		return new ModuleHookArgumentType();
	}

	static public EModuleHook getModuleHook(CommandContext<?> context, String name) {
		return context.getArgument(name, EModuleHook.class);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		for (EModuleHook hook : EModuleHook.values())
			builder.suggest(hook.name);
		return builder.buildFuture();
	}

	@Override
	public EModuleHook parse(StringReader reader)
	throws CommandSyntaxException
	{
		String literal = reader.readUnquotedString();

		return switch (literal) {
			case "item_model" -> EModuleHook.ITEM_MODEL;
			case "equippable" -> EModuleHook.EQUIPPABLE;
			default -> throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().createWithContext(reader, literal);
		};
	}
}
