package fr.estecka.variantscit.modules;

import java.util.Map;
import java.util.Optional;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.api.IVariantManager;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class EnchantmentModule
extends AComponentCachingModule<ItemEnchantmentsComponent>
{
	static public final MapCodec<EnchantmentModule> CreateCodec(ComponentType<ItemEnchantmentsComponent> targetComponent){
		return RecordCodecBuilder.mapCodec(builder->builder
		.group(
			// TODO: Figure out how to restrict it to specific classes
			// Registries.DATA_COMPONENT_TYPE.getCodec().fieldOf("componentType").forGetter(ItemComponentProperty::componentType),
			Codec.unboundedMap(Identifier.CODEC, Codec.INT).optionalFieldOf("requiredEnchantments", Map.of()).forGetter(o->o.precondition),
			Codec.STRING.optionalFieldOf("levelSeparator").forGetter(o->o.separator)
			)
			.apply(builder, (pre,sep)->new EnchantmentModule(targetComponent, pre, sep))
		);
	}

	private final Map<Identifier, Integer> precondition;
	private final Optional<String> separator;

	public EnchantmentModule(
		ComponentType<ItemEnchantmentsComponent> component,
		Map<Identifier, Integer> precondition,
		Optional<String> separator
	) {
		super(component);
		this.separator = separator;
		this.precondition = precondition;
	}

	@Override
	public ModelIdentifier GetModelForComponent(ItemEnchantmentsComponent enchants, IVariantManager modelProvider){
		if (enchants == null || enchants.isEmpty() || !this.MatchesPrecondition(enchants))
			return null;

		if (enchants.getSize() > 1 && null != modelProvider.GetSpecialModel("multi"))
			return modelProvider.GetSpecialModel("multi");

		Entry<RegistryEntry<Enchantment>> bestFit = null;
		for (var contestant : enchants.getEnchantmentEntries()){
			if (!this.precondition.containsKey(contestant.getKey().getKey().get().getValue())
			&&  CompareEnchants(contestant, bestFit, modelProvider) > 0
			){
				bestFit = contestant;
			}
		}

		if (bestFit == null)
			return null;
		else if (separator.isEmpty())
			return modelProvider.GetVariantModel(bestFit.getKey().getKey().get().getValue());
		else {
			int level = bestFit.getIntValue();
			Identifier variantId = bestFit.getKey().getKey().get().getValue();

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

	private int CompareEnchants(Entry<RegistryEntry<Enchantment>> a, Entry<RegistryEntry<Enchantment>> b, IVariantManager models){
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

	private boolean MatchesPrecondition(ItemEnchantmentsComponent component){
		Object2IntOpenHashMap<Identifier> enchants = new Object2IntOpenHashMap<>();
		for (var entry : component.getEnchantmentEntries())
			enchants.put(entry.getKey().getKey().get().getValue(), entry.getIntValue());

		for (var condition : this.precondition.entrySet())
			if (enchants.getInt(condition.getKey()) < condition.getValue())
				return false;

		return true;
	}
}
