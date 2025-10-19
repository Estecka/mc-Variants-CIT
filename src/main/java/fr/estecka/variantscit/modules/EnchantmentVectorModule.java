package fr.estecka.variantscit.modules;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.LinearSnapMap;
import fr.estecka.variantscit.MultiPropertyCache;
import fr.estecka.variantscit.VariantLibrary;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.modulebakers.IBakedModule;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;


public class EnchantmentVectorModule
implements IBakedModule
{
	static public record Entry(
		Object2IntMap<Identifier> vector,
		Identifier modelId
	) {}

	static public final MapCodec<String> PARAM_MAPCODEC = CodecUtil.IDENTIFIER_NAMESPACE.fieldOf("namespace");

	static private final String enchantSeparator = ValidateSeparator("__").getOrThrow();
	static private final String levelSeparator   = ValidateSeparator(".").getOrThrow();

	private final ComponentType<ItemEnchantmentsComponent> componentType = DataComponentTypes.ENCHANTMENTS;
	private final MultiPropertyCache cache = new MultiPropertyCache(false, componentType);
	private final Identifier fallback;
	private final LinearSnapMap<Entry> modelLine = new LinearSnapMap<>();


/******************************************************************************/
/* # Baking                                                                   */
/******************************************************************************/

	public EnchantmentVectorModule(VariantLibrary variantLibrary, String allowedNamespace){
		this.fallback = variantLibrary.fallbackModel();

		Pattern enchantRegex = BakeRegex(enchantSeparator, levelSeparator);

		Set<Object2IntMap<?>> knownVectors = new HashSet<>();
		Set<String> duplicateIds = new HashSet<>();

		for (var variant : variantLibrary.variantModels().entrySet())
		if  (variant.getKey().getNamespace().equals(allowedNamespace))
		{
			var optVector = VariantId2Vector(enchantRegex, variant.getKey());
			if (optVector.isPresent()){
				var vector = optVector.get();
				if (knownVectors.contains(vector))
					duplicateIds.add(variant.getKey().getPath());
				else {
					knownVectors.add(vector);
					modelLine.AddEntry(GetMagnitude(vector), new Entry(vector, variant.getValue()));
				}
			}
		}

		if (!duplicateIds.isEmpty()){
			String msg = "The following enchantment sets are duplicates:";
			for (String id : duplicateIds) {
				msg += '\n' + id;
			}

			VariantsCitMod.LOGGER.warn(msg);
		}
	}

	static private Optional<Object2IntMap<Identifier>> VariantId2Vector(Pattern regex, Identifier variantId){
		Object2IntMap<Identifier> vector = new Object2IntOpenHashMap<>();
		Matcher matches = regex.matcher(variantId.getPath());
		if (!matches.matches()){
			VariantsCitMod.LOGGER.warn("Not a valid enchantment set: {}", variantId.getPath());
			return Optional.empty();
		}

		while(matches.find()){
			String path      = matches.group("path");
			String namespace = Optional.ofNullable(matches.group("namespace")).orElse("minecraft");
			int    level     = Optional.ofNullable(matches.group("lvl")).map(Integer::parseInt).orElse(1);

			Identifier enchantId = Identifier.of(namespace, path);
			if (vector.containsKey(enchantId)){
				VariantsCitMod.LOGGER.warn("Duplicate enchantment '{}' in set: {}", enchantId, variantId.getPath());
				return Optional.empty();
			}

			vector.put(enchantId, level);
		}

		return Optional.of(vector);
	}

	/**
	 * Example regex
	 * (?<=^|.__)(?:(?<namespace>[a-z0-9_.-]*?)\.\.)?(?<path>[a-z0-9_.-]+?)(?:\.(?<lvl>[0-9]+))?(?=__.+|$)
	 */
	static private Pattern BakeRegex(String echantSep, String lvlSep){
		String regex = "(?<=^|."+enchantSeparator+")(?:(?<namespace>[a-z0-9_.-]*?)\\.\\.)?(?<path>[a-z0-9_.-]+?)(?:\\.(?<lvl>[0-9]+))?(?="+enchantSeparator+".+|$)";
		return Pattern.compile(regex);
	}

	/**
	 * @param raw The separator as specified in the json file.
	 * @return A regex that matches this string literally.
	 */
	static private DataResult<String> ValidateSeparator(String raw){
		if (raw.isEmpty())
			return DataResult.error(()->"Separator cannot be empty");
		if (raw.matches("^[a-z0-9_.-/]+$"))
			return DataResult.error(()->"Separator contains invalid characters: "+raw);

		return DataResult.success(raw.replace(".", "\\."));
	}

	static private int GetMagnitude(Object2IntMap<?> item){
		int magnitude = 0;
		for (var e : item.object2IntEntrySet())
			magnitude += e.getIntValue();

		return magnitude;
	}


/******************************************************************************/
/* # Rendering                                                                */
/******************************************************************************/

	@Override
	public Identifier GetModelForItem(ItemStack stack) {
		return cache.ComputeIfAbsent(stack, this::ComputeItemModel);
	}

	public Identifier ComputeItemModel(ItemStack stack) {
		var enchants = stack.get(componentType);
		if (enchants == null || enchants.isEmpty())
			return null;

		Entry result = modelLine.GetClosestValue(
			GetMagnitude(enchants),
			-1,
			model->IsElligible(enchants, model)
		);

		return (result != null) ? result.modelId : fallback;

	}

	static private int GetMagnitude(ItemEnchantmentsComponent item){
		int magnitude = 0;
		for (var e : item.getEnchantmentEntries())
			magnitude += e.getIntValue();

		return magnitude;
	}

	static private boolean IsElligible(ItemEnchantmentsComponent item, Entry model){
		int maxEnchantCount = item.getSize();

		// Fail if the model has enchantments that are not listed on the item.
		if (model.vector.size() > maxEnchantCount)
			return false;

		for (var itemEnchant : item.getEnchantmentEntries()) {
			if (model.vector.size() > maxEnchantCount)
				return false;

			Identifier enchantId = itemEnchant.getKey().getKey().get().getValue();

			if (!model.vector.containsKey(enchantId)){
				--maxEnchantCount;
				// Fail if the model has enchantments that are not listed on the item.
				if (model.vector.size() > maxEnchantCount)
						return false;
			}
			// Fail if the model has enchantments that are too high level.
			else if (model.vector.getInt(enchantId) > itemEnchant.getIntValue()){
				return false;
			}
		}

		return true;
	}
}
