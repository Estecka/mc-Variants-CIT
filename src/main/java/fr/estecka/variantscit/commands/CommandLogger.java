package fr.estecka.variantscit.commands;

import org.jetbrains.annotations.Nullable;
import com.mojang.brigadier.context.CommandContext;
import fr.estecka.variantscit.reload.EAssetType;
import fr.estecka.variantscit.reload.EModuleContext;
import fr.estecka.variantscit.reload.MetaModule;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public record CommandLogger(
	CommandContext<FabricClientCommandSource> commandContext,
	EModuleContext moduleContext,
	MetaModule metamodule
)
{

/******************************************************************************/
/* # Generic logging                                                          */
/******************************************************************************/

	static public MutableText TextOf(Object obj){
		if (obj instanceof Text text)
			return text.copy();
		else
			return Text.literal(String.valueOf(obj));
	}

	static public MutableText TextFormat(Formatting style, String format, Object... args){
		MutableText result = Text.empty().formatted(style);

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

	public MutableText TextFormat(String format, Object... args){
		return TextFormat(Formatting.RESET, format, args);
	}

	public void Info(Formatting formatting, String format, Object... args){
		this.Info(TextFormat(formatting, format, args));
	}

	public void Info(String format, Object... args){
		this.Info(TextFormat(Formatting.RESET, format, args));
	}

	public void Info(String message){
		this.Info(Text.literal(message));
	}

	public void Info(Text message){
		commandContext.getSource().sendFeedback(message);
	}

	public void Error(String message){
		this.Error(Text.literal(message));
	}

	public void Error(Text message){
		commandContext.getSource().sendError(message);
	}

/******************************************************************************/
/* # Preformatted                                                             */
/******************************************************************************/

	static public MutableText ItemData(@Nullable Object variant){
		return ItemData(variant, "null");
	}

	static public MutableText ItemData(@Nullable Object variant, String fallback){
		if (variant == null)
			return Text.literal(fallback).formatted(Formatting.RED);
		else
			return TextOf(variant).formatted(Formatting.AQUA);
	}

	static public MutableText PackData(Object variant){
		return TextOf(variant).formatted(Formatting.YELLOW);
	}

	private MutableText AssetFilename(Identifier variantId, EAssetType assetType){
		return TextFormat(Formatting.YELLOW, "/assets/{}/{}/{}{}{}",
			ItemData(variantId.getNamespace()),
			assetType.directory,
			metamodule.modelPrefix(),
			ItemData(variantId.getPath()),
			assetType.suffix
		);
	}

	public void PrintVariantIdTip(Identifier variantId){
		Info(Formatting.GRAY, "[TIP] The model prefix is \"{}\", the variant ID {} may be supported by providing one of these files:",
			PackData(metamodule.modelPrefix()),
			ItemData(variantId)
		);

		Text bullet = Text.literal("-").formatted(Formatting.GRAY);
		switch (moduleContext())
		{
			default:
				Error("Error: unknown context");
				break;
			case EQUIPPABLE:
				Info(bullet.copy().append(AssetFilename(variantId, EAssetType.EQUIPMENT)));
				break;
			case ITEM_MODEL:
				Info(bullet.copy().append(AssetFilename(variantId, EAssetType.ITEM_STATE)));
				Info(bullet.copy().append(AssetFilename(variantId, EAssetType.BAKED_MODEL)));
				Info(bullet.copy().append(AssetFilename(variantId, EAssetType.ITEM_TEXTURE)));
				break;
		}
	}
}
