package fr.estecka.variantscit.assetgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.format.Substitution;
import fr.estecka.variantscit.reload.ModuleDefinition;
import net.minecraft.util.Identifier;

public class GeneratorPresets
{
	static private final Pattern BOW_SUBVARIANTS      = Pattern.compile(".+_pulling_[0-9]+");
	static private final Pattern CROSSBOW_SUBVARIANTS = Pattern.compile(".+(_arrow|_firework|_pulling_[0-9]+)");
	static private final Pattern HORN_SUBVARIANTS     = Pattern.compile("tooting_.+");
	static private final Pattern ROD_SUBVARIANTS      = Pattern.compile(".+_cast");
	static private final Pattern SHIELD_SUBVARIANTS   = Pattern.compile(".+_blocking");
	static private final Pattern TRIDENT_SUBVARIANTS  = Pattern.compile(".+(_in_hand|_throwing)");

	static private final IBuilder MODELS_GENERATED        = TemplateBuilder.ModelParent("item/item_generated");
	static private final IBuilder MODELS_HANDHELD         = TemplateBuilder.ModelParent("item/handheld");
	static private final IBuilder MODELS_ROD              = TemplateBuilder.ModelParent("item/handheld_rod");
	static private final IBuilder MODELS_BOW              = TemplateBuilder.ModelParent("item/bow");
	static private final IBuilder MODELS_CROSSBOW         = TemplateBuilder.ModelParent("item/crossbow");
	static private final IBuilder MODELS_HORN_STANDBY     = TemplateBuilder.ModelParent("item/goat_horn");
	static private final IBuilder MODELS_HORN_TOOTING     = TemplateBuilder.ModelParent("item/tooting_goat_horn");
	static private final IBuilder MODELS_TRIDENT_GUI_ONLY = TemplateBuilder.ModelParent("item/generated").ExcludeRegex(TRIDENT_SUBVARIANTS);
	static private final IBuilder MODELS_TRIDENT_IN_HAND  = TemplateBuilder.ModelParent("variants-cit:item/trident_in_hand" ).IncludeSuffix("_in_hand" );
	static private final IBuilder MODELS_TRIDENT_THROWING = TemplateBuilder.ModelParent("variants-cit:item/trident_throwing").IncludeSuffix("_throwing");

	static private final IBuilder ITEMS_STATELESS        = TemplateBuilder.ItemStates("items/stateless");
	static private final IBuilder ITEMS_BOW              = TemplateBuilder.ItemStates("items/bow").ExcludeRegex(BOW_SUBVARIANTS);
	static private final IBuilder ITEMS_CROSSBOW         = TemplateBuilder.ItemStates("items/crossbow").ExcludeRegex(CROSSBOW_SUBVARIANTS);
	static private final IBuilder ITEMS_FISHING_ROD      = TemplateBuilder.ItemStates("items/fishing_rod").ExcludeRegex(ROD_SUBVARIANTS);
	static private final IBuilder ITEMS_SHIELD           = TemplateBuilder.ItemStates("items/shield").ExcludeRegex(SHIELD_SUBVARIANTS);
	static private final IBuilder ITEMS_GOAT_HORN        = TemplateBuilder.ItemStates("items/goat_horn").ExcludeRegex(HORN_SUBVARIANTS);
	static private final IBuilder ITEMS_TRIDENT          = TemplateBuilder.ItemStates("items/trident").ExcludeRegex(TRIDENT_SUBVARIANTS);
	static private final IBuilder ITEMS_TRIDENT_GUI_ONLY = TemplateBuilder.ItemStates("items/trident_gui_only");

	static private final Map<String, IBuilder> PRESETS = new HashMap<>();
	static public final Codec<IAssetGenerator> PRESET_CODEC = CodecUtil.Enum(Codec.STRING, PRESETS).flatXmap(IBuilder::get, _0->null);
	static {
		// Fullstack generators
		PRESETS.put("item_model/generated",        ListBuilder.Of(ITEMS_STATELESS, MODELS_GENERATED));
		PRESETS.put("item_model/handheld",         ListBuilder.Of(ITEMS_STATELESS, MODELS_HANDHELD));
		PRESETS.put("item_model/bow",              ListBuilder.Of(ITEMS_BOW, MODELS_BOW));
		PRESETS.put("item_model/crossbow",         ListBuilder.Of(ITEMS_CROSSBOW, MODELS_CROSSBOW));
		PRESETS.put("item_model/trident",          ListBuilder.Of(ITEMS_TRIDENT, MODELS_TRIDENT_GUI_ONLY, MODELS_TRIDENT_IN_HAND, MODELS_TRIDENT_THROWING));
		PRESETS.put("item_model/trident_gui_only", ListBuilder.Of(ITEMS_TRIDENT_GUI_ONLY, MODELS_GENERATED));

		// Baked model generators
		PRESETS.put("models/trident_gui_only", MODELS_TRIDENT_GUI_ONLY);
		PRESETS.put("models/trident_in_hand",  MODELS_TRIDENT_IN_HAND );
		PRESETS.put("models/trident_throwing", MODELS_TRIDENT_THROWING);

		// Item-state generators
		PRESETS.put("items/bow",         ITEMS_BOW);
		PRESETS.put("items/crossbow",    ITEMS_CROSSBOW);
		PRESETS.put("items/fishing_rod", ITEMS_FISHING_ROD);
		PRESETS.put("items/goat_horn",   ITEMS_GOAT_HORN);
		PRESETS.put("items/shield",      ITEMS_SHIELD);
		PRESETS.put("items/trident",     ITEMS_TRIDENT);
	}

	static public Predicate<Identifier> ExcludeRegex(Pattern regex){
		return (Identifier id) -> !regex.matcher(id.getPath()).matches();
	}

	static public Predicate<Identifier> IncludeRegex(Pattern regex){
		return (Identifier id) -> regex.matcher(id.getPath()).matches();
	}

	static public IAssetGenerator LegacyGenerator(ModuleDefinition module){
		IAssetGenerator items;
		IAssetGenerator models;
		if (!module.itemGen())
			return IAssetGenerator.NOOP;

		var optItems = ITEMS_STATELESS.get();
		if (optItems.isError()){
			VariantsCitMod.LOGGER.error("Bad item state generator: {}", optItems.error().get().message());
			return IAssetGenerator.NOOP;
		}

		items = optItems.getOrThrow();

		if (!module.modelParent().isPresent())
			return items;

		var optModels = TemplateBuilder.ModelParent(module.modelParent().get()).get();
		if (optModels.isError()){
			VariantsCitMod.LOGGER.error("Bad baked model generator: {}", optModels.error().get().message());
			return items;
		}

		models = optModels.getOrThrow();

		return IAssetGenerator.OfList(items, models);
	}


/******************************************************************************/
/* # Builders                                                                 */
/******************************************************************************/
	/**
	 * Generators depend on templates, which are not available at compile time.
	 * The builders will recreate the generators based on available templates.
	 */
	static public interface IBuilder
	extends Supplier<DataResult<IAssetGenerator>>
	{}

	static public record ListBuilder(IBuilder[] builders)
	implements IBuilder
	{
		@SafeVarargs
		static public ListBuilder Of(IBuilder... builders){
			return new ListBuilder(builders);
		}
	
		@Override
		public DataResult<IAssetGenerator> get() {
			List<IAssetGenerator> generators = new ArrayList<>(builders.length);
			for (var b : builders){
				DataResult<IAssetGenerator> r = b.get();
				if (r.isError())
					return r;
				else
					generators.add(r.getOrThrow());
			}

			return DataResult.success(IAssetGenerator.OfList(generators));
		}
	}

	static public class TemplateBuilder
	implements IBuilder
	{
		private EAssetGenPass pass;
		private Identifier templateId;
		private Predicate<Identifier> acceptedVariants = id->true;
		private Map<String,String> variableOverrides = new HashMap<>();

		public TemplateBuilder(EAssetGenPass pass, Identifier templateId){
			this.pass = pass;
			this.templateId = templateId;
		}

		@Override
		public DataResult<IAssetGenerator> get() {
			Substitution template = TemplateRepository.Get(templateId);
			if (template == null)
				return DataResult.error(()->"Missing template: " + templateId);
			else
				return DataResult.success(new TemplatedAssetGenerator(pass, template, acceptedVariants, variableOverrides));
		}

		static public TemplateBuilder ItemStates(String templatePath){
			return new TemplateBuilder(EAssetGenPass.ITEM_STATES, Identifier.ofVanilla(templatePath));
		}

		static public TemplateBuilder ModelParent(Identifier parent){
			return ModelParent(parent.toString());
		}
		static public TemplateBuilder ModelParent(String parent){
			return new TemplateBuilder(EAssetGenPass.BAKED_MODELS, Identifier.ofVanilla("models/model_parent"))
				.AddVariables(Map.of("modelParent", parent))
				;
		}

		public TemplateBuilder AddVariables(Map<String,String> variables){
			this.variableOverrides.putAll(variables);
			return this;
		}

		public TemplateBuilder ExcludeRegex(Pattern regex){
			this.acceptedVariants = id->!regex.matcher(id.getPath()).matches();
			return this;
		}

		public TemplateBuilder IncludeSuffix(String suffix){
			this.acceptedVariants = id->id.getPath().endsWith(suffix);
			return this;
		}
	}
}
