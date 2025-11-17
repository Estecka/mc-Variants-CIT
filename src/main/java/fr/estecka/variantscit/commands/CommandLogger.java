package fr.estecka.variantscit.commands;

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

	static public MutableText VariantName(Object variant){
		return Text.literal(variant.toString()).formatted(Formatting.AQUA);
	}

	static public MutableText ResourceName(Object variant){
		return Text.literal(variant.toString()).formatted(Formatting.YELLOW);
	}

	static private MutableText AssetFilename(Identifier id, String prefix, String suffix){
		return ResourceName("/assets/"+id.getNamespace()+"/"+prefix+id.getPath()+suffix);
	}

	public void PrintVariantIdTip(Identifier variantId){
		Info(
			Text.literal("TIP:").formatted(Formatting.ITALIC)
				.append(Text.literal("The model prefix is "))
				.append(ResourceName(metamodule.modelPrefix()))
				.append(Text.literal("The variant ID "))
				.append(VariantName(variantId))
				.append(" may be supported by providing one of these files:")
		);

		switch (context()) {
			default: Error("Error: unknown context");
			case EQUIPPABLE:
				Info(Text.literal("- ").append(AssetFilename(variantId, "items/", ".json")));
			break;
			case ITEM_MODEL:
				Info(Text.literal("- ").append(AssetFilename(variantId, "items/", ".png")));
				Info(Text.literal("- ").append(AssetFilename(variantId, "models/item/", ".json")));
				Info(Text.literal("- ").append(AssetFilename(variantId, "textures/item/", ".json")));
			break;
		}
	}
}
