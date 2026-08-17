package fr.estecka.variantscit.commands;

import com.mojang.brigadier.context.CommandContext;
import fr.estecka.variantscit.reload.EAssetType;
import fr.estecka.variantscit.reload.EModuleHook;
import fr.estecka.variantscit.reload.LibraryDefinition;
import fr.estecka.variantscit.reload.MetaModule;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

public class WalktroughLogger
extends CommandLogger
{
	public final EModuleHook moduleHook;
	public final MetaModule metamodule;
	public final String subPrefix;

	public WalktroughLogger(
		CommandContext<FabricClientCommandSource> commandContext,
		EModuleHook moduleHook,
		MetaModule metamodule,
		String subPrefix
	){
		super(commandContext);
		this.moduleHook = moduleHook;
		this.metamodule = metamodule;
		this.subPrefix = subPrefix;
	}

	public EModuleHook moduleHook() { return this.moduleHook; }
	public MetaModule metamodule() { return this.metamodule; }
	public String subPrefix() { return this.subPrefix; }

	public WalktroughLogger WithSubPrefix(String subPrefix){
		return new WalktroughLogger (
			commandContext,
			moduleHook,
			metamodule,
			this.subPrefix + subPrefix
		);
	}

	static private MutableComponent AssetFilename(String modelPrefix, Identifier variantId, EAssetType assetType){
		return TextFormat(ChatFormatting.YELLOW, "/assets/{}/{}/{}{}{}",
			ItemData(variantId.getNamespace()),
			assetType.packDirectory,
			PackData(modelPrefix),
			ItemData(variantId.getPath()),
			assetType.suffix
		);
	}

	static private MutableComponent LayeredAssetFilename(String modelPrefix, Identifier variantId, EAssetType assetType){
		return TextFormat(ChatFormatting.YELLOW, "/assets/{}/{}/{}/{}{}{}",
			ItemData(variantId.getNamespace()),
			assetType.packDirectory,
			ItemData("<layer name>"),
			PackData(modelPrefix),
			ItemData(variantId.getPath()),
			assetType.suffix
		);
	}

	public void PrintPlainModelTip(Identifier modelId){
		Info(ChatFormatting.GRAY,
			"[TIP] The model ID {} may be supported by providing "
			+ "one of these files:",
			ItemData(modelId)
		);

		PrintFileNamesTip("", modelId);
	}

	public void PrintVariantIdTip(Identifier variantId){
		final LibraryDefinition libDef = metamodule.libraryDefinition();
		variantId = variantId.withPrefix(subPrefix);

		Identifier modelId = libDef.GetModelId(variantId);
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

	public void PrintFileNamesTip(String modelPrefix, Identifier variantId){
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
