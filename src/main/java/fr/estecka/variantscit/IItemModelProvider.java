package fr.estecka.variantscit;

import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public interface IItemModelProvider
{
	Identifier GetModelForItem(ItemStack stack);

	static public IItemModelProvider OfList(List<? extends IItemModelProvider> providers){
		if (providers.isEmpty())
			return __->null;
		else if (providers.size() == 1)
			return providers.get(0);

		return (ItemStack stack)->{
			Identifier id;
			for (var p : providers)
				if (null != (id=p.GetModelForItem(stack)))
					return id;

			return null;
		};
	}
}
