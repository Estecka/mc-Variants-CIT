package fr.estecka.variantscit.assetgen.generators;

import java.io.InputStream;
import java.util.HashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.IoSupplier;
import com.mojang.serialization.Codec;
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
}
