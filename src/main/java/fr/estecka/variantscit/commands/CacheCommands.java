package fr.estecka.variantscit.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.NotImplementedException;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.modules.IBakedModule;
import fr.estecka.variantscit.modules.IModuleWrapper;
import fr.estecka.variantscit.modules.cache.ICacheKey;
import fr.estecka.variantscit.reload.EModuleContext;
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
import static fr.estecka.variantscit.commands.ModuleContextArgumentType.moduleContext;
import static fr.estecka.variantscit.commands.ModuleContextArgumentType.getModuleContext;


public class CacheCommands
extends CommandUtil
{
	static public final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(VariantsCitMod.MODID, "cache");
	static public final String CONTEXT_ARG = "context";
	static public final String ITEM_ARG    = "item id";

	static public void	Register(){
		ClientCommandRegistrationCallback.EVENT.register(ID, CacheCommands::RegisterWith);
	}

	static public void	RegisterWith(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess){
		var cachetree = literal("cachetree")
			.then(argument(CONTEXT_ARG, moduleContext())
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
		var availableItems = VariantsCitMod.GetItems(getModuleContext(context, CONTEXT_ARG));
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
		EModuleContext modCtx = getModuleContext(context, CONTEXT_ARG);
		Item item = getItem(context, ITEM_ARG).getItem();
		IBakedModule rootModule = switch (modCtx) {
			case ITEM_MODEL -> VariantsCitMod.GetItemModule(item);
			case EQUIPPABLE -> VariantsCitMod.GetEquipmentModule(item);
			default -> throw new NotImplementedException();
		};

		if (rootModule == null)
			return Info(context, "No modules exist for this item.");

		TreePrinter printer = new TreePrinter();
		printer.PrintRoot(TreeNodeOf(rootModule));
		VariantsCitMod.LOGGER.info("Cache tree for {} {}:\n{}", modCtx, item, printer);

		return Success(context, "Cache tree was printed into the log.");
	}

	static private TreeNode TreeNodeOf(IBakedModule module){
		TreeNode result = new TreeNode();

		result.name = module.getClass().getSimpleName();

		if (module instanceof IModuleWrapper wrapper)
			for (IBakedModule child : wrapper.Unwrap())
				result.children.add(TreeNodeOf(child));
		else
			for (ICacheKey key : module.GetCacheKeys())
				result.children.add(TreeNodeOf(key));

		return result;
	}

	static private TreeNode TreeNodeOf(ICacheKey module){
		TreeNode result = new TreeNode();
		result.name = module.getClass().getSimpleName();
		return result;
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
				PrintNode(child, isLastChild ? "│  " : "   ");
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
