package fr.estecka.variantscit.modules;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.api.ICitModule;
import fr.estecka.variantscit.api.IVariantManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class ItemCountModule
implements ICitModule
{
	static public final MapCodec<ItemCountModule> CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			CodecUtil.IDENTIFIER_NAMESPACE.fieldOf("namespace").orElse("minecraft").forGetter(o->o.namespace)
		)
		.apply(builder, ItemCountModule::new)
	);

	private final Int2ObjectMap<Identifier> cache = new Int2ObjectOpenHashMap<>();
	private final String namespace;

	public ItemCountModule(String namespace){
		this.namespace = namespace;
	}

	@Override
	public Identifier GetItemModel(ItemStack stack, IVariantManager library){
		final int count = stack.getCount();
		if (cache.containsKey(count))
			return cache.get(count);

		Identifier result = null;
		for (int c=count; c >= 0; --c){
			Identifier variant = Identifier.of(namespace, String.valueOf(c));
			if (library.HasVariantModel(variant)){
				result = library.GetVariantModel(variant);
				break;
			}
		}

		cache.put(count, result);
		return result;
	}
}
