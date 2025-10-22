package fr.estecka.variantscit.modules;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.ToIntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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

		public int EuclidianSquaredMagnitude(){
			int magnitude = 0;
			for (int i : this.values())
				magnitude += i*i;
			return magnitude;
		}

		public int Maximum(){
			int magnitude = 0;
			for (int i : this.values())
				if (i > magnitude)
					magnitude = i;
			return magnitude;
		}

		public int Dimensionality(){
			int dimensions = 0;
			for (int i : this.values())
				if (i != 0)
					++dimensions;
			return dimensions;
		}

		public boolean IsWithin(EnchantVector box){
			if (this.size() > box.size())
				return false;

			for (var entry : this.int2IntEntrySet())
				if (!box.containsKey(entry.getIntKey()) || entry.getIntValue() > box.get(entry.getIntKey()))
					return false;

			return true;
		}

	}

	static public record VariantEntry(
		EnchantVector vector,
		Identifier modelId
	) {}

	static public record Parameters(
		boolean bakingDebug,
		boolean runtimeDebug,
		boolean optionalLevel,
		List<ToIntFunction<EnchantVector>> ordering,
		String enchantSeparator,
		Optional<String> levelSeparator,
		Map<Identifier,Identifier> aliases,
		String namespace
	) {}

	static public final Codec<ToIntFunction<EnchantVector>> NORM_CODEC = CodecUtil.Enum(Codec.STRING, Map.of(
		"taxicab",   EnchantVector::TaxicabMagnitude,
		"euclidian", EnchantVector::EuclidianSquaredMagnitude,
		"maximum",   EnchantVector::Maximum,
		"dimension", EnchantVector::Dimensionality
	));

	static private final List<ToIntFunction<EnchantVector>> DEFAULT_ORDERING = List.of(EnchantVector::TaxicabMagnitude, EnchantVector::Dimensionality);

	static public final MapCodec<Parameters> PARAM_MAPCODEC = RecordCodecBuilder.mapCodec(builder->
		builder.group(
			Codec.BOOL.optionalFieldOf("bakingDebug",  false).forGetter(Parameters::bakingDebug),
			Codec.BOOL.optionalFieldOf("runtimeDebug", false).forGetter(Parameters::runtimeDebug),
			Codec.BOOL.optionalFieldOf("optionalLevel", false).forGetter(Parameters::optionalLevel),
			NORM_CODEC.listOf(1, 4).optionalFieldOf("ordering", DEFAULT_ORDERING).forGetter(Parameters::ordering),
			CodecUtil.NONEMPTY_STRING.validate(EnchantmentVectorModule::ValidateSeparator).optionalFieldOf("enchantSeparator", "__").forGetter(Parameters::enchantSeparator),
			Codec.STRING.validate(EnchantmentVectorModule::ValidateSeparator).optionalFieldOf("levelSeparator").forGetter(Parameters::levelSeparator),
			Codec.unboundedMap(Identifier.CODEC, Identifier.CODEC).optionalFieldOf("enchantAliases", Map.of()).forGetter(Parameters::aliases),
			CodecUtil.IDENTIFIER_NAMESPACE.optionalFieldOf("namespace", "minecraft").forGetter(Parameters::namespace)
		)
		.apply(builder, Parameters::new)
	);

	private final ComponentType<ItemEnchantmentsComponent> componentType;
	private final MultiPropertyCache cache;
	private final Identifier fallback;
	private final LinearSnapMap<VariantEntry> modelLine;
	private final ToIntFunction<EnchantVector> magnitudeGetter;

/******************************************************************************/
/* # Baking                                                                   */
/******************************************************************************/

	public EnchantmentVectorModule(VariantLibrary variantLibrary, Parameters params, ComponentType<ItemEnchantmentsComponent> component){
		this.componentType = component;
		this.fallback = variantLibrary.fallbackModel();
		this.cache = new MultiPropertyCache(params.runtimeDebug, componentType);
		this.magnitudeGetter = params.ordering.get(0);
		this.modelLine = OrderedSnapMap(params.ordering);

		VariantsCitMod.LOGGER.PushLabel("enchantment_vector");
		Pattern enchantRegex = BakeRegex(params);

		Set<Identifier> knownEnchants = new HashSet<>();
		Set<EnchantVector> knownVectors = new HashSet<>();
		Set<String> duplicateIds = new HashSet<>();

		for (var variant : variantLibrary.variantModels().entrySet())
		if  (variant.getKey().getNamespace().equals(params.namespace))
		{
			var optMap = VariantId2Map(enchantRegex, variant.getKey(), params.aliases);
			if (optMap.isPresent()){
				var vector = EnchantVector.FromMap(optMap.get());
				if (knownVectors.contains(vector))
					duplicateIds.add(variant.getKey().getPath());
				else {
					knownVectors.add(vector);
					knownEnchants.addAll(optMap.get().keySet());
					modelLine.AddEntry(magnitudeGetter.applyAsInt(vector), new VariantEntry(vector, variant.getValue()));
				}
			}
		}

		if (params.bakingDebug){
			String msg = "These enchantments were detected in the CITs. If this looks wrong, check your filenames and your aliases:";
			for (Identifier id : knownEnchants)
				msg += '\n' + id.toString();
			VariantsCitMod.LOGGER.info(msg);
		}

		if (!duplicateIds.isEmpty()){
			String msg = "The following variant IDs describe duplicate enchantment sets and will be ignored:";
			for (String id : duplicateIds) {
				msg += '\n' + id;
			}

			VariantsCitMod.LOGGER.warn(msg);
		}

		VariantsCitMod.LOGGER.PopLabel();
	}

	static public EnchantmentVectorModule Bake(VariantLibrary lib, Parameters params) {
		return new EnchantmentVectorModule(lib, params, DataComponentTypes.ENCHANTMENTS);
	}
	static public EnchantmentVectorModule BakeStored(VariantLibrary lib, Parameters params) {
		return new EnchantmentVectorModule(lib, params, DataComponentTypes.STORED_ENCHANTMENTS);
	}

	static private LinearSnapMap<VariantEntry> OrderedSnapMap(List<ToIntFunction<EnchantVector>> ordering){
		if (ordering.size() < 2)
			return new LinearSnapMap<>();

		@SuppressWarnings("unchecked")
		final Comparator<VariantEntry>[] tiebreaker = new Comparator[ordering.size()-1];
		for (int i=1; i<ordering.size(); ++i)
			tiebreaker[i-1] = Comparator.comparing(VariantEntry::vector, Comparator.comparingInt(ordering.get(i)));

		return new LinearSnapMap<>((a,b)->{
			for (var comp : tiebreaker){
				int r = comp.compare(a, b);
				if (r != 0)
					return r;
			}
			return 0;
		});
	}

	static private Optional<Map<Identifier,Integer>> VariantId2Map(Pattern regex, Identifier variantId, Map<Identifier,Identifier> aliases){
		Map<Identifier,Integer> vector = new HashMap<>();
		Matcher matches = regex.matcher(variantId.getPath());
		if (!matches.matches()){
			VariantsCitMod.LOGGER.warn("Not a valid enchantment set: {}", variantId.getPath());
			return Optional.empty();
		}

		matches.reset();
		while(matches.find()){
			String path      = matches.group("path");
			String namespace = Optional.ofNullable(matches.group("namespace")).orElse("minecraft");
			int    level     = Optional.ofNullable(matches.group("lvl")).map(Integer::parseInt).orElse(1);

			Identifier enchantId = Identifier.of(namespace, path);
			enchantId = aliases.getOrDefault(enchantId, enchantId);
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
	static private Pattern BakeRegex(Parameters params){
		String enchantSep = params.enchantSeparator;
		String lvlSep     = params.levelSeparator.orElse("");

		String lvlRegex;
		if (params.levelSeparator.isPresent() || params.optionalLevel)
			lvlRegex = lvlSep + "(?<lvl>[0-9]+)";
		else
			lvlRegex= "(?<lvl>)";

		if (params.optionalLevel)
			lvlRegex = "(?:"+lvlRegex+")?";

		String regex = "(?<=^|."+enchantSep+")(?:(?<namespace>[a-z0-9_.-]*?)\\.\\.)?(?<path>[a-z0-9_.-]+?)"+lvlRegex+"(?="+enchantSep+".+|$)";
		// if (params.bakingDebug)
		// 	VariantsCitMod.LOGGER.info("Filenames will be parsed using this regex:\n{}", regex);

		return Pattern.compile(regex);
	}

	/**
	 * @param raw The separator as specified in the json file.
	 * @return A regex that matches this string literally.
	 */
	static private DataResult<String> ValidateSeparator(String raw){
		if (!raw.matches("^[a-z0-9_.-/]*$"))
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
			magnitudeGetter.applyAsInt(enchantBox),
			-1,
			variant->variant.vector.IsWithin(enchantBox)
		);

		return (result != null) ? result.modelId : fallback;

	}
}
