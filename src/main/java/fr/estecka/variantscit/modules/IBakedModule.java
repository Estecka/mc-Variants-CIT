package fr.estecka.variantscit.modules;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import fr.estecka.variantscit.commands.CommandLogger;

public interface IBakedModule
{
	ResourceLocation GetModelForItem(ItemStack stack);

	static public IBakedModule OfList(List<? extends IBakedModule> modules){
		if (modules.isEmpty())
			return __->null;
		else if (modules.size() == 1)
			return modules.get(0);

		return (ItemStack stack)->{
			for (var m : modules){
				ResourceLocation id = m.GetModelForItem(stack);
				if (id != null)
					return id;
			}

			return null;
		};
	}

	default void Summary(CommandLogger logger){
		logger.Error("This module type does not support `summary`. Please report this issue.");
	}

	default void Dump(CommandLogger logger){
		logger.Error("This module type does not support `dump`. Please report this issue.");
	}

	default ResourceLocation Walkthrough(CommandLogger logger, ItemStack stack) {
		logger.Error("This module type does not support `walkthrough`. Please report this issue.");
		return this.GetModelForItem(stack);
	}
}
