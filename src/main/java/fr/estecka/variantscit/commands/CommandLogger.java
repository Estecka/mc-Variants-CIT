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
	EModuleContext context,
	MetaModule metamodule
)
{

/******************************************************************************/
/* # Generic logging                                                          */
/******************************************************************************/

	public void Info(String format, Object... args){
		String result = format;
		for (Object o : args)
			result = result.replaceFirst("\\{\\}", o.toString());
		this.Info(result);
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

	static public MutableText VariantName(@Nullable Object variant){
		return VariantName(variant, "null");
	}

	static public MutableText VariantName(@Nullable Object variant, String fallback){
		if (variant == null)
			return Text.literal(fallback).formatted(Formatting.RED);
		else
			return Text.literal(variant.toString()).formatted(Formatting.AQUA);
	}

	static public MutableText ResourceName(Object variant){
		return Text.literal(variant.toString()).formatted(Formatting.YELLOW);
	}

	private MutableText AssetFilename(Identifier id, String assetPrefix, String suffix){
		return Text.literal("/assets/")
			.append(VariantName(id.getNamespace()))
			.append("/"+assetPrefix+metamodule.modelPrefix())
			.append(VariantName(id.getPath()))
			.append(suffix)
			.formatted(Formatting.YELLOW)
			;
	}

	public void PrintVariantIdTip(Identifier variantId){
		Info(
			Text.literal("TIP:").formatted(Formatting.GRAY)
				.append(Text.literal("The model prefix is \""))
				.append(ResourceName(metamodule.modelPrefix()))
				.append(Text.literal("\", the variant ID "))
				.append(VariantName(variantId))
				.append(" may be supported by providing one of these files:")
		);

		Text bullet = Text.literal("- ").formatted(Formatting.GRAY);

		switch (context())
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
