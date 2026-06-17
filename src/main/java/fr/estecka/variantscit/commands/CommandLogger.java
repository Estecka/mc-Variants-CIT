package fr.estecka.variantscit.commands;

import org.jetbrains.annotations.Nullable;
import com.mojang.brigadier.context.CommandContext;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;
import fr.estecka.variantscit.reload.EAssetType;
import fr.estecka.variantscit.reload.EModuleHook;
import fr.estecka.variantscit.reload.LibraryDefinition;
import fr.estecka.variantscit.reload.MetaModule;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public record CommandLogger(
	CommandContext<FabricClientCommandSource> commandContext,
	EModuleHook moduleHook,
	MetaModule metamodule,
	String subPrefix
)
{

	public CommandLogger WithSubPrefix(String subPrefix){
		return new CommandLogger(
			commandContext,
			moduleHook,
			metamodule,
			this.subPrefix + subPrefix
		);
	}


/******************************************************************************/
/* # Generic logging                                                          */
/******************************************************************************/

	static public MutableComponent TextOf(Object obj){
		if (obj instanceof Component text)
			return text.copy();
		else if (obj instanceof IDataContainer data)
			return data.toText();
		else
			return Component.literal(String.valueOf(obj));
	}

	static public MutableComponent TextFormat(ChatFormatting style, String format, Object... args){
		MutableComponent result = Component.empty().withStyle(style);

		String remainder = format;
		int i = 0;
		int argPos;
		while (i < args.length && 0 <= (argPos=remainder.indexOf("{}"))){
			result.append(remainder.substring(0, argPos));
			result.append(TextOf(args[i]));

			++i;
			remainder = remainder.substring(argPos + 2);
		}

		if (!remainder.isEmpty())
			result.append(remainder);

		return result;
	}

	public MutableComponent TextFormat(String format, Object... args){
		return TextFormat(ChatFormatting.RESET, format, args);
	}


	public void Info(ChatFormatting formatting, String format, Object... args){
		this.Info(TextFormat(formatting, format, args));
	}

	public void Info(String format, Object... args){
		this.Info(TextFormat(ChatFormatting.RESET, format, args));
	}

	public void Info(String message){
		this.Info(Component.literal(message));
	}

	public void Info(Component message){
		commandContext.getSource().sendFeedback(message);
	}


	public void Error(ChatFormatting formatting, String format, Object... args){
		this.Error(TextFormat(formatting, format, args));
	}

	public void Error(String format, Object... args){
		this.Error(TextFormat(ChatFormatting.RESET, format, args));
	}

	public void Error(String message){
		this.Error(Component.literal(message));
	}

	public void Error(Component message){
		commandContext.getSource().sendError(message);
	}


/******************************************************************************/
/* # Preformatted                                                             */
/******************************************************************************/

	static public MutableComponent ItemData(@Nullable Object variant){
		return ItemData(variant, "null");
	}

	static public MutableComponent ItemData(@Nullable Object variant, String fallback){
		if (variant == null)
			return Component.literal(fallback).withStyle(ChatFormatting.RED);
		else
			return TextOf(variant).withStyle(ChatFormatting.AQUA);
	}

	static public MutableComponent PackData(Object variant){
		return TextOf(variant).withStyle(ChatFormatting.YELLOW);
	}

	static private MutableComponent AssetFilename(String modelPrefix, ResourceLocation variantId, EAssetType assetType){
		return TextFormat(ChatFormatting.YELLOW, "/assets/{}/{}/{}{}{}",
			ItemData(variantId.getNamespace()),
			assetType.packDirectory,
			PackData(modelPrefix),
			ItemData(variantId.getPath()),
			assetType.suffix
		);
	}

	static private MutableComponent LayeredAssetFilename(String modelPrefix, ResourceLocation variantId, EAssetType assetType){
		return TextFormat(ChatFormatting.YELLOW, "/assets/{}/{}/{}/{}{}{}",
			ItemData(variantId.getNamespace()),
			assetType.packDirectory,
			ItemData("<layer name>"),
			PackData(modelPrefix),
			ItemData(variantId.getPath()),
			assetType.suffix
		);
	}

	public void PrintVariantIdTip(ResourceLocation variantId){
		final LibraryDefinition libDef = metamodule.libraryDefinition();
		variantId = variantId.withPrefix(subPrefix);

		ResourceLocation modelId = libDef.GetModelId(variantId);
		if (modelId == null)
		{
			Info(ChatFormatting.GOLD, 
				"[WARN] The variant ID {} is not supported by this module. "
				+ "It is not listed in the hardcoded `modelList`, "
				+ "and an automatic binding could not be found, "
				+ "either because the module has no `modelPrefix`, "
				+ "or because the variant ID was filtered out by the options `modelNamespace` and `modelPathes`.",
				ItemData(variantId)
			);
		}
		else if (libDef.hardcodedList().containsKey(variantId) || !libDef.modelPrefix().isPresent())
		{
			Info(ChatFormatting.GRAY,
				"[TIP] The variant ID {} is hardwired to the model ID {}, "
				+ "it may be supported by providing one of these files:",
				ItemData(variantId),
				ItemData(modelId)
			);
	
			PrintFileNamesTip("", modelId);
		}
		else {
			Info(ChatFormatting.GRAY,
				"[TIP] The model prefix is \"{}\", "
				+ "the variant ID {} may be supported by providing one of these files:",
				PackData(libDef.modelPrefix().get()),
				ItemData(variantId)
			);
	
			PrintFileNamesTip(libDef.modelPrefix().get(), variantId);
		}

	}

	public void PrintFileNamesTip(String modelPrefix, ResourceLocation variantId){
		Component bullet = Component.literal("-").withStyle(ChatFormatting.GRAY);
		switch (this.moduleHook)
		{
			default:
				Error("Error: unknown hook");
				break;
			case TRIM_PATTERN:
				Info(bullet.copy().append(AssetFilename(modelPrefix, variantId, EAssetType.TRIM_MODEL)));
				Info(bullet.copy().append(LayeredAssetFilename(modelPrefix, variantId, EAssetType.TRIM_TEXTURE)));
				break;
			case EQUIPPABLE:
				Info(bullet.copy().append(AssetFilename(modelPrefix, variantId, EAssetType.EQUIPMENT)));
				Info(bullet.copy().append(LayeredAssetFilename(modelPrefix, variantId, EAssetType.EQUIP_TEXTURE)));
				break;
			case ITEM_MODEL:
				Info(bullet.copy().append(AssetFilename(modelPrefix, variantId, EAssetType.ITEM_STATE)));
				Info(bullet.copy().append(AssetFilename(modelPrefix, variantId, EAssetType.BAKED_MODEL)));
				Info(bullet.copy().append(AssetFilename(modelPrefix, variantId, EAssetType.ITEM_TEXTURE)));
				break;
		}
	}
}
