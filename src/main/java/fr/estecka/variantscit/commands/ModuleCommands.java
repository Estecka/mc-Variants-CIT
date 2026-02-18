package fr.estecka.variantscit.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.modules.IBakedModule;
import fr.estecka.variantscit.reload.EModuleContext;
import fr.estecka.variantscit.reload.MetaModule;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.commands.arguments.ResourceLocationArgument.id;
import static fr.estecka.variantscit.commands.ModuleContextArgumentType.moduleContext;
import static fr.estecka.variantscit.commands.ModuleContextArgumentType.getModuleContext;

public class ModuleCommands
{
	static public final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(VariantsCitMod.MODID, "modules");

	static public final String CONTEXT_ARG = "context";
	static public final String MODULE_ARG  = "module id";
	
	static public void	Register(){
		ClientCommandRegistrationCallback.EVENT.register(ID, ModuleCommands::RegisterWith);
	}

	static public void	RegisterWith(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess){
		var module = argument(MODULE_ARG, id())
			.suggests(ModuleCommands::ModuleAutofill)
			.then(literal("dump").executes(c->Execute(c, ModuleCommands::Dump)))
			.then(literal("summary").executes(c->Execute(c, ModuleCommands::Summary)))
			.then(literal("walkthrough").executes(c->Execute(c, ModuleCommands::Walkthrough)))
			;

		var context = literal("module")
			.then(argument(CONTEXT_ARG, moduleContext())
				.suggests(ModuleCommands::ContextAutofill)
				.then(module)	
			);

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
		Stream<ResourceLocation> modules = VariantsCitMod.GetMeta().entrySet().stream()
			.filter(entry -> entry.getValue().bakedModules().get(moduleContext) != null)
			.map(Map.Entry::getKey)
			;

		SharedSuggestionProvider.suggestResource(modules, builder);

		return builder.buildFuture();
	}


/******************************************************************************/
/* # Command Handlers                                                         */
/******************************************************************************/

	@FunctionalInterface
	static private interface IModuleCommand
	{
		int Execute(CommandContext<FabricClientCommandSource> context, CommandLogger logger, IBakedModule module) throws CommandSyntaxException;
	}

	static private int Execute(CommandContext<FabricClientCommandSource> context, IModuleCommand command) throws CommandSyntaxException {
		EModuleContext moduleContext = getModuleContext(context, CONTEXT_ARG);
		ResourceLocation moduleId = context.getArgument(MODULE_ARG, ResourceLocation.class);
		MetaModule meta = VariantsCitMod.GetMeta().get(moduleId);
		IBakedModule module = VariantsCitMod.GetModule(moduleContext, moduleId);

		if (module == null){
			context.getSource().sendError(Component.literal("No such module: "+moduleContext+" "+moduleId));
			return -1;
		}

		CommandLogger logger = new CommandLogger(context, moduleContext, meta);
		return command.Execute(context, logger, module);
	}

	static private int Dump(CommandContext<FabricClientCommandSource> context, CommandLogger logger, IBakedModule module){
		module.Dump(logger);
		return 0;
	}

	static private int Summary(CommandContext<FabricClientCommandSource> context, CommandLogger logger, IBakedModule module){
		module.Summary(logger);
		return 0;
	}

	static private int Walkthrough(CommandContext<FabricClientCommandSource> cmdCtx, CommandLogger logger, IBakedModule module){
		ItemStack stack = cmdCtx.getSource().getPlayer().getMainHandItem();

		logger.Info("--------");
		logger.Info("Testing {} module {} on main-hand item: {} ({})",
			CommandLogger.PackData(logger.moduleContext()),
			CommandLogger.PackData(logger.metamodule().id()).withStyle(ChatFormatting.UNDERLINE),
			CommandLogger.ItemData(stack.getHoverName()).withStyle(ChatFormatting.UNDERLINE),
			CommandLogger.ItemData(stack.getItem())
		);
		logger.Info("----");

		if (!logger.metamodule().targets().contains(stack.getItem()))
			logger.Info(ChatFormatting.GOLD, "[WARN] This module would normally not be applied to items of type {}", stack.getItem());

		ResourceLocation modelId = module.Walkthrough(logger, stack);
		if (modelId != null){
			logger.Info("The module returned the model: {}", CommandLogger.PackData(modelId));
		}
		else
			logger.Info("The module failed to apply to the item.");

		return 0;
	}

}
