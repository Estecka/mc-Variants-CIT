package fr.estecka.variantscit.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.assetgen.FilledTemplate;
import fr.estecka.variantscit.assetgen.GeneratedResourcePack;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.repository.PackRepository;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
// import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
// import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
// import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
// import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
// import static com.mojang.brigadier.arguments.StringArgumentType.getString;
// import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.commands.arguments.IdentifierArgument.id;

public class AssetGenCommands
extends CommandUtil
{
	static public final Identifier ID = Identifier.fromNamespaceAndPath(VariantsCitMod.MODID, "assetgen");
	static public final String BAKED_PACK_DIR = "VCIT Baked AssetGen";
	static public final String ASSET_ARG = "asset id";

	static public void	Register(){
		ClientCommandRegistrationCallback.EVENT.register(ID, AssetGenCommands::RegisterWith);
	}

	static public void	RegisterWith(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess){
		var pack = literal("createPack")
			.executes(AssetGenCommands::CreatePack)
			;

		var peek = literal("peek")
			.then(argument(ASSET_ARG, id())
				.suggests(AssetGenCommands::AssetAutofill)
				.executes(AssetGenCommands::AssetPeek)
			);

		var root = literal(VariantsCitMod.MODID)
			.then(literal("assetgen")
				.then(peek)
				.then(pack)
			);

		dispatcher.register(root);
	}


/******************************************************************************/
/* # Autofill                                                                 */
/******************************************************************************/

	static private CompletableFuture<Suggestions> AssetAutofill(final CommandContext<FabricClientCommandSource> context, final SuggestionsBuilder builder){
		SharedSuggestionProvider.suggestResource(GeneratedResourcePack.INSTANCE.GetAll().keySet(), builder);
		return builder.buildFuture();
	}


/******************************************************************************/
/* # Command Handlers                                                         */
/******************************************************************************/

	static private int AssetPeek(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		Identifier id = context.getArgument(ASSET_ARG, Identifier.class);
		FilledTemplate resource = (FilledTemplate)GeneratedResourcePack.INSTANCE.GetAll().get(id);

		if (resource == null)
			return Error(context, "No such asset: "+id.toString());

		VariantsCitMod.LOGGER.info("{}:\n{}", id, resource.getString());

		context.getSource().sendFeedback(Component.literal("Asset content was printed into the game's log."));
		return 0;
	}

	/**
	 * @implNote Overwrites existing files, keeps track of the files it writes,
	 * then delete any file it didn't intend to write.
	 */
	static private int CreatePack(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		final Set<Path> filesToKeep = new HashSet<>();
		final Path src = FabricLoader.getInstance().getModContainer(VariantsCitMod.MODID).get().findPath("baked_pack_base").get();
		final Path dst = FabricLoader.getInstance().getGameDir().resolve("resourcepacks/" + BAKED_PACK_DIR);
		boolean initError = false;
		boolean assetError = false;
		boolean cleanUpError = false;

		// Init
		dst.toFile().mkdirs();
		// Errors on individual files should not prevent other files from being
		// written. `Files.list` may throw too.
		try {
			IOException copyError = null;
			for (Path in : Files.list(src).toList())
			try {
				Path out = dst.resolve(in.getFileName().toString());
				filesToKeep.add(out);
				Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
			}
			catch (IOException e2)
			{
				copyError = e2;
				VariantsCitMod.LOGGER.error("Failed to write root file:\n{}", copyError);
			}

			if (copyError != null)
				throw copyError;
		}
		catch (IOException e){
			Error(context, "Error while initializing baked pack.");
			VariantsCitMod.LOGGER.error("Unable to initialize baked pack:\n{}", e);
			initError = true;
		}

		// Assets
		context.getSource().sendFeedback(Component.literal("Writing assets..."));
		for (var entry : GeneratedResourcePack.INSTANCE.GetAll().entrySet()){
			Identifier id = entry.getKey();
			String assetPath = "assets/"+id.getNamespace()+"/"+id.getPath();
			Path assetDst = dst.resolve(assetPath);
			filesToKeep.add(assetDst);

			assetDst.getParent().toFile().mkdirs();
			try {
				Files.write(assetDst, entry.getValue().get().readAllBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			}
			catch (IOException e){
				VariantsCitMod.LOGGER.error("Failed to write asset: {}\n{}", assetDst, e);
				assetError = true;
			}
		}

		if (assetError)
			Error(context, "Error while writing some assets. See log for details.");

		// Clean-up
		context.getSource().sendFeedback(Component.literal("Cleaning-up obsolete assets..."));
		try {
			Files.walkFileTree(dst, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					super.visitFile(file, attrs);
					if (!filesToKeep.contains(file))
						Files.deleteIfExists(file);
					return FileVisitResult.CONTINUE;
				}
			});
		}
		catch (IOException e){
			Error(context, "Pack cleanup failed.");
			VariantsCitMod.LOGGER.error("Pack cleanup failed:\n{}", e);
			cleanUpError = true;
		}

		// Reload
		if (initError || assetError)
			return Error(context, "Reload skipped because of previous errors.");
		else if (cleanUpError)
			Error(context, "Reloading, but cleanup failed so results may be incorrect.");
		else
			context.getSource().sendFeedback(Component.literal("Done !"));

		PackRepository packManager = Minecraft.getInstance().getResourcePackRepository();
		String packId = "file/"+BAKED_PACK_DIR;
		List<String> enabled = new ArrayList<>(packManager.getSelectedIds());
		if (!packManager.getSelectedIds().contains(packId)){
			enabled.addFirst(packId);
			packManager.reload(); // Scan for available packs. NOT a resource reload.
			packManager.setSelected(enabled);
		}

		Minecraft.getInstance().reloadResourcePacks(); // THIS is a resource reload.
		return 1;
	}

}

