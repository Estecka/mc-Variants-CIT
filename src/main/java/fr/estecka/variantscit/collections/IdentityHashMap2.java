package fr.estecka.variantscit.collections;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class IdentityHashMap2<K1,K0,V>
extends IdentityHashMap<K1, Map<K0, V>>
implements Map2<K1,K0,V>
{
	private final Supplier<Map<K0,V>> subFactory;

	public IdentityHashMap2(Supplier<Map<K0,V>> subFactory){
		Objects.requireNonNull(subFactory);
		this.subFactory = subFactory;
	}

	public IdentityHashMap2(){
		this(IdentityHashMap::new);
	}

	@Override
	public Map<K0, V> CreateSubMap(K1 key) {
		return subFactory.get();
	}
}
