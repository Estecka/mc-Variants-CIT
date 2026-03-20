package fr.estecka.variantscit.modules.libraries;

import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.modules.IBakedModule;
import fr.estecka.variantscit.reload.IUnbakedModule;

public record VariantModuleBaker<T extends IGenericCitModule<IVariantLibrary>>(
	T parameters
)
implements IUnbakedModule
{
	static public <T extends IGenericCitModule<IVariantLibrary>> MapCodec<VariantModuleBaker<T>> Of(MapCodec<T> moduleCodec){
		return moduleCodec.xmap(
			VariantModuleBaker::new,
			VariantModuleBaker::parameters
		);
	}

	@Override
	public IBakedModule Bake(VariantLibrary library) {
		return new GenericBakedModule<>(library, parameters);
	}
}
