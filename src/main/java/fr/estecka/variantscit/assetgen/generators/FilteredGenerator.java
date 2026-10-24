package fr.estecka.variantscit.assetgen.generators;

import java.util.Map.Entry;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.assetgen.EAssetGenPass;
import fr.estecka.variantscit.itemdata.transforms.IDataTransform;
import fr.estecka.variantscit.itemdata.transforms.SuccessiveTransform;
import fr.estecka.variantscit.util.CodecUtil;
import net.minecraft.resources.Identifier;

public record FilteredGenerator(
	IDataTransform filter,
	IAssetGenerator inner
)
implements IAssetGenerator
{
	static public <T extends IAssetGenerator> MapCodec<IAssetGenerator> CodecOf(MapCodec<T> inner){
		return RecordCodecBuilder.<IAssetGenerator>mapCodec(builder->
			builder.group(
				SuccessiveTransform.CHAIN_CODEC.optionalFieldOf("filter", IDataTransform.NOOP).forGetter(CodecUtil.NoGetter("filter")),
				CodecUtil.<IAssetGenerator,T>AnonymizeMap(inner).forGetter(CodecUtil.NoGetter("inner"))
			)
			.apply(builder, FilteredGenerator::Wrap)
		);
	}

	static public IAssetGenerator Wrap(IDataTransform filter, IAssetGenerator inner){
		if (filter == IDataTransform.NOOP)
			return inner;
		else
			return new FilteredGenerator(filter, inner);
	}

	@Override
	public Result AcceptAsset(EAssetGenPass pass, Identifier assetId) {
		Result result = this.inner.AcceptAsset(pass, assetId);

		var iterator = result.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Identifier,ParentedResource> entry = iterator.next();
			if (!IDataTransform.Test(filter, entry.getValue().radical()))
				iterator.remove();
		}

		return result;
	}
}
