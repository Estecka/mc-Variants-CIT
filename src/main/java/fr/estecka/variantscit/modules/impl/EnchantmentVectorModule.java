package fr.estecka.variantscit.modules.impl;

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
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.modules.libraries.LinearSnapMap;
import fr.estecka.variantscit.modules.libraries.VariantLibrary;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.commands.CommandLogger;
import fr.estecka.variantscit.modules.IBakedModule;
import fr.estecka.variantscit.modules.IModuleBaker;
import fr.estecka.variantscit.modules.cache.CacheKeySet;
import fr.estecka.variantscit.modules.cache.ComponentCacheKey;
import fr.estecka.variantscit.modules.cache.ECachePolicy;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;


public class EnchantmentVectorModule
implements IBakedModule
{
	static private class VectorSpace
	{
		public final Object2IntMap<ResourceLocation> indices;
		private final EnchantVector maxLevels;

		public VectorSpace(Map<ResourceLocation,Integer> maxLevels){
			this.indices = new Object2IntOpenHashMap<>(maxLevels.size());
			int i = 0;
			for (var entry : maxLevels.entrySet())
				this.indices.put(entry.getKey(), i++);
			this.maxLevels = VectorFromMap(maxLevels);
		}

		public EnchantVector TruncatedVectorFromComponent(ItemEnchantments enchants){
			EnchantVector vector = new EnchantVector(this.indices.size());
			for (var entry : enchants.entrySet()){
				int i = this.indices.getOrDefault(entry.getKey().unwrapKey().get().location(), -1);
				if (i >= 0)
					vector.values[i] = Math.min(entry.getIntValue(), maxLevels.values[i]);
			}

			return vector;
		}

		public EnchantVector VectorFromMap(Map<ResourceLocation,Integer> enchants){
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
		ResourceLocation modelId
	) {}

	static public record Parameters(
		boolean optionalLevel,
		List<ToIntFunction<EnchantVector>> ordering,
		String enchantSeparator,
		Optional<String> levelSeparator,
		Map<ResourceLocation,ResourceLocation> aliases,
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
			Codec.BOOL.optionalFieldOf("optionalLevel", true).forGetter(Parameters::optionalLevel),
			NORM_CODEC.listOf(1, 4).optionalFieldOf("ordering", DEFAULT_ORDERING).forGetter(Parameters::ordering),
			CodecUtil.NONEMPTY_STRING.validate(EnchantmentVectorModule::ValidateSeparator).optionalFieldOf("enchantSeparator", "__").forGetter(Parameters::enchantSeparator),
			Codec.STRING.validate(EnchantmentVectorModule::ValidateSeparator).optionalFieldOf("levelSeparator").forGetter(Parameters::levelSeparator),
			Codec.unboundedMap(ResourceLocation.CODEC, ResourceLocation.CODEC).optionalFieldOf("enchantAliases", Map.of()).forGetter(Parameters::aliases),
			CodecUtil.IDENTIFIER_NAMESPACE.optionalFieldOf("namespace", "minecraft").forGetter(Parameters::namespace)
		)
		.apply(builder, Parameters::new)
	);

	private final DataComponentType<ItemEnchantments> componentType;
	private final ResourceLocation fallback;
	private final VectorSpace vectorSpace;
	private final LinearSnapMap<VariantEntry> modelLine;
	private final ToIntFunction<EnchantVector> magnitudeGetter;

	private final Parameters params;

/******************************************************************************/
/* # Baking                                                                   */
/******************************************************************************/

	static public IModuleBaker<Parameters> GetBaker(DataComponentType<ItemEnchantments> component){
		return new IModuleBaker<>() {
			@Override
			public IBakedModule Bake(VariantLibrary library, Parameters parameters) {
				return new EnchantmentVectorModule(library, parameters, component);
			};
			@Override
			public boolean AcceptVariant(ResourceLocation variantId, Parameters parameters) {
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

	@Override
	public CacheKeySet GetCacheKeys() {
		return ComponentCacheKey.KeysOf(componentType);
	}

	@Override
	public ECachePolicy GetCachePolicy() {
		return ECachePolicy.ALWAYS;
	}

	public EnchantmentVectorModule(VariantLibrary variantLibrary, Parameters params, DataComponentType<ItemEnchantments> component){
		VariantsCitMod.LOGGER.PushLabel("enchantment_vector");
		this.params = params;
		this.componentType = component;
		this.fallback = variantLibrary.fallbackModel();
		this.magnitudeGetter = params.ordering.get(0);
		this.modelLine = OrderedSnapMap(params.ordering);

		Pattern enchantRegex = BakeRegex(params);

		Map<ResourceLocation,Integer> enchant2MaxLevel = new HashMap<>();
		Map<Map<ResourceLocation,Integer>, ResourceLocation> vector2Model = new HashMap<>();
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

	static private Optional<Map<ResourceLocation,Integer>> VariantId2Map(Pattern regex, ResourceLocation variantId, Map<ResourceLocation,ResourceLocation> aliases){
		Map<ResourceLocation,Integer> vector = new HashMap<>();
		Matcher matches = regex.matcher(variantId.getPath());
		if (!matches.matches()){
			VariantsCitMod.LOGGER.warn("Not a valid enchantment set: {}", variantId.getPath());
			return Optional.empty();
		}

		matches.reset();
		while(matches.find()){
			String path      = matches.group("path");
			String namespace = Optional.ofNullable(matches.group("namespace")).orElse("minecraft");
			int    level     = Optional.ofNullable(matches.group("lvl")).flatMap(i->SafeParseInt(regex, variantId, i)).orElse(1);

			ResourceLocation enchantId = ResourceLocation.fromNamespaceAndPath(namespace, path);
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

	static private Optional<Integer> SafeParseInt(Pattern regex, ResourceLocation variantId, String input){
		try {
			return Optional.of(Integer.parseInt(input));
		}
		catch (NumberFormatException e){
			VariantsCitMod.LOGGER.error(
				"Critical error parsing enchantment level. Please report this issue!"
				+ "\nRegex: {}"
				+ "\nVariant ID: {}"
				+ "\nRaw level: \"{}\"",
				regex.pattern(),
				variantId,
				input
			);
			return Optional.empty();
		}
	}

	/**
	 * Example regex
	 * (?<=^|.__)(?:(?<namespace>[a-z0-9_.-]*?)\.\.)?(?<path>[a-z0-9_.-]+?)(?:\.(?<lvl>[0-9]+))?(?=__.+|$)
	 */
	static private Pattern BakeRegex(Parameters params){
		String enchantSep = params.enchantSeparator;
		String lvlSep     = params.levelSeparator.orElse("");

		String lvlRegex = "(?<lvl>[0-9]+)";
		if (params.levelSeparator.isPresent())
			lvlRegex = lvlSep + lvlRegex;

		if (params.optionalLevel)
			lvlRegex = "(?:"+lvlRegex+")?";
		else if (!params.levelSeparator.isPresent())
			lvlRegex = "(?:"+lvlRegex+"){0}"; // Causes the group to be accessible, but evaluate to null.

		String regex = "(?<=^|."+enchantSep+")(?:(?<namespace>[a-z0-9_.-]*?)\\.\\.)?(?<path>[a-z0-9_.-]+?)"+lvlRegex+"(?="+enchantSep+".+|$)";

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
	public ResourceLocation GetModelForItem(ItemStack stack) {
		var enchants = stack.get(componentType);
		if (enchants == null || enchants.isEmpty())
			return null;

		VariantEntry result = FindEntry(enchants);
		return (result != null) ? result.modelId : fallback;
	}

	public VariantEntry FindEntry(ItemEnchantments enchants) {
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

	private ResourceLocation[] GetEnchantIds(){
		ResourceLocation[] enchantIds = new ResourceLocation[vectorSpace.indices.size()];
		for (var dimension : vectorSpace.indices.object2IntEntrySet())
			enchantIds[dimension.getIntValue()] = dimension.getKey();
		return enchantIds;
	}

	private void PrintVector(CommandLogger logger, EnchantVector vector, ResourceLocation[] enchantIds, boolean itemSided){
		for (int i=0; i<enchantIds.length; ++i)
		if  (vector.values[i] != 0)
		{
			logger.Info("- lvl {} {}", 
				vector.values[i], 
				itemSided ? CommandLogger.ItemData(enchantIds[i]) : CommandLogger.PackData(enchantIds[i])
			);
		}
	}

	private boolean IsPerfectMatch(EnchantVector model, ItemEnchantments item){
		boolean checkLevels = params.optionalLevel || params.levelSeparator.isPresent();

		if (item.entrySet().size() != model.Dimensionality())
			return false;

		for (var entry : item.entrySet()){
			ResourceLocation enchantId = entry.getKey().unwrapKey().get().location();
			if (!vectorSpace.indices.containsKey(enchantId))
				return false;
			int index = vectorSpace.indices.getInt(enchantId);
			int itemLevel = (checkLevels) ? entry.getIntValue() : 1;
			if (model.values[index] != itemLevel)
				return false;
		}

		return true;
	}

	private String GetPerfectVariantId(ItemEnchantments item){
		String result = "";
		boolean first = true;
		String enchantSep = params.enchantSeparator.replace("\\", "");
		String levelSep = params.levelSeparator.orElse(params.optionalLevel ? "" : null);
		if (levelSep != null)
			levelSep = levelSep.replace("\\", "");

		for (var entry : item.entrySet()){
			if (!first)
				result += enchantSep;
			first = false;
			ResourceLocation id = entry.getKey().unwrapKey().get().location();
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
		logger.Info("The model IDs were parsed using this regex:");
		logger.Info("{}", CommandLogger.PackData(BakeRegex(this.params).pattern()));

		logger.Info("This module has {} variants, spread across {} enchantments:", this.modelLine.size(), this.vectorSpace.indices.size());
		for (ResourceLocation id : this.vectorSpace.indices.keySet())
			logger.Info(" - {}", CommandLogger.ItemData(id));
	}

	@Override
	public void Dump(CommandLogger logger) {
		ResourceLocation[] enchantIds = GetEnchantIds();

		if (this.modelLine.size() <= 0)
			logger.Info("This module does not have any variant.");
		else for (var entry : this.modelLine)
		{
			logger.Info("{}:", CommandLogger.PackData(entry.value().modelId));

			EnchantVector vector = entry.value().vector;
			PrintVector(logger, vector, enchantIds, true);
		}
	}

	@Override
	public ResourceLocation Walkthrough(CommandLogger logger, ItemStack stack) {
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
			logger.Info(ChatFormatting.GRAY, "[TIP] An optimal variant ID for this item could be: {}", CommandLogger.ItemData(perfectVariant));
			logger.Info(ChatFormatting.GRAY, "This estimation does not take into account aliases or syntax issues arising from oddly named enchantments.");
			logger.PrintVariantIdTip(ResourceLocation.fromNamespaceAndPath(this.params.namespace, perfectVariant));
		}
		else
			logger.Info("This is a perfect match.");

		return this.GetModelForItem(stack);
	}

}
