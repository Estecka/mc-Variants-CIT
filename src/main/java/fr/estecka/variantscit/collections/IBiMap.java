package fr.estecka.variantscit.collections;

import java.util.Map;
import java.util.function.Supplier;

public interface IBiMap<K1,K0,V>
extends Map<K1, Map<K0, V>>
{
	Map<K0, V> CreateSubMap(K1 key);

	default V get(K1 key1, K0 key0){
		var submap = this.get(key1);
		if (submap == null)
			return null;
		else
			return submap.get(key0);
	}

	default Map<K0, V> initIfAbsent(K1 key1){
		return this.computeIfAbsent(key1, this::CreateSubMap);
	}

	default V put(K1 key1, K0 key0, V value){
		return this.initIfAbsent(key1)
		           .put(key0, value)
		           ;
	}

	default V computeIfAbsent(K1 key1, K0 key0, Supplier<V> supplier){
		return this.initIfAbsent(key1)
		           .computeIfAbsent(key0, k->supplier.get())
		           ;
	}
	
}
