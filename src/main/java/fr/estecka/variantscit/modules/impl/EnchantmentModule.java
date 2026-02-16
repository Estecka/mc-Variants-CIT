package fr.estecka.variantscit.modules.impl;

import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.modules.libraries.IVariantLibrary;
import fr.estecka.variantscit.commands.CommandLogger;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;

public class EnchantmentModule
extends AComponentCachingModule<ItemEnchantments>
{
	static public final MapCodec<EnchantmentModule> CreateCodec(DataComponentType<ItemEnchantments> targetComponent){
		return RecordCodecBuilder.mapCodec(builder->builder
			.group(
				// TODO: Figure out how to restrict it to specific classes
				// Registries.DATA_COMPONENT_TYPE.getCodec().fieldOf("componentType").forGetter(ItemComponentProperty::componentType),
				Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).optionalFieldOf("requiredEnchantments", Map.of()).forGetter(o->o.precondition),
				Codec.STRING.optionalFieldOf("levelSeparator").forGetter(o->o.separator)
			)
			.apply(builder, (pre,sep)->new EnchantmentModule(targetComponent, pre, sep))
		);
	}

	private final Map<ResourceLocation, Integer> precondition;
	private final Optional<String> separator;

	public EnchantmentModule(
		DataComponentType<ItemEnchantments> component,
		Map<ResourceLocation, Integer> precondition,
		Optional<String> separator
	) {
		super(component);
		this.separator = separator;
		this.precondition = precondition;
	}

	@Override
	public ResourceLocation GetModelForComponent(ItemEnchantments enchants, IVariantLibrary library)
	{
		if (enchants == null || enchants.isEmpty() || !this.MatchesPrecondition(enchants))
			return null;

		if (enchants.size() > precondition.size()+1 && null != library.GetSpecialModel("multi"))
			return library.GetSpecialModel("multi");

		Entry<Holder<Enchantment>> bestFit = GetBestEnchant(enchants, library);
		if (bestFit == null)
			return null;
		else
			return this.GetEnchantModel(bestFit, library);
	}

	private boolean MatchesPrecondition(ItemEnchantments component){
		// Cast the component, so that the keys are plain identifiers, instead
		// of registry entries.
		Object2IntOpenHashMap<ResourceLocation> enchants = new Object2IntOpenHashMap<>();
		for (var entry : component.entrySet())
			enchants.put(entry.getKey().unwrapKey().get().location(), entry.getIntValue());

		for (var condition : this.precondition.entrySet()) {
			if (enchants.getInt(condition.getKey()) < condition.getValue())
				return false;
		}

		return true;
	}

	private @Nullable Entry<Holder<Enchantment>> GetBestEnchant(ItemEnchantments enchants, IVariantLibrary library){
		Entry<Holder<Enchantment>> bestFit = null;
		for (var enchant : enchants.entrySet()){
			if (!this.precondition.containsKey(enchant.getKey().unwrapKey().get().location())
			&&  CompareEnchants(enchant, bestFit, library) > 0
			){
				bestFit = enchant;
			}
		}

		return bestFit;
	}

	private int CompareEnchants(Entry<Holder<Enchantment>> a, Entry<Holder<Enchantment>> b, IVariantLibrary library){
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

	private ResourceLocation GetEnchantModel(Entry<Holder<Enchantment>> enchant, IVariantLibrary library){
		ResourceLocation variantId = enchant.getKey().unwrapKey().get().location();

		if (separator.isPresent()) {
			int level = enchant.getIntValue();
			ResourceLocation baseId = variantId.withSuffix(separator.get());

			for (int i=level; 0<=i; --i)
			{
				ResourceLocation leveledId = baseId.withSuffix(String.valueOf(i));
				if (library.HasVariantModel(leveledId)){
					variantId = leveledId;
					break;
				}
			}
		}

		return library.GetVariantModel(variantId);
	}

	private boolean HasVariantModel(Entry<Holder<Enchantment>> enchant, IVariantLibrary library){
		return null != GetEnchantModel(enchant, library);
	}

	@Override
	public @Nullable ResourceLocation Walkthrough(ItemStack stack, IVariantLibrary library, CommandLogger logger) {
		ItemEnchantments enchants = stack.get(this.componentType);
		if (enchants == null || enchants.isEmpty()){
			logger.Info("The item does not have any enchantment.");
			return null;
		}

		if (!this.MatchesPrecondition(enchants)){
			logger.Info("The item is missing some of the required enchantments.");
			return null;
		}

		if (enchants.size() <= precondition.size()){
			logger.Info("The item does not have any enchantment besides the required ones.");
			return null;
		}

		return super.Walkthrough(stack, library, logger);
	}
}
