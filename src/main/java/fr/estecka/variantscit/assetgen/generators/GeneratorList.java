package fr.estecka.variantscit.assetgen.generators;

import java.util.List;
import java.util.Objects;
import com.mojang.serialization.Codec;
import fr.estecka.variantscit.assetgen.EAssetGenPass;
import fr.estecka.variantscit.util.CodecUtil;
import net.minecraft.resources.Identifier;

public record GeneratorList(IAssetGenerator[] subGenerators)
implements IAssetGenerator
{
	@Override
	public Result AcceptAsset(EAssetGenPass pass, Identifier assetId) {
		Result result = new Result();
		for (IAssetGenerator generator : subGenerators)
			result.PutAllIfAbsent(generator.AcceptAsset(pass, assetId));
		return result;
	}

	static public <T extends IAssetGenerator> Codec<IAssetGenerator> CodecOf(Codec<T> inner){
		Objects.requireNonNull(inner);
		return CodecUtil.OneOrMany(inner).xmap(GeneratorList::Wrap, CodecUtil.NoGetter("Generator List"));
	}

	static public GeneratorList Of(IAssetGenerator... subGenerators){
		return new GeneratorList(subGenerators);
	}

	static public <T extends IAssetGenerator> IAssetGenerator Wrap(List<T> list){
		if (list.size() == 0)
			return IAssetGenerator.NOOP;
		if (list.size() == 1)
			return list.get(0);
		else
			return new GeneratorList(list.toArray(IAssetGenerator[]::new));
	}

	static public List<IAssetGenerator> Unwrap(IAssetGenerator generator){
		if (generator instanceof GeneratorList list)
			return List.of(list.subGenerators);
		else
			return List.of(generator);
	}
}
