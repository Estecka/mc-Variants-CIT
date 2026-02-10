package fr.estecka.variantscit.mixin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.client.resources.model.ModelDiscovery;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import fr.estecka.variantscit.duck.DynamicModelResolverDuck;

@Mixin(ModelDiscovery.class)
public interface ReferencedModelsCollectorMixin
{
	@Accessor
	public Map<ResourceLocation,UnbakedModel> getInputModels();

	@Mixin(targets={"net.minecraft.client.resources.model.ModelDiscovery$ResolverImpl"})
	public abstract class ResolverImplMixin
	implements ResolvableModel.Resolver, DynamicModelResolverDuck
	{
		//super.this
		@Shadow(remap=false) private @Final ModelDiscovery field_53669;

		@Override
		public <T> Map<ResourceLocation,T> variantscit$ResolveIf(Function<ResourceLocation,Optional<T>> predicate){
			var result = new HashMap<ResourceLocation,T>();

			for (ResourceLocation id : ((ReferencedModelsCollectorMixin)field_53669).getInputModels().keySet()){
				Optional<T> r = predicate.apply(id);
				if (r.isPresent()){
					this.resolve(id);
					result.put(id, r.get());
				}
			}

			return result;
		}
	}
}
