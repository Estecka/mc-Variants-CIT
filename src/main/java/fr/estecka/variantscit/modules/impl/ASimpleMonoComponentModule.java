package fr.estecka.variantscit.modules.impl;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import java.util.function.Function;

import fr.estecka.variantscit.modules.cache.ECachePolicy;
import fr.estecka.variantscit.modules.libraries.IVariantLibrary;

abstract class ASimpleMonoComponentModule<T>
extends AMonoComponentModule<T>
{
	static public <T> ASimpleMonoComponentModule<T> Of(ECachePolicy cachePolicy, DataComponentType<T> type, Function<T,ResourceLocation> function){
		return new ASimpleMonoComponentModule<T>(type, cachePolicy) {
			@Override
			public ResourceLocation GetVariantForComponent(T component) {
				return function.apply(component);
			}
		};
	}
	static public <T> ASimpleMonoComponentModule<T> Of(DataComponentType<T> type, Function<T,ResourceLocation> function){
		return Of(ECachePolicy.AVOID, type, function);
	}

	public ASimpleMonoComponentModule(DataComponentType<T> component, ECachePolicy cachePolicy){
		super(component, cachePolicy);
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
