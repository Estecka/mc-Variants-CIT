package fr.estecka.variantscit.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.modules.IBakedModule;
import fr.estecka.variantscit.reload.EModuleHook;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
// import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
// import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
// import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
// import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
// import static com.mojang.brigadier.arguments.StringArgumentType.getString;
// import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
// import static net.minecraft.commands.arguments.ResourceLocationArgument.id;
import static fr.estecka.variantscit.commands.ModuleHookArgumentType.moduleHook;
import static fr.estecka.variantscit.commands.ModuleHookArgumentType.getModuleHook;

public class ModuleTreeCommands
extends CommandUtil
{
	static public final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(VariantsCitMod.MODID, "modules");

	static public final String HOOK_ARG = "hook";

	static public void	Register(){
		ClientCommandRegistrationCallback.EVENT.register(ID, ModuleTreeCommands::RegisterWith);
	}

	static public void	RegisterWith(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess){
		var crawl = literal("crawl")       .executes(c->Crawl(c, IClientEntitySelector::GetSelf))
			.then(literal("self")          .executes(c->Crawl(c, IClientEntitySelector::GetSelf)))
			.then(literal("nearest_item")  .executes(c->Crawl(c, IClientEntitySelector::GetGroundItem)))
			.then(literal("nearest_player").executes(c->Crawl(c, IClientEntitySelector::GetPlayer)))
			;

		var hook = literal("moduletree")
			.then(argument(HOOK_ARG, moduleHook())
				.then(crawl)
			);

		var root = literal(VariantsCitMod.MODID)
			.then(hook)
			;

		dispatcher.register(root);
	}


/******************************************************************************/
/* # Command Handlers                                                         */
/******************************************************************************/

	static private int Crawl(CommandContext<FabricClientCommandSource> context, IClientEntitySelector target) throws CommandSyntaxException {
		EModuleHook hook = getModuleHook(context, HOOK_ARG);
		final CommandLogger logger = new CommandLogger(context);

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
			return Error(context, "No elligible entity could be found.");

		logger.Info("--------");
		logger.Info("Looking for the {} module that applied to {} item: {} ({})",
			hook,
			itemSource,
			CommandLogger.ItemData(stack.getHoverName()).withStyle(ChatFormatting.UNDERLINE),
			CommandLogger.ItemData(stack.getItem())
		);
		logger.Info("----");

		IBakedModule module = VariantsCitMod.GetModules().GetArchModule(hook, stack.getItem());
		if (module == null){
			logger.Error("No module exist for this item on this hook.");
			return -1;
		}
		else
		{
			IBakedModule r = module.Crawl(logger, stack);
			if (r == null){
				logger.Error("No module could apply to this item.");
				return -1;
			}

			ResourceLocation moduleId = VariantsCitMod.GetModules().GetId(module);
			if (moduleId == null){
				logger.Error("A module applied, but it could not be identified. This is a bug.");
				return -1;
			}
			else
			{
				logger.Info("The module {} applied to the item.", CommandLogger.PackData(moduleId));
				return 1;
			}
		}
	}

}
