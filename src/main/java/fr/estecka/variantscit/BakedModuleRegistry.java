package fr.estecka.variantscit;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public class BakedModuleRegistry
{
	static private Map<Item, IItemModelProvider> ITEM_TO_MODULE = new HashMap<>();
	static private Map<Identifier, IItemModelProvider> MODEL_TO_MODULE = new HashMap<>();
	static private Map<Identifier, BakedModule> ID_TO_MODULE = new HashMap<>();

	static void Clear(){
		ITEM_TO_MODULE = new HashMap<>();
		ID_TO_MODULE = new HashMap<>();
	}

	static void RegisterItem(Item key, IItemModelProvider value){
		ITEM_TO_MODULE.put(key, value);
	}
	static void RegisterModel(Identifier key, IItemModelProvider value){
		MODEL_TO_MODULE.put(key, value);
	}
	static void RegisterId(Identifier key, BakedModule value){
		ID_TO_MODULE.put(key, value);
	}

	static public IItemModelProvider GetForItem(Item item){
		return ITEM_TO_MODULE.get(item);
	}
	static public IItemModelProvider GetForModel(Identifier model){
		return MODEL_TO_MODULE.get(model);
	}
	static public BakedModule GetById(Identifier id){
		return ID_TO_MODULE.get(id);
	}
}
