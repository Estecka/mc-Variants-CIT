package fr.estecka.variantscit.collections;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class NestedMaps
{
	static public <K1,K0,V> IBiMap<K1,K0,V> Create(
		BiFactory<K1,K0,V> sub1,
		UniSupplier<K0,V> sub0
	){
		return sub1.apply(sub0);
	}

	// WIP: Unused, and would require unecessary refactor of TriMap.
	static public <K2,K1,K0,V> HashTriMap<K2,K1,K0,V> Create(
		TriFactory<K2,K1,K0,V> sub2,
		BiFactory<K1,K0,V> sub1,
		UniSupplier<K0,V> sub0
	){
		return sub2.apply(()->Create(sub1, sub0));
	}

	@FunctionalInterface
	public interface UniSupplier<K0,V> 
	extends Supplier< Map<K0,V> >
	{}

	@FunctionalInterface
	public interface BiSupplier<K1,K0,V>
	extends Supplier<IBiMap<K1,K0,V> >
	{}

	@FunctionalInterface
	public interface BiFactory<K1,K0,V>
	extends Function< UniSupplier<K0,V>, IBiMap<K1,K0,V> >
	{}

	@FunctionalInterface
	public interface TriFactory<K2,K1,K0,V>
	extends Function< BiSupplier<K1,K0,V>, HashTriMap<K2,K1,K0,V> >
	{}
}
