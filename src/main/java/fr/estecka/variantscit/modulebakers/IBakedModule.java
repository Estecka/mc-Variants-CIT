package fr.estecka.variantscit.modulebakers;

import java.util.List;
import fr.estecka.variantscit.commands.CommandLogger;
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

	default void Dump(CommandLogger logger){
		logger.Error("This module type does not implement `dump`");
	}

	default void Walkthrough(CommandLogger logger){
		logger.Error("This module type does not implement `waktrough`");
	}
}
