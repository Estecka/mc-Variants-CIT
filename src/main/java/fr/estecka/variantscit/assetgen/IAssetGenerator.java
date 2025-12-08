package fr.estecka.variantscit.assetgen;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import com.mojang.serialization.Codec;
import fr.estecka.variantscit.CodecUtil;
import net.minecraft.resource.InputSupplier;
import net.minecraft.util.Identifier;

public interface IAssetGenerator
{
	Result AcceptAsset(EAssetGenPass pass, Identifier assetId);

	// public default Identifier GetRadical(Identifier assetId){
	// 	return assetId;
	// }

	static public final IAssetGenerator NOOP = (_0,_1)->new Result();
	static public final Codec<IAssetGenerator> CODEC = CodecUtil.OneOrMany(Codec.withAlternative(
		GeneratorPresets.PRESET_CODEC,
		TemplatedAssetGenerator.MAPCODEC.codec()
	)).xmap(IAssetGenerator::OfList, _0->null);

	static public IAssetGenerator OfList(final IAssetGenerator... generators){
		return OfList(List.of(generators));
	}

	static public IAssetGenerator OfList(final List<IAssetGenerator> generators){
		return (pass, assetId)->{
			Result result = new Result();
			for (IAssetGenerator generator : generators)
				result.PutAllIfAbsent(generator.AcceptAsset(pass, assetId));
			return result;
		};
	}

	static public class Result
	extends HashMap<Identifier,InputSupplier<InputStream>>
	{
		public void PutAllIfAbsent(Result behind){
			for (var entry : behind.entrySet())
				this.putIfAbsent(entry.getKey(), entry.getValue());
		}
	}
}
