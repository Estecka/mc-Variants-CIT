package fr.estecka.variantscit.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
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
import net.minecraft.item.ItemStack;
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

public class ModuleCommands
{
	static public final Identifier ID = Identifier.of(VariantsCitMod.MODID, "dump");

	static public final String CONTEXT_ARG = "context";
	static public final String MODULE_ARG  = "module id";
	
	static public void	Register(){
		ClientCommandRegistrationCallback.EVENT.register(ID, ModuleCommands::RegisterWith);
	}

	static public void	RegisterWith(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess){
		var root = literal(VariantsCitMod.MODID);

		root.then(ModuleCommand("dump", ModuleCommands::Dump));
		root.then(ModuleCommand("summary", ModuleCommands::Summary));
		root.then(ModuleCommand("walkthrough", ModuleCommands::Walkthrough));

		dispatcher.register(root);
	}

	static private LiteralArgumentBuilder<FabricClientCommandSource> ModuleCommand(String name, IModuleCommand handler){
		return literal(name)
			.then(argument(CONTEXT_ARG, moduleContext())
				.suggests(ModuleCommands::ContextAutofill)
				.then(argument(MODULE_ARG, identifier())
					.suggests(ModuleCommands::ModuleAutofill)
					.executes(c->Execute(c, handler))
				)
		);
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

	@FunctionalInterface
	static private interface IModuleCommand
	{
		int Execute(CommandContext<FabricClientCommandSource> context, IBakedModule module) throws CommandSyntaxException;
	}

	static private int Execute(CommandContext<FabricClientCommandSource> context, IModuleCommand command) throws CommandSyntaxException {
		EModuleContext modContext = getModuleContext(context, CONTEXT_ARG);
		Identifier moduleId = context.getArgument(MODULE_ARG, Identifier.class);
		IBakedModule module = CommandUtil.modules.get(modContext).get(moduleId);

		if (module == null){
			context.getSource().sendError(Text.literal("No such module: "+modContext+" "+moduleId));
			return -1;
		}

		return command.Execute(context, module);
	}

	static private int Dump(CommandContext<FabricClientCommandSource> context, IBakedModule module){
		module.Dump(new CommandLogger(context));
		return 0;
	}

	static private int Summary(CommandContext<FabricClientCommandSource> context, IBakedModule module){
		module.Summary(new CommandLogger(context));
		return 0;
	}

	static private int Walkthrough(CommandContext<FabricClientCommandSource> context, IBakedModule module){
		ItemStack stack = context.getSource().getPlayer().getMainHandStack();
		CommandLogger logger = new CommandLogger(context);
		EModuleContext modContext = getModuleContext(context, CONTEXT_ARG);
		Identifier moduleId = context.getArgument(MODULE_ARG, Identifier.class);

		logger.Info("Applying {} module {} to item {} ({})", modContext, moduleId, stack.getName().getString(), stack.getItem());
		Identifier modelId = module.Walkthrough(new CommandLogger(context), stack);
		if (modelId != null){
			logger.Info(
				Text.literal("The module returned the model: ")
				    .append(Text.literal(modelId.toString()).formatted(Formatting.YELLOW))
			);
		}
		else
			logger.Info("The module failed to apply to the item.");

		return 0;
	}

}
