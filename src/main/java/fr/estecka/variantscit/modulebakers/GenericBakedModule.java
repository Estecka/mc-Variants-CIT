package fr.estecka.variantscit.modulebakers;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * TODO: Although not abstract yet, this class should not be used directly, so
 * that debug commands get implemented in child classes.
 */
public class GenericBakedModule<L>
implements IBakedModule
{
	private final L library;
	private final IGenericCitModule<L> logic;

	public GenericBakedModule(L library, IGenericCitModule<L> logic){
		this.library = library;
		this.logic = logic;
	}

	@Override
	public Identifier GetModelForItem(ItemStack stack) {
		return logic.GetItemModel(stack, library);
	}
}
