package fr.estecka.variantscit.assetgen.generators;

import java.io.InputStream;
import java.util.HashMap;
import java.util.regex.Pattern;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.IoSupplier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.assetgen.EAssetGenPass;
import fr.estecka.variantscit.assetgen.FilledTemplate;
import fr.estecka.variantscit.util.CodecUtil;
import fr.estecka.variantscit.util.ReloadableRepository;


public interface IAssetGenerator
{
	Result AcceptAsset(EAssetGenPass pass, Identifier assetId);

	static public final IAssetGenerator NOOP = (_0,_1)->new Result();

	static public record ParentedResource(
		Identifier radical,
		IoSupplier<InputStream> resource
	) {}

	static public class Result
	extends HashMap<Identifier,ParentedResource>
	{
		public void PutAllIfAbsent(Result behind){
			for (var entry : behind.entrySet())
				this.putIfAbsent(entry.getKey(), entry.getValue());
		}
	}


/******************************************************************************/
/* # CODECS                                                                   */
/******************************************************************************/

	/***********************/
	/* ## Model-Parent     */
	/***********************/

	static public final Codec<IAssetGenerator> MODELPARENT_CODEC = Identifier.CODEC.comapFlatMap(IAssetGenerator::ModelParent, CodecUtil.NoGetter("ModelPrent generator"));
	static public final MapCodec<IAssetGenerator> MODELPARENT_MAPCODEC = MODELPARENT_CODEC.fieldOf("modelParent");

	static public DataResult<IAssetGenerator> ModelParent(Identifier modelParent){
		Pattern acceptAll = Pattern.compile(".*");
		IAssetGenerator items;
		IAssetGenerator models;

		var itemsTemplate = FilledTemplate.Stateless();
		if (itemsTemplate.isError())
			return DataResult.error(() -> "Missing built-in template: \n"+itemsTemplate.error().get().message());

		items  = new TemplatedAssetGenerator(EAssetGenPass.ITEM_STATES , acceptAll, itemsTemplate.getOrThrow());

		var modelTemplate = FilledTemplate.ModelParent(modelParent.toString());
		if (modelTemplate.isError()){
			return DataResult.error(()->"Missing built-in template: \n"+modelTemplate.error().get().message());
		}
		models = new TemplatedAssetGenerator(EAssetGenPass.BAKED_MODELS, acceptAll, modelTemplate.getOrThrow());

		return DataResult.success(GeneratorList.Of(items, models));
	}

	/***********************/
	/* ## Preset-Disabled  */
	/***********************/

	static public final MapCodec<IAssetGenerator> SINGLE_MAPCODEC_NOPRESET = FilteredGenerator.CodecOf(CodecUtil.MapWithAlternatives(
		TemplatedAssetGenerator.MAPCODEC,
		MODELPARENT_MAPCODEC,
		LazyListMapcodecNoPreset()
	));

	static public Codec<IAssetGenerator> LIST_CODEC_NOPRESET = GeneratorList.CodecOf(SINGLE_MAPCODEC_NOPRESET.codec());
	static private MapCodec<IAssetGenerator> LazyListMapcodecNoPreset(){
		return Codec.lazyInitialized(()->LIST_CODEC_NOPRESET).fieldOf("assetGen");
	}

	static public final ReloadableRepository<IAssetGenerator> PRESET_REPOSITORY = new ReloadableRepository<>(IAssetGenerator.LIST_CODEC_NOPRESET, "variants-cit/assetgen_presets", ".json");
	static public final Codec<IAssetGenerator> PRESET_CODEC = PRESET_REPOSITORY.UNIT_CODEC;
	static public final MapCodec<IAssetGenerator> PRESET_MAPCODEC = PRESET_CODEC.fieldOf("preset");


	/**********************/
	/* ## Preset-Enabled  */
	/**********************/

	static public final MapCodec<IAssetGenerator> SINGLE_MAPCODEC = FilteredGenerator.CodecOf(CodecUtil.MapWithAlternatives(
		TemplatedAssetGenerator.MAPCODEC,
		MODELPARENT_MAPCODEC,
		PRESET_MAPCODEC,
		LazyListMapcodec()
	));
	static public final Codec<IAssetGenerator> SINGLE_CODEC = CodecUtil.WithAlternative(
		PRESET_CODEC,
		SINGLE_MAPCODEC.codec()
	);
	
	static public Codec<IAssetGenerator> LIST_CODEC = GeneratorList.CodecOf(SINGLE_CODEC);
	static private MapCodec<IAssetGenerator> LazyListMapcodec(){
		return Codec.lazyInitialized(()->LIST_CODEC).fieldOf("assetGen");
	}

}
