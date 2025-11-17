package fr.estecka.variantscit.commands;

import fr.estecka.variantscit.reload.EModuleContext;
import fr.estecka.variantscit.reload.ModuleLoader.MetaModule;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public record WalkthroughData(
	CommandLogger logger,
	EModuleContext context,
	MetaModule metamodule
)
{

	static private String AssetFilename(Identifier id, String prefix, String suffix){
		return "/assets/"+id.getNamespace()+"/"+prefix+id.getPath()+suffix;
	}

	public void PrintVariantIdTip(Identifier variantId){
		logger.Info(
			Text.literal("TIP: The variant ID ").formatted(Formatting.ITALIC)
				.append(Text.literal(variantId.toString()).formatted(Formatting.AQUA))
				.append(" may be supported by providing one of these files:")
		);

		switch (context()) {
			default: logger.Error("Error: unknown context");
			case EQUIPPABLE:
				logger.Info(Text.literal("- ").append(Text.literal(AssetFilename(variantId, "items/", ".json")).formatted(Formatting.YELLOW)));
			break;
			case ITEM_MODEL:
				logger.Info(Text.literal("- ").append(Text.literal(AssetFilename(variantId, "items/", ".png")).formatted(Formatting.YELLOW)));
				logger.Info(Text.literal("- ").append(Text.literal(AssetFilename(variantId, "models/item/", ".json")).formatted(Formatting.YELLOW)));
				logger.Info(Text.literal("- ").append(Text.literal(AssetFilename(variantId, "textures/item/", ".json")).formatted(Formatting.YELLOW)));
			break;
		}
	}
}
