package fr.estecka.variantscit.mixin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import fr.estecka.variantscit.duck.DynamicModelResolverDuck;
import net.minecraft.client.render.model.ReferencedModelsCollector;
import net.minecraft.client.render.model.ResolvableModel;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;

@Mixin(ReferencedModelsCollector.class)
public interface ReferencedModelsCollectorMixin
{
	@Accessor
	public Map<Identifier,UnbakedModel> getInputs();

	@Mixin(targets={"net.minecraft.client.render.model.ReferencedModelsCollector$ResolverImpl"})
	public abstract class ResolverImplMixin
	implements ResolvableModel.Resolver, DynamicModelResolverDuck
	{
		//super.this
		@Shadow(remap=false) private @Final ReferencedModelsCollector field_53669;

		@Override
		public <T> Map<Identifier,T> variantscit$ResolveIf(Function<Identifier,Optional<T>> predicate){
			var result = new HashMap<Identifier,T>();

			for (Identifier id : ((ReferencedModelsCollectorMixin)field_53669).getInputs().keySet()){
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
