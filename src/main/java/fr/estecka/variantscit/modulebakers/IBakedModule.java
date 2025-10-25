package fr.estecka.variantscit.modulebakers;

import java.util.List;
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
}
