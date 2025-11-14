package fr.estecka.variantscit.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.modulebakers.IBakedModule;
import fr.estecka.variantscit.reload.EModuleContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import java.util.concurrent.CompletableFuture;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.command.argument.IdentifierArgumentType.identifier;
import static net.minecraft.command.argument.IdentifierArgumentType.getIdentifier;
import static fr.estecka.variantscit.commands.ModuleContextArgumentType.moduleContext;
import static fr.estecka.variantscit.commands.ModuleContextArgumentType.getModuleContext;

public class DumpCommand
{
	static public final Identifier ID = Identifier.of(VariantsCitMod.MODID, "dump");

	static public final String CONTEXT_ARG = "context";
	static public final String MODULE_ARG  = "module id";
	
	static public void	Register(){
		ClientCommandRegistrationCallback.EVENT.register(ID, DumpCommand::RegisterWith);
	}

	static public void	RegisterWith(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess){
		var root = literal(VariantsCitMod.MODID);
		var dump = literal("dump");


		dump.then(argument(CONTEXT_ARG, moduleContext())
			.suggests(DumpCommand::ContextAutofill)
			.then(argument(MODULE_ARG, identifier())
				.suggests(DumpCommand::ModuleAutofill)
				.executes(DumpCommand::DumpModule)
			)
		);

		root.then(dump);
		dispatcher.register(root);
	}


/******************************************************************************/
/* # Autofill                                                                 */
/******************************************************************************/

	static private CompletableFuture<Suggestions> ContextAutofill(final CommandContext<FabricClientCommandSource> context, final SuggestionsBuilder builder){
		for (EModuleContext moduleContext : EModuleContext.values())
			builder.suggest(moduleContext.name);
		return builder.buildFuture();
	}

	static private CompletableFuture<Suggestions> ModuleAutofill(final CommandContext<FabricClientCommandSource> context, final SuggestionsBuilder builder){
		EModuleContext moduleContext = getModuleContext(context, CONTEXT_ARG);
		for (Identifier id : CommandUtil.modules.get(moduleContext).keySet())
			builder.suggest(id.toString());
		return builder.buildFuture();
	}


/******************************************************************************/
/* # Command Handlers                                                         */
/******************************************************************************/

	static private int DumpModule(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		EModuleContext modContext = getModuleContext(context, CONTEXT_ARG);
		Identifier moduleId = context.getArgument(MODULE_ARG, Identifier.class);
		IBakedModule module = CommandUtil.modules.get(modContext).get(moduleId);

		if (module == null){
			context.getSource().sendError(Text.literal("No such module: "+modContext+" "+moduleId));
			return -1;
		}

		module.Dump(new CommandLogger(context));
		return 0;
	}

}
