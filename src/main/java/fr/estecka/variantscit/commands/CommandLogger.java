package fr.estecka.variantscit.commands;

import org.jetbrains.annotations.Nullable;
import com.mojang.brigadier.context.CommandContext;
import fr.estecka.variantscit.reload.EModuleContext;
import fr.estecka.variantscit.reload.ModuleLoader.MetaModule;
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

	public void InfoFormat(String format, Object... args){
		String result = format;
		for (Object o : args)
			result = result.replaceFirst("\\{\\}", o.toString());
		this.Info(result);
	}

	public void Info(Object... texts){
		this.Info(Formatting.RESET, (Object[])texts);
	}

	public void Info(Formatting formatting, Object... args){
		MutableText message = Text.empty().formatted(formatting);

		for (Object obj : args)
			message.append(TextOf(obj));

		this.Info(message);
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

	static public MutableText TextOf(Object obj){
		if (obj instanceof Text t)
			return t.copy();
		else
			return Text.literal(String.valueOf(obj));
	}

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

	private MutableText AssetFilename(Identifier id, String assetPrefix, String suffix){
		return Text.literal("/assets/")
			.append(ItemData(id.getNamespace()))
			.append("/"+assetPrefix+metamodule.modelPrefix())
			.append(ItemData(id.getPath()))
			.append(suffix)
			.formatted(Formatting.YELLOW)
			;
	}

	public void PrintVariantIdTip(Identifier variantId){
		Info(
			Text.empty().formatted(Formatting.GRAY)
				.append("[TIP] ")
				.append("The model prefix is \"")
				.append(PackData(metamodule.modelPrefix()))
				.append("\", the variant ID ")
				.append(ItemData(variantId))
				.append(" may be supported by providing one of these files:")
		);

		Text bullet = Text.literal("- ").formatted(Formatting.GRAY);

		switch (moduleContext())
		{
			default:
				Error("Error: unknown context");
				break;
			case EQUIPPABLE:
				Info(bullet.copy().append(AssetFilename(variantId, "items/", ".json")));
				break;
			case ITEM_MODEL:
				Info(bullet.copy().append(AssetFilename(variantId, "items/", ".json")));
				Info(bullet.copy().append(AssetFilename(variantId, "models/item/", ".json")));
				Info(bullet.copy().append(AssetFilename(variantId, "textures/item/", ".png")));
				break;
		}
	}
}
