package fr.estecka.variantscit.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.DataResult;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.modules.IBakedModule;
import fr.estecka.variantscit.modules.cache.ICacheKey;
import fr.estecka.variantscit.modules.libraries.VariantLibrary;
import fr.estecka.variantscit.reload.EModuleHook;
import fr.estecka.variantscit.reload.LibraryDefinition;
import fr.estecka.variantscit.reload.MetaModule;
import fr.estecka.variantscit.util.VariantUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
// import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
// import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
// import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
// import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
// import static com.mojang.brigadier.arguments.StringArgumentType.getString;
// import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.commands.arguments.IdentifierArgument.id;
import static fr.estecka.variantscit.commands.ModuleHookArgumentType.moduleHook;
import static fr.estecka.variantscit.commands.ModuleHookArgumentType.getModuleHook;

// TODO: Break down into separate clases
public class ModuleCommands
extends CommandUtil
{
	static public final Identifier ID = Identifier.fromNamespaceAndPath(VariantsCitMod.MODID, "modules");

	static public final String HOOK_ARG       = "hook";
	static public final String MODULE_ARG     = "module id";
	static public final String VARIANT_ID_ARG = "variant id";
	static public final String MODEL_ID_ARG   = "model id";

	static public void	Register(){
		ClientCommandRegistrationCallback.EVENT.register(ID, ModuleCommands::RegisterWith);
	}

	static public void	RegisterWith(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess){
		var walkthrough = literal("walkthrough").executes(Walkthrough(IClientEntitySelector::GetSelf))
			.then(literal("self")          .executes(Walkthrough(IClientEntitySelector::GetSelf)))
			.then(literal("nearest_item")  .executes(Walkthrough(IClientEntitySelector::GetGroundItem)))
			.then(literal("nearest_player").executes(Walkthrough(IClientEntitySelector::GetPlayer)))
			;

		var variantId = literal("variant-id")
			.then(argument(VARIANT_ID_ARG, id())
				.suggests(ModuleCommands::VariantIdAutofill)
				.executes((IModuleCommand)ModuleCommands::VariantId)
			);

		var modelId = literal("model-id")
			.then(argument(MODEL_ID_ARG, id())
				.suggests(ModuleCommands::ModelIdAutofill)
				.executes((IModuleCommand)ModuleCommands::ModelId)
			);

		var module = argument(MODULE_ARG, id())
			.suggests(ModuleCommands::ModuleAutofill)
			.then(literal("dump"   ).executes((IModuleCommand)ModuleCommands::Dump))
			.then(literal("summary").executes((IModuleCommand)ModuleCommands::Summary))
			.then(variantId)
			.then(modelId)
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
/* # Util                                                                     */
/******************************************************************************/

	static private DataResult<MetaModule> GetMeta(CommandContext<FabricClientCommandSource> context){
		EModuleHook hook = getModuleHook(context, HOOK_ARG);
		Identifier moduleId = context.getArgument(MODULE_ARG, Identifier.class);
		DataResult<MetaModule> optMeta = VariantsCitMod.GetModules().GetMeta(moduleId);
		if (optMeta.isSuccess() && optMeta.getOrThrow().bakedModules().get(hook) == null)
			return DataResult.error(() -> "No hook "+hook+" for module "+moduleId);
		else
			return optMeta;
	}

	static private DataResult<VariantLibrary> GetLibrary(CommandContext<FabricClientCommandSource> context){
		EModuleHook hook = getModuleHook(context, HOOK_ARG);
		return GetMeta(context).map(meta -> meta.collectedModels().get(hook));
	}

	static private DataResult<IBakedModule> GetBaked(CommandContext<FabricClientCommandSource> context){
		EModuleHook hook = getModuleHook(context, HOOK_ARG);
		return GetMeta(context).map(meta -> meta.bakedModules().get(hook));
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

	static private CompletableFuture<Suggestions> VariantIdAutofill(final CommandContext<FabricClientCommandSource> context, final SuggestionsBuilder builder){
		GetLibrary(context).ifSuccess( (library) -> {
			SharedSuggestionProvider.suggestResource(
				library.variantModels().keySet(),
				builder
			);
		});
		GetMeta(context).ifSuccess( (meta) -> {
			SharedSuggestionProvider.suggestResource(
				meta.libraryDefinition().hardcodedList().keySet(),
				builder
			);
			SharedSuggestionProvider.suggestResource(
				meta.parameters().GetIntrinsicModels().stream().map(VariantUtil::IntrinsicVariantId),
				builder
			);
		});
		return builder.buildFuture();
	}

	static private CompletableFuture<Suggestions> ModelIdAutofill(final CommandContext<FabricClientCommandSource> context, final SuggestionsBuilder builder){
		GetLibrary(context).ifSuccess( (library) -> {
			SharedSuggestionProvider.suggestResource(
				library.variantModels().values(),
				builder
			);
		});
		GetMeta(context).ifSuccess( (meta) -> {
			SharedSuggestionProvider.suggestResource(
				meta.libraryDefinition().hardcodedList().values(),
				builder
			);
			SharedSuggestionProvider.suggestResource(
				meta.parameters().GetIntrinsicModels(),
				builder
			);
		});
		return builder.buildFuture();
	}

/******************************************************************************/
/* # Command Handlers                                                         */
/******************************************************************************/

	@FunctionalInterface
	static private interface IModuleCommand
	extends Command<FabricClientCommandSource>
	{
		int run(CommandContext<FabricClientCommandSource> context, WalktroughLogger logger, MetaModule meta) throws CommandSyntaxException;

		default int run(CommandContext<FabricClientCommandSource> context)
		throws CommandSyntaxException
		{
			EModuleHook hook = getModuleHook(context, HOOK_ARG);
			MetaModule meta;
			DataResult<MetaModule> optMeta = GetMeta(context);
			if (optMeta.isError())
				return Error(context, optMeta.error().get().message());
			else
				meta = optMeta.getOrThrow();
	
			IBakedModule module = meta.bakedModules().get(hook);
			Objects.requireNonNull(module);
	
			WalktroughLogger logger = new WalktroughLogger(context, hook, meta, "");
			return this.run(context, logger, meta);
		}
	}

	static private IModuleCommand Walkthrough(IClientEntitySelector target){
		return (c,l,m)->Walkthrough(c,l,m, target);
	}

	static private int Dump(CommandContext<FabricClientCommandSource> context, WalktroughLogger logger, MetaModule meta){
		final VariantLibrary library = GetLibrary(context).getOrThrow();
		library.Dump(logger);
		return 0;
	}

	static private int Summary(CommandContext<FabricClientCommandSource> context, WalktroughLogger logger, MetaModule meta){
		final IBakedModule module = GetBaked(context).getOrThrow();
		module.Summary(logger);
		logger.Info("Data used by this module:");
		for (ICacheKey key : module.GetCacheKeys())
			logger.Info(" • {}", CommandLogger.PackData(key.toString()));
		if (module.GetCacheKeys().isEmpty())
			logger.Error("[ERR] This module does not declare any data or component.");

		return 0;
	}

	static private int VariantId(CommandContext<FabricClientCommandSource> context, WalktroughLogger logger, MetaModule meta){
		final VariantLibrary library = GetLibrary(context).getOrThrow();
		final IBakedModule bakedModule = GetBaked(context).getOrThrow();
		final LibraryDefinition libDefinition = meta.libraryDefinition();
		final Identifier variantId = context.getArgument(VARIANT_ID_ARG, Identifier.class);

		Optional<Identifier> intrinsicModelId = VariantUtil.GetIntrinsicModelId(variantId);
		Identifier modelIdCandidate = libDefinition.GetModelId(variantId);
		Identifier modelIdFound = library.GetVariantModelStrict(variantId);

		boolean isHardCoded = libDefinition.hardcodedList().containsKey(variantId);
		boolean isCompatiblePrefix = libDefinition.AcceptsVariant(variantId);
		boolean isCompatibleModule = meta.parameters().AcceptsVariant(variantId) || variantId.getNamespace().equals(VariantsCitMod.MODID);
		boolean isIntrinsic = intrinsicModelId.map(meta.parameters()::AcceptsIntrinsic).orElse(false);
		if (isIntrinsic)
			modelIdCandidate = intrinsicModelId.get();

		boolean isMissing = modelIdFound==null && modelIdCandidate!=null && isCompatibleModule;

		logger.Info("--------");
		logger.Info("Looking for variant ID {} in the {} module {}",
			CommandLogger.ItemData(variantId),
			CommandLogger.PackData(logger.moduleHook),
			CommandLogger.PackData(meta.id())
		);
		logger.Info("----");

		if (isIntrinsic)
			logger.Info("This variant is intrinsic to this module's type or parameters.");
		else if (isHardCoded)
			logger.Info("This variant was hardwired to the model ID {}", CommandLogger.PackData(modelIdCandidate));
		else if (isCompatiblePrefix && libDefinition.modelPrefix().isPresent()){
			logger.Info(
				"This variant was combined with the modelPrefix, and leads to the model ID: {}",
				CommandLogger.PackData(variantId.withPrefix(libDefinition.modelPrefix().get()))
			);
		}
		else
			logger.Info("This variant was not found in this module's definition.");

		logger.Info("--");

		if (bakedModule.VariantIdInfo(logger, variantId))
			logger.Info("--");

		if (modelIdFound != null){
			logger.Info(ChatFormatting.GREEN, "The variant was found, and is bound to the model ID: {}", CommandLogger.PackData(modelIdFound));
			if (!modelIdFound.equals(modelIdCandidate)){
				logger.Error(
					"The effective model ID is different from the "
					+ "requested one. Please report this issue."
					+ "\nRequested model ID: {}",
					CommandLogger.PackData(modelIdCandidate)
				);
			}
		}
		else if (isMissing){
			logger.Info(ChatFormatting.GOLD,
				"The model ID {} appears ot be missing, or the module does not "
				+ "have the necessary assetGen or modelParent options.",
				CommandLogger.PackData(modelIdCandidate)
			);
		}
		else if (!isCompatibleModule){
			logger.Info(ChatFormatting.GOLD,
				"The variant ID {} was rejected because it is incompatible with "
				+ "this module's type or parameters.",
				CommandLogger.ItemData(variantId)
			);
		}
		else if (!isHardCoded && !isCompatiblePrefix && libDefinition.modelPrefix().isPresent()){
			logger.Info(ChatFormatting.GOLD,
				"The variant ID {} was rejected because it does no match this "
				+ " module's modelNamespace or modelPathes options.",
				CommandLogger.ItemData(variantId)
			);
		}
		else if (modelIdCandidate == null){
			logger.Error("No model-Id was found for this variant, but I'm unsure as to why. Please report this issue.");
		}
		else {
			logger.Error("This variant is not present in this module, but I'm unsure as to why. Please report this issue.");
		}

		
		if (modelIdCandidate != null) {
			logger.Info("--");
			logger.Info(ChatFormatting.GRAY,
				"The model ID {} may correspond to any of these files:",
				CommandLogger.ItemData(modelIdCandidate)
			);
			logger.PrintFileNamesTip("", modelIdCandidate);
		}

		return 0;
	}

	static private int ModelId(CommandContext<FabricClientCommandSource> context, WalktroughLogger logger, MetaModule meta){
		final VariantLibrary library = GetLibrary(context).getOrThrow();
		final LibraryDefinition libDefinition = meta.libraryDefinition();
		final Identifier modelId = context.getArgument(MODEL_ID_ARG, Identifier.class);
		Optional<Identifier> unprefixedVariant = libDefinition.modelPrefix().flatMap(prefix -> VariantUtil.RemovePrefix(modelId, prefix));
		Set<Identifier> candidateVariantIds = meta.libraryDefinition().GetVariantIds(modelId);
		Set<Identifier> foundvariants = new HashSet<>();
		Set<Identifier> rejectedVariants = new HashSet<>();
		Set<Identifier> missingVariants = new HashSet<>();

		boolean isIntrinsic = meta.parameters().AcceptsIntrinsic(modelId);
		boolean isHardCoded = meta.libraryDefinition().hardcodedList().containsValue(modelId);
		boolean matchesPrefix = unprefixedVariant.isPresent();
		boolean isPrefixOverriden = unprefixedVariant.map(variantId -> libDefinition.hardcodedList().containsKey(variantId)).orElse(false);
		boolean isPrefixCompatible = unprefixedVariant.map(variantId -> libDefinition.AcceptsVariant(variantId)).orElse(false);

		if (unprefixedVariant.isPresent() && !isPrefixOverriden)
			candidateVariantIds.add(unprefixedVariant.get());
		if (isIntrinsic)
			candidateVariantIds.add(VariantUtil.IntrinsicVariantId(modelId));

		for (Identifier variantId : candidateVariantIds){
			if (library.HasVariantModel(variantId))
				foundvariants.add(variantId);
			else if (!meta.parameters().AcceptsVariant(variantId))
				rejectedVariants.add(variantId);
			else
				missingVariants.add(variantId);
		}

		logger.Info("--------");
		logger.Info("Looking for Model ID {} in the {} module {}",
			CommandLogger.PackData(modelId),
			CommandLogger.PackData(logger.moduleHook),
			CommandLogger.PackData(meta.id())
		);
		logger.Info("----");
		{
			Component yesText = Component.literal("yes").withStyle(ChatFormatting.GREEN);
			Component noText  = Component.literal("no").withStyle(ChatFormatting.RED);
			
			Component intrinsicText = (isIntrinsic) ? yesText : noText;
			Component hardcodedText = Component
				.literal(String.valueOf(
					libDefinition.hardcodedList().values().stream().filter(id->id.equals(modelId)).count()
				)+" ocurrences")
				.withStyle(isHardCoded ? ChatFormatting.GREEN : ChatFormatting.RED)
				;
			Component prefixText =
				(!libDefinition.modelPrefix().isPresent()) ? Component.literal("no prefix").withStyle(ChatFormatting.RED) :
				(!matchesPrefix) ? Component.literal("doesn't match").withStyle(ChatFormatting.RED) :
				(isPrefixOverriden) ? Component.literal("overriden by modelList").withStyle(ChatFormatting.GOLD) :
				(!isPrefixCompatible) ? Component.literal("rejected by model filters").withStyle(ChatFormatting.GOLD) :
				Component.literal("matches").withStyle(ChatFormatting.GREEN)
				;

			logger.Info("Intrinsic: {}", intrinsicText);
			logger.Info("Model list: {}", hardcodedText);
			logger.Info("Model prefix: {}", prefixText);
			logger.Info("This model was bound to {} variants out of {} candidates.", foundvariants.size(), candidateVariantIds.size());
		}
		logger.Info("--");

		if (foundvariants.size() > 0){
			logger.Info(ChatFormatting.GREEN, 
				"This model was collected by this module, and bound to the "
				+ "following variant IDs:"
			);
			for (Identifier id : foundvariants)
				logger.Info(" • {}", CommandLogger.ItemData(id));
		}
		else if (missingVariants.size() <= 0){
			logger.Info(ChatFormatting.GOLD, 
				"This model was not collected by this module, "
				+ "because it wasn't bound to any elligible variant ID."
			);
		}

		if (rejectedVariants.size() > 0){
			logger.Info(
				"The following variant IDs were rejected, because they are "
				+ "incompatible with this module's type or parameters:"
			);
			for (Identifier id : rejectedVariants)
				logger.Info(" • {}", CommandLogger.ItemData(id));
		}

		if (missingVariants.size() > 0 ){
			if (foundvariants.size() <= 0)
				logger.Info(ChatFormatting.GOLD,
					"The model was not bound to any of the following variant IDs. "
					+ "The model may be missing, or the module does not have "
					+ "the necessary assetGen or modelParent options."
				);
			else
				logger.Error(
					"The model was not bound to the following variant IDs, "
					+ "but I'm unsure as to why. Please report this issue."
				);

			for (Identifier id : missingVariants) {
				logger.Info(" • {}", CommandLogger.ItemData(id));
			}
		}

		logger.Info("--");
		logger.Info(ChatFormatting.GRAY,
			"The model ID {} may correspond to any of these files:",
			CommandLogger.ItemData(modelId)
		);
		logger.PrintFileNamesTip("", modelId);
		return 0;
	}

	static private int Walkthrough(CommandContext<FabricClientCommandSource> context, WalktroughLogger logger, MetaModule meta, IClientEntitySelector target){
		final IBakedModule module = GetBaked(context).getOrThrow();
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
				"Walkthrough did not return the same model ID as a live run of "
				+ "the module. Please report this issue."
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
