package fr.estecka.variantscit.modules.impl;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import fr.estecka.variantscit.modules.libraries.ISimpleCitModule;


abstract class ASimpleMonoComponentModule<T>
implements ISimpleCitModule
{
	protected final DataComponentType<T> componentType;

	public ASimpleMonoComponentModule(DataComponentType<T> component){
		this.componentType = component;
	}


	@Override
	public final ResourceLocation GetItemVariant(ItemStack stack){
		T component = stack.get(this.componentType);
		if (component == null)
			return null;
		else
			return this.GetVariantForComponent(component);
	}

	public abstract ResourceLocation GetVariantForComponent(T component);
}
