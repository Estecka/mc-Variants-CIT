package fr.estecka.variantscit.modules;

import java.util.HashSet;
import java.util.Map;
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
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;


public class EnchantmentVectorModule
implements IBakedModule
{
	static public class EnchantVector
	extends Int2IntArrayMap
	{
		public EnchantVector(){ super(); }
		public EnchantVector(int size){ super(size); }

		static public EnchantVector FromComponent(ItemEnchantmentsComponent enchants){
			EnchantVector vector = new EnchantVector(enchants.getSize());
			for (var entry : enchants.getEnchantmentEntries())
				vector.put(entry.getKey().getKey().get().getValue().hashCode(), entry.getIntValue());
			return vector;
		}

		static public EnchantVector FromMap(Map<Identifier,Integer> enchants){
			EnchantVector vector = new EnchantVector(enchants.size());
			for (var entry : enchants.entrySet())
				vector.put(entry.getKey().hashCode(), (int)entry.getValue());
			return vector;
		}

		public int TaxicabMagnitude(){
			int magnitude = 0;
			for (int i : this.values())
				magnitude += i;
			return magnitude;
		}

		public boolean IsWithin(EnchantVector box){
			if (this.size() > box.size())
				return false;

			for (var entry : this.entries)
				if (!box.containsKey(entry.getIntKey()) || entry.getIntValue() > box.get(entry.getIntKey()))
					return false;

			return true;
		}

	}

	static public record VariantEntry(
		EnchantVector vector,
		Identifier modelId
	) {}

	static public final MapCodec<String> PARAM_MAPCODEC = CodecUtil.IDENTIFIER_NAMESPACE.fieldOf("namespace");

	static private final String enchantSeparator = ValidateSeparator("__").getOrThrow();
	static private final String levelSeparator   = ValidateSeparator(".").getOrThrow();

	private final ComponentType<ItemEnchantmentsComponent> componentType = DataComponentTypes.ENCHANTMENTS;
	private final MultiPropertyCache cache = new MultiPropertyCache(false, componentType);
	private final Identifier fallback;
	private final LinearSnapMap<VariantEntry> modelLine = new LinearSnapMap<>();


/******************************************************************************/
/* # Baking                                                                   */
/******************************************************************************/

	public EnchantmentVectorModule(VariantLibrary variantLibrary, String allowedNamespace){
		this.fallback = variantLibrary.fallbackModel();

		Pattern enchantRegex = BakeRegex(enchantSeparator, levelSeparator);

		Set<EnchantVector> knownVectors = new HashSet<>();
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
					modelLine.AddEntry(vector.TaxicabMagnitude(), new VariantEntry(vector, variant.getValue()));
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

	static private Optional<EnchantVector> VariantId2Vector(Pattern regex, Identifier variantId){
		EnchantVector vector = new EnchantVector();
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
			int enchantHash = enchantId.hashCode();
			if (vector.containsKey(enchantHash)){
				VariantsCitMod.LOGGER.warn("Duplicate enchantment '{}' in set: {}", enchantId, variantId.getPath());
				return Optional.empty();
			}

			vector.put(enchantHash, level);
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

		EnchantVector enchantBox = EnchantVector.FromComponent(enchants);
		VariantEntry result = modelLine.GetClosestValue(
			enchantBox.TaxicabMagnitude(),
			-1,
			variant->variant.vector.IsWithin(enchantBox)
		);

		return (result != null) ? result.modelId : fallback;

	}
}
