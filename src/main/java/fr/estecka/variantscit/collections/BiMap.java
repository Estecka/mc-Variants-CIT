package fr.estecka.variantscit.collections;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class BiMap<K1,K0,V>
extends HashMap<K1, Map<K0, V>>
{
	public V get(K1 key1, K0 key0){
		var submap = this.get(key1);
		if (submap == null)
			return null;
		else
			return submap.get(key0);
	}

	private Map<K0, V> computeIfAbsent(K1 key1){
		return this.computeIfAbsent(key1, k->new HashMap<>());
	}

	public V put(K1 key1, K0 key0, V value){
		return this.computeIfAbsent(key1)
		           .put(key0, value)
		           ;
	}

	public V computeIfAbsent(K1 key1, K0 key0, Supplier<V> supplier){
		return this.computeIfAbsent(key1)
		           .computeIfAbsent(key0, k->supplier.get())
		           ;
	}
}
