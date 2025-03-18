package fr.estecka.variantscit.modules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.api.ICitModule;
import fr.estecka.variantscit.api.IVariantManager;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public record ItemCountModule(String namespace)
implements ICitModule
{
	static public final MapCodec<ItemCountModule> CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			Codec.STRING.fieldOf("namespace").orElse("minecraft").forGetter(ItemCountModule::namespace)
		)
		.apply(builder, ItemCountModule::new)
	);

	@Override
	public ModelIdentifier GetItemModel(ItemStack stack, IVariantManager library){
		for (int c=stack.getCount(); c >= 0; --c){
			Identifier variant = Identifier.of(namespace, String.valueOf(c));
			if (library.HasVariantModel(variant))
				return library.GetVariantModel(variant);
		}

		return null;
	}
}
