package fr.estecka.variantscit.duck;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.util.Identifier;

public interface DynamicModelResolverDuck
{
	<T> Map<Identifier,T> variantscit$ResolveIf(Function<Identifier,Optional<T>> predicate);
}
