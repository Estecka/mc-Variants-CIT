package fr.estecka.variantscit.modules;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.commands.CommandLogger;
import fr.estecka.variantscit.commands.WalktroughLogger;
import fr.estecka.variantscit.modules.cache.ICacheKey;

public interface IBakedModule
extends ICacheKey.Cacheable
{
	ResourceLocation GetModelForItem(ItemStack stack);

	static public IBakedModule OfList(List<? extends IBakedModule> modules){
		if (modules.size() == 1)
			return modules.get(0);
		else
			return new ModuleList(modules);
	}

	default IBakedModule Crawl(CommandLogger logger, ItemStack stack, boolean skip){
		boolean success = this.GetModelForItem(stack) != null;
		ResourceLocation moduleId = VariantsCitMod.GetModules().GetId(this);

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
			action = "Tested";
			format = ChatFormatting.GRAY;
		}

		if (moduleId != null)
			logger.Info(format, "[{}] {}: {}", successMarker, action, CommandLogger.PackData(moduleId));
		else
			logger.Error("[{}] {} unidentified module: {}", successMarker, action, Integer.toHexString(System.identityHashCode(this)));

		return success ? this : null;
	}

	/**
	 * TODO: remove default implementations.
	 */
	default void Summary(CommandLogger logger){
		logger.Error("This module type does not support `summary`. Please report this issue.");
	}

	default void Dump(CommandLogger logger){
		logger.Error("This module type does not support `dump`. Please report this issue.");
	}

	default ResourceLocation Walkthrough(WalktroughLogger logger, ItemStack stack) {
		logger.Error("This module type does not support `walkthrough`. Please report this issue.");
		return this.GetModelForItem(stack);
	}
}
