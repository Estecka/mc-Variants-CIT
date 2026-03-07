package fr.estecka.variantscit.assetgen;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.IoSupplier;
import com.mojang.serialization.Codec;
import fr.estecka.variantscit.CodecUtil;

public interface IAssetGenerator
{
	Result AcceptAsset(EAssetGenPass pass, ResourceLocation assetId);

	// public default Identifier GetRadical(Identifier assetId){
	// 	return assetId;
	// }

	static public final IAssetGenerator NOOP = (_0,_1)->new Result();
	static public final Codec<IAssetGenerator> CODEC = CodecUtil.OneOrMany(CodecUtil.WithAlternative(
		GeneratorPresets.PRESET_CODEC,
		TemplatedAssetGenerator.MAPCODEC.codec()
	)).xmap(ListGenerator::Wrap, ListGenerator::Unwrap);

	static public record ListGenerator(IAssetGenerator[] subGenerators)
	implements IAssetGenerator
	{
		@Override
		public Result AcceptAsset(EAssetGenPass pass, ResourceLocation assetId) {
			Result result = new Result();
			for (IAssetGenerator generator : subGenerators)
				result.PutAllIfAbsent(generator.AcceptAsset(pass, assetId));
			return result;
		}

		static public ListGenerator Of(IAssetGenerator... subGenerators){
			return new ListGenerator(subGenerators);
		}

		static public <T extends IAssetGenerator> IAssetGenerator Wrap(List<T> list){
			if (list.size() == 0)
				return IAssetGenerator.NOOP;
			if (list.size() == 1)
				return list.get(0);
			else
				return new ListGenerator(list.toArray(IAssetGenerator[]::new));
		}

		static public List<IAssetGenerator> Unwrap(IAssetGenerator generator){
			if (generator instanceof ListGenerator list)
				return List.of(list.subGenerators);
			else
				return List.of(generator);
		}
	}

	static public record ParentedResource(
		ResourceLocation radical,
		IoSupplier<InputStream> resource
	) {}

	static public class Result
	extends HashMap<ResourceLocation,ParentedResource>
	{
		public void PutAllIfAbsent(Result behind){
			for (var entry : behind.entrySet())
				this.putIfAbsent(entry.getKey(), entry.getValue());
		}
	}
}
