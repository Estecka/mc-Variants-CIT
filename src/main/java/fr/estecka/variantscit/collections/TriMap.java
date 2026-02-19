package fr.estecka.variantscit.collections;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class TriMap<K2,K1,K0,V>
extends BiMap<K2, K1, Map<K0, V>>
{
	public V get(K2 key2, K1 key1, K0 key0){
		return this.getOrDefault(key2, Map.of())
		           .getOrDefault(key1, Map.of())
		           .get(key0)
		           ;
	}

	protected Map<K0, V> computeIfAbsent(K2 key2, K1 key1){
		return this.computeIfAbsent(key2, k->new HashMap<>())
		           .computeIfAbsent(key1, k->new HashMap<>())
		           ;
	}

	public V put(K2 key2, K1 key1, K0 key0, V value){
		return this.computeIfAbsent(key2, key1)
		           .put(key0, value)
		           ;
	}

	public V computeIfAbsent(K2 key2, K1 key1, K0 key0, Supplier<V> supplier){
		return this.computeIfAbsent(key2, key1)
		           .computeIfAbsent(key0, k->supplier.get())
		           ;
	}
}
