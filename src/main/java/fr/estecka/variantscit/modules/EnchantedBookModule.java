package fr.estecka.variantscit.modules;

import java.util.Optional;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.api.IVariantManager;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.util.Identifier;

public class EnchantedBookModule
extends AComponentCachingModule<ItemEnchantmentsComponent>
{
	static public final MapCodec<EnchantedBookModule> CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			Codec.STRING.optionalFieldOf("levelSeparator").forGetter(m->m.separator)
		)
		.apply(builder, EnchantedBookModule::new)
	);

	private final Optional<String> separator;

	public EnchantedBookModule(){
		this(Optional.empty());
	}

	public EnchantedBookModule(Optional<String> separator){
		super(DataComponentTypes.STORED_ENCHANTMENTS);
		this.separator = separator;
	}

	@Override
	public ModelIdentifier GetModelForComponent(ItemEnchantmentsComponent enchants, IVariantManager modelProvider){
		if (enchants == null || enchants.isEmpty())
			return null;
		else if (enchants.getSize() > 1)
			return modelProvider.GetSpecialModel("multi");
		else if (separator.isEmpty())
			return modelProvider.GetVariantModel(enchants.getEnchantments().iterator().next().getKey().get().getValue());
		else {
			var enchant = enchants.getEnchantmentEntries().iterator().next();
			int level = enchant.getIntValue();
			Identifier variantId = enchant.getKey().getKey().get().getValue();

			Identifier baseId = variantId.withSuffixedPath(separator.get());
			for (int i=level; 0<=i; --i)
			{
				Identifier leveledId = baseId.withSuffixedPath(String.valueOf(i));
				if (modelProvider.HasVariantModel(leveledId)){
					variantId = leveledId;
					break;
				}
			}

			return modelProvider.GetVariantModel(variantId);
		}
	}
}
