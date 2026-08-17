package fr.estecka.variantscit.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.DataResult;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.modules.IBakedModule;
import fr.estecka.variantscit.modules.cache.ICacheKey;
import fr.estecka.variantscit.reload.EModuleHook;
import fr.estecka.variantscit.reload.MetaModule;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
// import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
// import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
// import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
// import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
// import static com.mojang.brigadier.arguments.StringArgumentType.getString;
// import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.commands.arguments.IdentifierArgument.id;
import static fr.estecka.variantscit.commands.ModuleHookArgumentType.moduleHook;
import static fr.estecka.variantscit.commands.ModuleHookArgumentType.getModuleHook;

public class ModuleCommands
extends CommandUtil
{
	static public final Identifier ID = Identifier.fromNamespaceAndPath(VariantsCitMod.MODID, "modules");

	static public final String HOOK_ARG    = "hook";
	static public final String MODULE_ARG  = "module id";

	static public void	Register(){
		ClientCommandRegistrationCallback.EVENT.register(ID, ModuleCommands::RegisterWith);
	}

	static public void	RegisterWith(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess){
		var walkthrough = literal("walkthrough").executes(c->Execute(c, Walkthrough(IClientEntitySelector::GetSelf)))
			.then(literal("self")          .executes(c->Execute(c, Walkthrough(IClientEntitySelector::GetSelf))))
			.then(literal("nearest_item")  .executes(c->Execute(c, Walkthrough(IClientEntitySelector::GetGroundItem))))
			.then(literal("nearest_player").executes(c->Execute(c, Walkthrough(IClientEntitySelector::GetPlayer))))
			;

		var module = argument(MODULE_ARG, id())
			.suggests(ModuleCommands::ModuleAutofill)
			.then(literal("dump").executes(c->Execute(c, ModuleCommands::Dump)))
			.then(literal("summary").executes(c->Execute(c, ModuleCommands::Summary)))
			.then(walkthrough)
			;

		var hook = literal("module")
			.then(argument(HOOK_ARG, moduleHook())
				.then(module)
			);

		var root = literal(VariantsCitMod.MODID)
			.then(hook)
			;

		dispatcher.register(root);
	}


/******************************************************************************/
/* # Autofill                                                                 */
/******************************************************************************/

	static private CompletableFuture<Suggestions> ModuleAutofill(final CommandContext<FabricClientCommandSource> context, final SuggestionsBuilder builder){
		EModuleHook hook = getModuleHook(context, HOOK_ARG);
		Stream<Identifier> modules = VariantsCitMod.GetModules().GetAvailableModules(hook);

		SharedSuggestionProvider.suggestResource(modules, builder);

		return builder.buildFuture();
	}


/******************************************************************************/
/* # Command Handlers                                                         */
/******************************************************************************/

	@FunctionalInterface
	static private interface IModuleCommand
	{
		int Execute(CommandContext<FabricClientCommandSource> context, WalktroughLogger logger, IBakedModule module) throws CommandSyntaxException;
	}

	static private IModuleCommand Walkthrough(IClientEntitySelector target){
		return (c,l,m)->Walkthrough(c,l,m, target);
	}

	static private int Execute(CommandContext<FabricClientCommandSource> context, IModuleCommand command) throws CommandSyntaxException {
		EModuleHook hook = getModuleHook(context, HOOK_ARG);
		Identifier moduleId = context.getArgument(MODULE_ARG, Identifier.class);
		MetaModule meta;
		DataResult<MetaModule> optMeta = VariantsCitMod.GetModules().GetMeta(moduleId);
		if (optMeta.isError())
			return Error(context, optMeta.error().get().message());
		else
			meta = optMeta.getOrThrow();

		IBakedModule module = meta.bakedModules().get(hook);
		if (module == null)
			return Error(context, "No hook "+hook+" for module "+moduleId);

		WalktroughLogger logger = new WalktroughLogger(context, hook, meta, "");
		return command.Execute(context, logger, module);
	}

	static private int Dump(CommandContext<FabricClientCommandSource> context, WalktroughLogger logger, IBakedModule module){
		module.Dump(logger);
		return 0;
	}

	static private int Summary(CommandContext<FabricClientCommandSource> context, WalktroughLogger logger, IBakedModule module){
		module.Summary(logger);
		logger.Info("Data used by this module:");
		for (ICacheKey key : module.GetCacheKeys())
			logger.Info(" - {}", CommandLogger.PackData(key.toString()));
		if (module.GetCacheKeys().isEmpty())
			logger.Error("[ERR] This module does not declare any data or component.");

		return 0;
	}

	static private int Walkthrough(CommandContext<FabricClientCommandSource> cmdCtx, WalktroughLogger logger, IBakedModule module, IClientEntitySelector target){
		ItemStack stack;
		String itemSource;
		Entity targetEntity = target.get();
		if (targetEntity instanceof ItemEntity groundItem){
			stack = groundItem.getItem();
			itemSource = "ground";
		}
		else if (targetEntity instanceof Player player){
			stack = player.getMainHandItem();
			itemSource = "main-hand";
		}
		else
			return Error(cmdCtx, "No elligible entity could be found.");

		logger.Info("--------");
		logger.Info("Testing {} module {} on {} item: {} ({})",
			CommandLogger.PackData(logger.moduleHook()),
			CommandLogger.PackData(logger.metamodule().id()).withStyle(ChatFormatting.UNDERLINE),
			itemSource,
			CommandLogger.ItemData(stack.getHoverName()).withStyle(ChatFormatting.UNDERLINE),
			CommandLogger.ItemData(stack.getItem())
		);
		logger.Info("----");

		if (!logger.metamodule().targets().contains(stack.getItem()))
			logger.Info(ChatFormatting.GOLD, "[WARN] This module would normally not be applied to items of type {}", stack.getItem());

		logger.Info("Data found on this item:");
		for (ICacheKey key : module.GetCacheKeys())
			logger.Info("- {}: {}", CommandLogger.PackData(key.toString()), CommandLogger.ItemData(key.Extract(stack)));
		if (module.GetCacheKeys().isEmpty())
			logger.Error("[ERR] This module does not declare any data or component.");
		logger.Info("----");

		Identifier liveModelId = module.GetModelForItem(stack);
		Identifier walkthroughModelId = module.Walkthrough(logger, stack);
		if (liveModelId != walkthroughModelId){
			logger.Error(
				"Walkthrough did not return the same model ID "
				+ "as a live run of the module. Please report this issue."
				+ "\nWalkthrough: {}"
				+ "\nLive run: {}",
				walkthroughModelId,
				liveModelId
			);
		}
		if (liveModelId != null){
			logger.Info("The module returned the model: {}", CommandLogger.PackData(liveModelId));
		}
		else
			logger.Info("The module failed to apply to the item.");

		return 0;
	}

}
