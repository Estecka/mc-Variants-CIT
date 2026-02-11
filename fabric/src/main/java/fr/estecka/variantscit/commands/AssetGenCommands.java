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
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.io.FileUtils;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.command.argument.IdentifierArgumentType.identifier;

public class AssetGenCommands
{
	static public final Identifier ID = Identifier.of(VariantsCitMod.MODID, "assetgen");
	static public final String BAKED_PACK_DIR = "VCIT Baked AssetGen";
	static public final String ASSET_ARG = "asset id";

	static public void	Register(){
		ClientCommandRegistrationCallback.EVENT.register(ID, AssetGenCommands::RegisterWith);
	}

	static public void	RegisterWith(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess){
		var pack = literal("createPack")
			.executes(AssetGenCommands::CreatePack)
			;

		var peek = literal("peek")
			.then(argument(ASSET_ARG, identifier())
				.suggests(AssetGenCommands::AssetAutofill)
				.executes(AssetGenCommands::AssetDump)
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
		CommandSource.suggestIdentifiers(GeneratedResourcePack.INSTANCE.GetAll().keySet(), builder);
		return builder.buildFuture();
	}


/******************************************************************************/
/* # Command Handlers                                                         */
/******************************************************************************/

	static private int Error(CommandContext<FabricClientCommandSource> context, String message){
		context.getSource().sendError(Text.literal(message));
		return -1;
	}

	static private int AssetDump(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		Identifier id = context.getArgument(ASSET_ARG, Identifier.class);
		FilledTemplate resource = (FilledTemplate)GeneratedResourcePack.INSTANCE.GetAll().get(id);

		if (resource == null)
			return Error(context, "No such asset: "+id.toString());

		VariantsCitMod.LOGGER.info("{}:\n{}", id, resource.getString());

		context.getSource().sendFeedback(Text.literal("Asset content was printed into the game's log."));
		return 0;
	}

	static private int CreatePack(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		File base = FabricLoader.getInstance().getModContainer(VariantsCitMod.MODID).get().findPath("baked_pack_base").get().toFile();
		File pack = FabricLoader.getInstance().getGameDir().resolve("resourcepacks/" + BAKED_PACK_DIR).toFile();

		if (!base.exists() || !base.isDirectory())
			return Error(context, "Missing base for baked assetgen pack.");

		// Delete
		if (pack.exists()){
			if (!pack.isDirectory())
				return Error(context, "File \""+BAKED_PACK_DIR+"\" already exists but is not a directory.");
			else {
				context.getSource().sendFeedback(Text.literal("Deleting existing pack..."));
				try {
					FileUtils.deleteDirectory(pack);
				}
				catch (IOException e){
					VariantsCitMod.LOGGER.error("Unable to delete existing baked assetgen pack:\n{}", e);
					return Error(context, "Unable to delete existing baked assetgen pack.");
				}
			}
		}

		// Init
		try {
			FileUtils.copyDirectory(base, pack);
		}
		catch (IOException e){
			VariantsCitMod.LOGGER.error("Unable to initialize baked pack:\n{}", e);
			return Error(context, "Unable to initialize baked pack.");
		}

		// Assets
		context.getSource().sendFeedback(Text.literal("Writing assets..."));
		boolean error = false;
		for (var entry : GeneratedResourcePack.INSTANCE.GetAll().entrySet()){
			Identifier id = entry.getKey();
			String assetPath = "assets/"+id.getNamespace()+"/"+id.getPath();
			Path assetDst = pack.toPath().resolve(assetPath);

			assetDst.getParent().toFile().mkdirs();

			try {
				Files.write(assetDst, entry.getValue().get().readAllBytes());
			}
			catch (IOException e){
				VariantsCitMod.LOGGER.error("Error writing file: {}\n{}", assetDst, e);
			}
		}

		if (error)
			return Error(context, "Error while writing some assets. See log for details.");

		context.getSource().sendFeedback(Text.literal("Done !"));

		// Reload
		ResourcePackManager packManager = MinecraftClient.getInstance().getResourcePackManager();
		String packId = "file/"+BAKED_PACK_DIR;
		List<String> enabled = new ArrayList<>(packManager.getEnabledIds());
		if (!packManager.getEnabledIds().contains(packId)){
			enabled.addFirst(packId);
			packManager.scanPacks();
			packManager.setEnabledProfiles(enabled);
		}

		MinecraftClient.getInstance().reloadResources();
		return 1;
	}

}

