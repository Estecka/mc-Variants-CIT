package fr.estecka.variantscit.modules.libraries;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.reload.IUnbakedModule;
import net.minecraft.resources.Identifier;

public record LinearModuleBaker<T extends ILinearCitModule>(
	String namespace,
	T parameters
)
implements IUnbakedModule
{
	static public <T extends ILinearCitModule> MapCodec<LinearModuleBaker<T>> Of(MapCodec<T> moduleCodec){
		return RecordCodecBuilder.<LinearModuleBaker<T>>mapCodec(builder->
			builder.group(
				CodecUtil.IDENTIFIER_NAMESPACE.optionalFieldOf("namespace", Identifier.DEFAULT_NAMESPACE).forGetter(LinearModuleBaker::namespace),
				moduleCodec.forGetter(LinearModuleBaker::parameters)
			)
			.apply(builder, LinearModuleBaker::new)
		);
	}


	@Override
	public GenericBakedModule<ILinearLibrary> Bake(VariantLibrary library){
		return new GenericBakedModule<ILinearLibrary>(
			new LinearLibrary(library, namespace),
			parameters
		);
	};

	@Override
	public boolean AcceptsVariant(Identifier variantId) {
		if (!variantId.getNamespace().equals(namespace))
			return false;
		
		try {
			Integer.parseUnsignedInt(variantId.getPath());
		}
		catch (NumberFormatException e){
			return false;
		}
		
		return true;
	};
};
