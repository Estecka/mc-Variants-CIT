package fr.estecka.variantscit.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.modulebakers.IBakedModule;
import fr.estecka.variantscit.reload.EModuleContext;
import fr.estecka.variantscit.reload.ModuleLoader.MetaModule;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
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
		var module = argument(MODULE_ARG, identifier())
			.suggests(ModuleCommands::ModuleAutofill)
			.then(literal("dump").executes(c->Execute(c, ModuleCommands::Dump)))
			.then(literal("summary").executes(c->Execute(c, ModuleCommands::Summary)))
			.then(literal("walkthrough").executes(c->Execute(c, ModuleCommands::Walkthrough)))
			;

		var context = argument(CONTEXT_ARG, moduleContext())
			.suggests(ModuleCommands::ContextAutofill)
			.then(module)
			;

		var root = literal(VariantsCitMod.MODID)
			.then(context)
			;

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
		for (var entry : VariantsCitMod.GetMeta().entrySet()){
			MetaModule meta = entry.getValue();
			boolean exist = switch (moduleContext){
				default -> false;
				case ITEM_MODEL -> meta.itemModule().isPresent();
				case EQUIPPABLE -> meta.equipModule().isPresent();
			};

			if (exist)
				builder.suggest(entry.getKey().toString());
		}

		return builder.buildFuture();
	}


/******************************************************************************/
/* # Command Handlers                                                         */
/******************************************************************************/

	@FunctionalInterface
	static private interface IModuleCommand
	{
		int Execute(CommandContext<FabricClientCommandSource> context, MetaModule meta, IBakedModule module) throws CommandSyntaxException;
	}

	static private int Execute(CommandContext<FabricClientCommandSource> context, IModuleCommand command) throws CommandSyntaxException {
		EModuleContext moduleContext = getModuleContext(context, CONTEXT_ARG);
		Identifier moduleId = context.getArgument(MODULE_ARG, Identifier.class);
		MetaModule meta = VariantsCitMod.GetMeta().get(moduleId);
		IBakedModule module = VariantsCitMod.GetModule(moduleContext, moduleId);

		if (module == null){
			context.getSource().sendError(Text.literal("No such module: "+moduleContext+" "+moduleId));
			return -1;
		}

		return command.Execute(context, meta, module);
	}

	static private int Dump(CommandContext<FabricClientCommandSource> context, MetaModule meta, IBakedModule module){
		module.Dump(new CommandLogger(context));
		return 0;
	}

	static private int Summary(CommandContext<FabricClientCommandSource> context, MetaModule meta, IBakedModule module){
		module.Summary(new CommandLogger(context));
		return 0;
	}

	static private int Walkthrough(CommandContext<FabricClientCommandSource> cmdCtx, MetaModule meta, IBakedModule module){
		ItemStack stack = cmdCtx.getSource().getPlayer().getMainHandStack();
		CommandLogger logger = new CommandLogger(cmdCtx);
		EModuleContext modCtx = getModuleContext(cmdCtx, CONTEXT_ARG);
		Identifier moduleId = cmdCtx.getArgument(MODULE_ARG, Identifier.class);

		logger.Info("Applying {} module {} to item {} ({})", modCtx, moduleId, stack.getName().getString(), stack.getItem());

		WalkthroughData debugInfo = new WalkthroughData(logger, modCtx, meta);
		Identifier modelId = module.Walkthrough(debugInfo, stack);
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
