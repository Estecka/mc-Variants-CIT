package fr.estecka.variantscit.modulebakers;

import java.util.List;
import fr.estecka.variantscit.commands.CommandLogger;
import fr.estecka.variantscit.commands.WalkthroughData;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public interface IBakedModule
{
	Identifier GetModelForItem(ItemStack stack);

	static public IBakedModule OfList(List<? extends IBakedModule> modules){
		if (modules.isEmpty())
			return __->null;
		else if (modules.size() == 1)
			return modules.get(0);

		return (ItemStack stack)->{
			for (var m : modules){
				Identifier id = m.GetModelForItem(stack);
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

	default Identifier Walkthrough(WalkthroughData debug, ItemStack stack) {
		debug.logger().Error("This module type does not support `walktrough`. Please report this issue.");
		return this.GetModelForItem(stack);
	}
}
