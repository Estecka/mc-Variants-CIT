package fr.estecka.variantscit.duck;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;

public interface DynamicModelResolverDuck
{
	<T> Map<ResourceLocation,T> variantscit$ResolveIf(Function<ResourceLocation,Optional<T>> predicate);
}
