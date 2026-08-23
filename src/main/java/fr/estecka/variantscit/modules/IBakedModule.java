package fr.estecka.variantscit.modules;

import java.util.List;
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
	// TODO: Remove, use library in metadata instead
	@Deprecated
	void Dump(CommandLogger logger);
	Identifier Walkthrough(WalktroughLogger logger, ItemStack stack);

	static public IBakedModule OfList(List<? extends IBakedModule> modules){
		if (modules.size() == 1)
			return modules.get(0);
		else
			return new ModuleList(modules);
	}

	/**
	 * @return Wether the module has printed any custom information about the variant ID
	 */
	default boolean VariantIdInfo(CommandLogger logger, Identifier variantId){ return false; }

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
