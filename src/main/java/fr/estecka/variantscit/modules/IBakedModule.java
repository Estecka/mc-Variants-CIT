package fr.estecka.variantscit.modules;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.commands.CommandLogger;
import fr.estecka.variantscit.commands.WalktroughLogger;
import fr.estecka.variantscit.modules.cache.ICacheKey;

public interface IBakedModule
extends ICacheKey.Cacheable
{
	Identifier GetModelForItem(ItemStack stack);
	void Summary(CommandLogger logger);

	// TODO: Make sure non snitch-based implementations don't merely return GetModelForItem
	Identifier Walkthrough(WalktroughLogger logger, ItemStack stack);

	/**
	 * @return Wether the module has printed any custom information about the variant ID
	 */
	default boolean VariantIdInfo(CommandLogger logger, Identifier variantId){ return false; }

	/**
	 * @return The identifiable module that applied to the item.
	 */
	default IBakedModule Crawl(CommandLogger logger, ItemStack stack, boolean skip){
		boolean success = this.GetModelForItem(stack) != null;
		Identifier moduleId = VariantsCitMod.GetModules().GetId(this);

		String action;
		ChatFormatting format;
		Component successMarker = success ?
			Component.literal("O").withStyle(ChatFormatting.GREEN) :
			Component.literal("X").withStyle(ChatFormatting.RED)
			;

		if (skip){
			action = "Skipped";
			format = ChatFormatting.GRAY;
		}
		else if (success){
			action = "Applied";
			format = ChatFormatting.WHITE;
		}
		else {
			action = "Failed";
			format = ChatFormatting.GRAY;
		}

		if (moduleId != null)
			logger.Info(format, "[{}] {}: {}", successMarker, action, CommandLogger.PackData(moduleId));
		else
			logger.Error("[{}] {} unidentified module: {}", successMarker, action, Integer.toHexString(System.identityHashCode(this)));

		return success ? this : null;
	}
}
