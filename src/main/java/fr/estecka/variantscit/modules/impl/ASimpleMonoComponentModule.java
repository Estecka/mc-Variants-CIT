package fr.estecka.variantscit.modules.impl;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import java.util.function.Function;
import fr.estecka.variantscit.modules.libraries.IVariantLibrary;

abstract class ASimpleMonoComponentModule<T>
extends AMonoComponentModule<T>
{
	static public <T> ASimpleMonoComponentModule<T> Of(DataComponentType<T> type, Function<T,ResourceLocation> function){
		return new ASimpleMonoComponentModule<T>(type) {
			@Override
			public ResourceLocation GetVariantForComponent(T component) {
				return function.apply(component);
			}
		};
	}

	public ASimpleMonoComponentModule(DataComponentType<T> component){
		super(component);
	}

	@Override
	public final ResourceLocation GetModelForComponent(T component, IVariantLibrary library) {
		if (component == null)
			return null;
		else
			return library.GetVariantModel(this.GetVariantForComponent(component));
	}

	public abstract ResourceLocation GetVariantForComponent(T component);
}
