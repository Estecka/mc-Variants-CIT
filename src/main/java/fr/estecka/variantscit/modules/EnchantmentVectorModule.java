package fr.estecka.variantscit.modules;

import java.util.Arrays;
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
import fr.estecka.variantscit.commands.CommandLogger;
import fr.estecka.variantscit.modulebakers.IBakedModule;
import fr.estecka.variantscit.modulebakers.IModuleBaker;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;


public class EnchantmentVectorModule
implements IBakedModule
{
	static private class VectorSpace
	{
		public final Object2IntMap<Identifier> indices;
		private final EnchantVector maxLevels;

		public VectorSpace(Map<Identifier,Integer> maxLevels){
			this.indices = new Object2IntOpenHashMap<>(maxLevels.size());
			int i = 0;
			for (var entry : maxLevels.entrySet())
				this.indices.put(entry.getKey(), i++);
			this.maxLevels = VectorFromMap(maxLevels);
		}

		public EnchantVector TruncatedVectorFromComponent(ItemEnchantmentsComponent enchants){
			EnchantVector vector = new EnchantVector(this.indices.size());
			for (var entry : enchants.getEnchantmentEntries()){
				int i = this.indices.getOrDefault(entry.getKey().getKey().get().getValue(), -1);
				if (i >= 0)
					vector.values[i] = Math.min(entry.getIntValue(), maxLevels.values[i]);
			}

			return vector;
		}

		public EnchantVector VectorFromMap(Map<Identifier,Integer> enchants){
			EnchantVector vector = new EnchantVector(this.indices.size());
			for (var entry : enchants.entrySet()){
				int i = this.indices.getOrDefault(entry.getKey(), -1);
				if (i >= 0)
					vector.values[i] = entry.getValue();
			}

			return vector;
		}
	}

	static private class EnchantVector
	{
		public final int[] values;

		public EnchantVector(int size){
			this.values = new int[size];
		}

		@Override
		public int hashCode(){
			return Arrays.hashCode(this.values);
		}

		@Override
		public boolean equals(Object other){
			return other instanceof EnchantVector vec && this.equals(vec);
		}

		public boolean equals(EnchantVector other){
			return Arrays.equals(this.values, other.values);
		}

		public int TaxicabMagnitude(){
			int magnitude = 0;
			for (int i : this.values)
				magnitude += i;
			return magnitude;
		}

		public int EuclidianSquaredMagnitude(){
			int magnitude = 0;
			for (int i : this.values)
				magnitude += i*i;
			return magnitude;
		}

		public int Maximum(){
			int magnitude = 0;
			for (int i : this.values)
				if (i > magnitude)
					magnitude = i;
			return magnitude;
		}

		public int Dimensionality(){
			int dimensions = 0;
			for (int i : this.values)
				if (i != 0)
					++dimensions;
			return dimensions;
		}

		public boolean IsWithin(EnchantVector box){
			for (int i=0; i<values.length; ++i)
				if (this.values[i] > box.values[i])
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
	private final VectorSpace vectorSpace;
	private final LinearSnapMap<VariantEntry> modelLine;
	private final ToIntFunction<EnchantVector> magnitudeGetter;

	private final Parameters params;

/******************************************************************************/
/* # Baking                                                                   */
/******************************************************************************/

	static public IModuleBaker<Parameters> GetBaker(ComponentType<ItemEnchantmentsComponent> component){
		return new IModuleBaker<>() {
			@Override
			public IBakedModule Bake(VariantLibrary library, Parameters parameters) {
				return new EnchantmentVectorModule(library, parameters, component);
			};
			@Override
			public boolean AcceptVariant(Identifier variantId, Parameters parameters) {
				if (!parameters.namespace.equals(variantId.getNamespace()))
					return false;

				Pattern vectorRegex = BakeRegex(parameters);
				if (!vectorRegex.matcher(variantId.getPath()).matches()){
					VariantsCitMod.LOGGER.warn("Not a valid enchantment set: {}", variantId.getPath());
					return false;
				}
				return true;
			};
		};
	}

	public EnchantmentVectorModule(VariantLibrary variantLibrary, Parameters params, ComponentType<ItemEnchantmentsComponent> component){
		VariantsCitMod.LOGGER.PushLabel("enchantment_vector");
		this.params = params;
		this.componentType = component;
		this.fallback = variantLibrary.fallbackModel();
		this.cache = new MultiPropertyCache(params.runtimeDebug, componentType);
		this.magnitudeGetter = params.ordering.get(0);
		this.modelLine = OrderedSnapMap(params.ordering);

		Pattern enchantRegex = BakeRegex(params);

		Map<Identifier,Integer> enchant2MaxLevel = new HashMap<>();
		Map<Map<Identifier,Integer>, Identifier> vector2Model = new HashMap<>();
		Set<String> duplicateIds = new HashSet<>();

		for (var model : variantLibrary.variantModels().entrySet())
		if  (model.getKey().getNamespace().equals(params.namespace))
		{
			var optMap = VariantId2Map(enchantRegex, model.getKey(), params.aliases);
			if (optMap.isPresent()){
				var enchants = optMap.get();
				if (vector2Model.containsKey(enchants))
					duplicateIds.add(model.getKey().getPath());
				else {
					vector2Model.put(enchants, model.getValue());
					for (var e : enchants.entrySet())
						if (e.getValue() > enchant2MaxLevel.getOrDefault(e.getKey(), 0))
							enchant2MaxLevel.put(e.getKey(), e.getValue());
				}
			}
		}

		if (params.bakingDebug){
			String msg = "These enchantments were detected in the CITs. If this looks wrong, check your filenames and your aliases:";
			for (Identifier id : enchant2MaxLevel.keySet())
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

		this.vectorSpace = new VectorSpace(enchant2MaxLevel);
		for (var variant : vector2Model.entrySet()){
			EnchantVector vector = vectorSpace.VectorFromMap(variant.getKey());
			modelLine.AddEntry(magnitudeGetter.applyAsInt(vector), new VariantEntry(vector, variant.getValue()));
		}

		VariantsCitMod.LOGGER.PopLabel();
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

			if (level == 0)
				VariantsCitMod.LOGGER.warn("Level 0 enchantments have no effect. {}", variantId.getPath());

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

		VariantEntry result = FindEntry(enchants);
		return (result != null) ? result.modelId : fallback;
	}

	public VariantEntry FindEntry(ItemEnchantmentsComponent enchants) {
		EnchantVector enchantBox = this.vectorSpace.TruncatedVectorFromComponent(enchants);
		VariantEntry result = modelLine.GetClosestValue(
			magnitudeGetter.applyAsInt(enchantBox),
			-1,
			variant->variant.vector.IsWithin(enchantBox)
		);

		return result;

	}

/******************************************************************************/
/* # Debug Commands                                                           */
/******************************************************************************/

	private Identifier[] GetEnchantIds(){
		Identifier[] enchantIds = new Identifier[vectorSpace.indices.size()];
		for (var dimension : vectorSpace.indices.object2IntEntrySet())
			enchantIds[dimension.getIntValue()] = dimension.getKey();
		return enchantIds;
	}

	private void PrintVector(CommandLogger logger, EnchantVector vector, Identifier[] enchantIds, boolean itemSided){
		for (int i=0; i<enchantIds.length; ++i)
		if  (vector.values[i] != 0)
		{
			logger.Info("- lvl {} {}", 
				vector.values[i], 
				itemSided ? CommandLogger.ItemData(enchantIds[i]) : CommandLogger.PackData(enchantIds[i])
			);
		}
	}

	private boolean IsPerfectMatch(EnchantVector model, ItemEnchantmentsComponent item){
		boolean checkLevels = params.optionalLevel || params.levelSeparator.isPresent();

		for (var entry : item.getEnchantmentEntries()){
			Identifier enchantId = entry.getKey().getKey().get().getValue();
			if (!vectorSpace.indices.containsKey(enchantId))
				return false;
			int index = vectorSpace.indices.getInt(enchantId);
			int itemLevel = (checkLevels) ? entry.getIntValue() : 1;
			if (model.values[index] != itemLevel)
				return false;
		}

		return true;
	}

	private String GetPerfectVariantId(ItemEnchantmentsComponent item){
		String result = "";
		boolean first = true;
		String enchantSep = params.enchantSeparator.replace("\\", "");
		String levelSep = params.levelSeparator.orElse(params.optionalLevel ? "" : null);
		if (levelSep != null)
			levelSep = levelSep.replace("\\", "");

		for (var entry : item.getEnchantmentEntries()){
			if (!first)
				result += enchantSep;
			first = false;
			Identifier id = entry.getKey().getKey().get().getValue();
			if (!id.getNamespace().equals("minecraft"))
				result += id.getNamespace()+"..";
			result += id.getPath();

			if (entry.getIntValue() > 1 && levelSep != null)
				result += levelSep + String.valueOf(entry.getIntValue());
		}

		return result;
	}

	@Override
	public void Summary(CommandLogger logger) {
		logger.Info("This module has {} variants, spread across {} enchantments:", this.modelLine.Size(), this.vectorSpace.indices.size());
		for (Identifier id : this.vectorSpace.indices.keySet())
			logger.Info(" - {}", CommandLogger.ItemData(id));
	}

	@Override
	public void Dump(CommandLogger logger) {
		Identifier[] enchantIds = GetEnchantIds();

		for (var entry : this.modelLine)
		{
			logger.Info("{}:", CommandLogger.PackData(entry.value().modelId));

			EnchantVector vector = entry.value().vector;
			PrintVector(logger, vector, enchantIds, true);
		}
	}

	@Override
	public Identifier Walkthrough(CommandLogger logger, ItemStack stack) {
		var enchants = stack.get(componentType);
		if (enchants == null || enchants.isEmpty()){
			logger.Info("The item has no enchantment.");
			return null;
		}

		VariantEntry result = FindEntry(enchants);
		if (result == null)
			logger.Info("The item has enchantments, but no associated models exist.");
		else {
			logger.Info("Best matching model: ");
			PrintVector(logger, result.vector, GetEnchantIds(), false);
		}


		if (result == null || !IsPerfectMatch(result.vector, enchants)){
			String perfectVariant = GetPerfectVariantId(enchants);
			logger.Info(Formatting.GRAY, "[TIP] An optimal variant ID for this item could be: {}", CommandLogger.ItemData(perfectVariant));
			logger.Info(Formatting.GRAY, "This estimation does not take into account aliases or syntax issues arising from oddly named enchantments.");
			logger.PrintVariantIdTip(Identifier.of(this.params.namespace, perfectVariant));
		}
		else
			logger.Info("This is a perfect match.");

		return this.ComputeItemModel(stack);
	}

}
