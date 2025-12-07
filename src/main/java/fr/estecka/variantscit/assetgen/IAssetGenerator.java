package fr.estecka.variantscit.assetgen;

import java.io.InputStream;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import fr.estecka.variantscit.CodecUtil;
import net.minecraft.resource.InputSupplier;
import net.minecraft.util.Identifier;

public interface IAssetGenerator
{
	@Nullable InputSupplier<InputStream> AcceptAsset(EAssetGenPass pass, Identifier assetId);

	static public final IAssetGenerator NOOP = (_0,_1)->null;
	static public final Codec<IAssetGenerator> CODEC = CodecUtil.OneOrMany(Codec.withAlternative(
		GeneratorPresets.PRESET_CODEC,
		TemplatedAssetGenerator.MAPCODEC.codec()
	)).xmap(IAssetGenerator::OfList, _0->null);

	static public IAssetGenerator OfList(final IAssetGenerator... generators){
		return OfList(List.of(generators));
	}

	static public IAssetGenerator OfList(final List<IAssetGenerator> generators){
		return (pass, assetId)->{
			for (IAssetGenerator generator : generators){
				InputSupplier<InputStream> result = generator.AcceptAsset(pass, assetId);
				if (result != null)
					return result;
			}
			return null;
		};
	}
}
