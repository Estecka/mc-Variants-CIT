package fr.estecka.variantscit.collections;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class NestedMaps
{
	static public <K1,K0,V> Map2<K1,K0,V> Create(
		Factory2<K1,K0,V> sub1,
		Supplier1<K0,V> sub0
	){
		return sub1.apply(sub0);
	}

	// WIP: Unused. Would require currently unecessary refactor of HashMap3 to work.
	static public <K2,K1,K0,V> HashMap3<K2,K1,K0,V> Create(
		Factory3<K2,K1,K0,V> sub2,
		Factory2<K1,K0,V> sub1,
		Supplier1<K0,V> sub0
	){
		return sub2.apply(()->Create(sub1, sub0));
	}

	@FunctionalInterface
	public interface Supplier1<K0,V> 
	extends Supplier< Map<K0,V> >
	{}

	@FunctionalInterface
	public interface Supplier2<K1,K0,V>
	extends Supplier<Map2<K1,K0,V> >
	{}

	@FunctionalInterface
	public interface Factory2<K1,K0,V>
	extends Function< Supplier1<K0,V>, Map2<K1,K0,V> >
	{}

	@FunctionalInterface
	public interface Factory3<K2,K1,K0,V>
	extends Function< Supplier2<K1,K0,V>, HashMap3<K2,K1,K0,V> >
	{}
}
