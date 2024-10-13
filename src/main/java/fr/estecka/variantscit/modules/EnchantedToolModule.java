package fr.estecka.variantscit.modules;

import java.util.Map;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.api.IVariantManager;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class EnchantedToolModule
extends AComponentCachingModule<ItemEnchantmentsComponent>
{
	static public final MapCodec<EnchantedToolModule> CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			Codec.unboundedMap(Identifier.CODEC, Codec.INT).fieldOf("requiredEnchantments").orElse(Map.of()).forGetter(p->p.precondition)
		)
		.apply(builder, EnchantedToolModule::new)
	);

	private final Map<Identifier, Integer> precondition;

	public EnchantedToolModule(Map<Identifier, Integer> precondition){
		super(DataComponentTypes.ENCHANTMENTS);
		this.precondition = precondition;
	}

	@Override
	public ModelIdentifier GetModelForComponent(ItemEnchantmentsComponent enchants, IVariantManager models){
		if (enchants == null || enchants.isEmpty() || !this.Matches(enchants))
			return null;

		Entry<RegistryEntry<Enchantment>> bestFit = null;
		for (var contestant : enchants.getEnchantmentEntries()){
			if (!this.precondition.containsKey(contestant.getKey().getKey().get().getValue())
			&&  Compare(contestant, bestFit, models) > 0
			){
				bestFit = contestant;
			}
		}

		if (bestFit == null)
			return null;
		else
			return models.GetVariantModel(bestFit.getKey().getKey().get().getValue());
	}

	public int Compare(Entry<RegistryEntry<Enchantment>> a, Entry<RegistryEntry<Enchantment>> b, IVariantManager models){
		int result = 0;

		if (a == null) return -1;
		if (b == null) return 1;

		result = Boolean.compare(
			models.HasVariantModel(a.getKey().getKey().get().getValue()),
			models.HasVariantModel(b.getKey().getKey().get().getValue())
		);
		if (result != 0) return result;

		result = a.getKey().value().exclusiveSet().size() - b.getKey().value().exclusiveSet().size();
		if (result != 0) return result;

		result = a.getIntValue() - b.getIntValue();
		if (result != 0) return result;

		return result;
	}

	public boolean Matches(ItemEnchantmentsComponent component){
		Object2IntOpenHashMap<Identifier> enchants = new Object2IntOpenHashMap<>();
		for (var entry : component.getEnchantmentEntries())
			enchants.put(entry.getKey().getKey().get().getValue(), entry.getIntValue());

		for (var condition : this.precondition.entrySet())
			if (enchants.getInt(condition.getKey()) < condition.getValue())
				return false;

		return true;
	}
}
