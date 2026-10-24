package fr.estecka.variantscit.assetgen.generators;

import java.io.InputStream;
import java.util.HashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.IoSupplier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.assetgen.EAssetGenPass;
import fr.estecka.variantscit.assetgen.GeneratorPresets;
import fr.estecka.variantscit.util.CodecUtil;

public interface IAssetGenerator
{
	Result AcceptAsset(EAssetGenPass pass, Identifier assetId);

	static public final IAssetGenerator NOOP = (_0,_1)->new Result();
	static public final Codec<IAssetGenerator> CODEC = CodecUtil.OneOrMany(CodecUtil.WithAlternative(
		GeneratorPresets.PRESET_CODEC,
		TemplatedAssetGenerator.MAPCODEC.codec()
	)).xmap(GeneratorList::Wrap, GeneratorList::Unwrap);

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

	static public final MapCodec<IAssetGenerator> SINGLE_MAPCODEC_NOPRESET = FilteredGenerator.CodecOf(CodecUtil.MapWithAlternatives(
		TemplatedAssetGenerator.MAPCODEC,
		GeneratorPresets.MODELPARENT_MAPCODEC,
		LazyListMapcodecNoPreset()
	));

	static public final MapCodec<IAssetGenerator> SINGLE_MAPCODEC = FilteredGenerator.CodecOf(CodecUtil.MapWithAlternatives(
		TemplatedAssetGenerator.MAPCODEC,
		GeneratorPresets.MODELPARENT_MAPCODEC,
		GeneratorPresets.PRESET_MAPCODEC,
		LazyListMapcodec()
	));

	static public final Codec<IAssetGenerator> SINGLE_CODEC = CodecUtil.WithAlternative(
		GeneratorPresets.PRESET_CODEC,
		SINGLE_MAPCODEC.codec()
	);

	static public Codec<IAssetGenerator> LIST_CODEC_NOPRESET = GeneratorList.CodecOf(SINGLE_MAPCODEC_NOPRESET.codec());
	static public Codec<IAssetGenerator> LIST_CODEC = GeneratorList.CodecOf(SINGLE_CODEC);

	static private MapCodec<IAssetGenerator> LazyListMapcodecNoPreset(){
		return Codec.lazyInitialized(()->LIST_CODEC_NOPRESET).fieldOf("assetGen");
	}
	static private MapCodec<IAssetGenerator> LazyListMapcodec(){
		return Codec.lazyInitialized(()->LIST_CODEC).fieldOf("assetGen");
	}
}
