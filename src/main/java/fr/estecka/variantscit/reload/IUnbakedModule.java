package fr.estecka.variantscit.reload;

import fr.estecka.variantscit.modules.libraries.VariantLibrary;
import net.minecraft.resources.Identifier;
import java.util.Set;
import fr.estecka.variantscit.modules.IBakedModule;

@FunctionalInterface
public interface IUnbakedModule
{
	public IBakedModule Bake(VariantLibrary library);

	public default boolean AcceptsVariant(Identifier variantId){
		return true;
	}

	public default boolean AcceptsIntrinsic(Identifier modelId){
		return this.GetIntrinsicModels().contains(modelId);
	}

	public default Set<Identifier> GetIntrinsicModels(){
		return Set.of();
	}
}
