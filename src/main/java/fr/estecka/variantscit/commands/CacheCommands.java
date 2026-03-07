package fr.estecka.variantscit.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.modules.IBakedModule;
import fr.estecka.variantscit.modules.IModuleWrapper;
import fr.estecka.variantscit.modules.ModuleList;
import fr.estecka.variantscit.modules.cache.CacheModule;
import fr.estecka.variantscit.modules.cache.ICacheKey;
import fr.estecka.variantscit.reload.EModuleHook;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.commands.arguments.ResourceLocationArgument.id;
import static net.minecraft.commands.arguments.item.ItemArgument.item;
import static net.minecraft.commands.arguments.item.ItemArgument.getItem;
import static fr.estecka.variantscit.commands.ModuleHookArgumentType.moduleHook;
import static fr.estecka.variantscit.commands.ModuleHookArgumentType.getModuleHook;


public class CacheCommands
extends CommandUtil
{
	static public final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(VariantsCitMod.MODID, "cache");
	static public final String HOOK_ARG = "hook";
	static public final String ITEM_ARG = "item id";

	static public void	Register(){
		ClientCommandRegistrationCallback.EVENT.register(ID, CacheCommands::RegisterWith);
	}

	static public void	RegisterWith(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess){
		var cachetree = literal("cachetree")
			.then(argument(HOOK_ARG, moduleHook())
				.then(argument(ITEM_ARG, item(registryAccess))
					.suggests(CacheCommands::ItemAutofill)
					.executes(CacheCommands::PrintTree)
				)
			);

		var root = literal(VariantsCitMod.MODID)
			.then(cachetree)
			;

		dispatcher.register(root);
	}

/******************************************************************************/
/* # Autofill                                                                 */
/******************************************************************************/

	static public <T> CompletableFuture<Suggestions> ItemAutofill(CommandContext<T> context, SuggestionsBuilder builder) {
		var availableItems = VariantsCitMod.GetModules().GetAvailableItem(getModuleHook(context, HOOK_ARG));
		var ids = availableItems.stream()
			.map(item->BuiltInRegistries.ITEM.getKey(item))
			.toList()
			;

		return SharedSuggestionProvider.suggestResource(ids, builder);
	}

/******************************************************************************/
/* # Command Handlers                                                         */
/******************************************************************************/

	static private int PrintTree(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		EModuleHook hook = getModuleHook(context, HOOK_ARG);
		Item item = getItem(context, ITEM_ARG).getItem();
		IBakedModule rootModule = VariantsCitMod.GetModules().GetArchModule(hook, item);

		if (rootModule == null)
			return Info(context, "No modules exist for this item.");

		TreePrinter printer = new TreePrinter();
		printer.PrintRoot(TreeNodeOf(rootModule));
		VariantsCitMod.LOGGER.info("Cache tree for {} {}:\n{}", hook, item, printer);

		return Success(context, "Cache tree was printed into the log.");
	}

	static private TreeNode TreeNodeOf(IBakedModule module){
		TreeNode result = new TreeNode();

		result.name = ModuleName(module);

		if (module instanceof IModuleWrapper wrapper)
			for (IBakedModule child : wrapper.Unwrap())
				result.children.add(TreeNodeOf(child));
		else
			for (ICacheKey key : module.GetCacheKeys())
				result.children.add(TreeNodeOf(key));

		return result;
	}

	static private TreeNode TreeNodeOf(ICacheKey key){
		TreeNode result = new TreeNode();
		result.name = key.toString();
		return result;
	}

	static private String ModuleName(IBakedModule module){
		if (module instanceof CacheModule)
			return "<cache>";
		if (module instanceof ModuleList)
			return "<list>";

		ResourceLocation id = VariantsCitMod.GetModules().GetId(module);
		if (id != null)
			return id.toString();
		else
			return "[Unknown Module]";
	}

	static private class TreeNode {
		public String name;
		public List<TreeNode> children = new ArrayList<>();
	}

	// Pretty: │ └ ├ ┬ ─
	// Log-friendly: | \- |- T -
	static private class TreePrinter
	{
		private final StringBuilder result = new StringBuilder();
		private String indentation = "";

		public void PrintRoot(TreeNode node){
			PrintNode(node, " ");
		}

		public void PrintNode(TreeNode node, String nodeIndent){
			// result.append((node.children.size() > 0) ? '┬' : '─');
			result.append(node.name);

			String oldIndent = this.indentation;
			this.indentation += nodeIndent;
			for (int i=0; i<node.children.size(); ++i){
				boolean isLastChild = i < node.children.size()-1;
				TreeNode child = node.children.get(i);

				NewLine();
				result.append(isLastChild ? "|- " : "\\- ");
				PrintNode(child, isLastChild ? "|  " : "   ");
			}
			this.indentation = oldIndent;
		}

		private void NewLine(){
			result.append('\n');
			result.append(this.indentation);
		}

		public String toString(){ return result.toString(); }
		
	}
}
