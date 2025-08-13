package fr.estecka.variantscit.modules;

import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;
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
	public ModelIdentifier GetModelForComponent(ItemEnchantmentsComponent enchants, IVariantManager library)
	{
		if (enchants == null || enchants.isEmpty() || !this.MatchesPrecondition(enchants))
			return null;

		if (enchants.getSize() > precondition.size()+1 && null != library.GetSpecialModel("multi"))
			return library.GetSpecialModel("multi");

		Entry<RegistryEntry<Enchantment>> bestFit = GetBestEnchant(enchants, library);
		if (bestFit == null)
			return null;
		else
			return this.GetEnchantModel(bestFit, library);
	}

	private boolean MatchesPrecondition(ItemEnchantmentsComponent component){
		// Cast the component, so that the keys are plain identifiers, instead
		// of registry entries.
		Object2IntOpenHashMap<Identifier> enchants = new Object2IntOpenHashMap<>();
		for (var entry : component.getEnchantmentEntries())
			enchants.put(entry.getKey().getKey().get().getValue(), entry.getIntValue());

		for (var condition : this.precondition.entrySet()) {
			if (enchants.getInt(condition.getKey()) < condition.getValue())
				return false;
		}

		return true;
	}

	private @Nullable Entry<RegistryEntry<Enchantment>> GetBestEnchant(ItemEnchantmentsComponent enchants, IVariantManager library){
		Entry<RegistryEntry<Enchantment>> bestFit = null;
		for (var enchant : enchants.getEnchantmentEntries()){
			if (!this.precondition.containsKey(enchant.getKey().getKey().get().getValue())
			&&  CompareEnchants(enchant, bestFit, library) > 0
			){
				bestFit = enchant;
			}
		}

		return bestFit;
	}

	private int CompareEnchants(Entry<RegistryEntry<Enchantment>> a, Entry<RegistryEntry<Enchantment>> b, IVariantManager library){
		int result = 0;

		if (a == null) return -1;
		if (b == null) return 1;

		result = Boolean.compare(
			this.HasVariantModel(a, library),
			this.HasVariantModel(b, library)
		);
		if (result != 0) return result;

		result = a.getKey().value().exclusiveSet().size() - b.getKey().value().exclusiveSet().size();
		if (result != 0) return result;

		result = a.getIntValue() - b.getIntValue();
		if (result != 0) return result;

		return result;
	}

	private ModelIdentifier GetEnchantModel(Entry<RegistryEntry<Enchantment>> enchant, IVariantManager library){
		Identifier variantId = enchant.getKey().getKey().get().getValue();

		if (separator.isPresent()) {
			int level = enchant.getIntValue();
			Identifier baseId = variantId.withSuffixedPath(separator.get());

			for (int i=level; 0<=i; --i)
			{
				Identifier leveledId = baseId.withSuffixedPath(String.valueOf(i));
				if (library.HasVariantModel(leveledId)){
					variantId = leveledId;
					break;
				}
			}
		}

		return library.GetVariantModel(variantId);
	}

	private boolean HasVariantModel(Entry<RegistryEntry<Enchantment>> enchant, IVariantManager library){
		return null != GetEnchantModel(enchant, library);
	}
}
