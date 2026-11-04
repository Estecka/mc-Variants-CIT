package fr.estecka.variantscit.assetgen.generators;

import java.util.Map.Entry;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.assetgen.EAssetGenPass;
import fr.estecka.variantscit.util.CodecUtil;
import fr.estecka.variantscit.util.identifier.IdentifierFilter;
import net.minecraft.resources.Identifier;

public record FilteredGenerator(
	IdentifierFilter filter,
	IAssetGenerator inner
)
implements IAssetGenerator
{
	static public <T extends IAssetGenerator> MapCodec<IAssetGenerator> CodecOf(MapCodec<T> inner){
		return RecordCodecBuilder.<IAssetGenerator>mapCodec(builder->
			builder.group(
				IdentifierFilter.MAPCODEC.codec().optionalFieldOf("filter", IdentifierFilter.NOOP).forGetter(CodecUtil.NoGetter("filter")),
				CodecUtil.<IAssetGenerator,T>AnonymizeMap(inner).forGetter(CodecUtil.NoGetter("inner"))
			)
			.apply(builder, FilteredGenerator::Wrap)
		);
	}

	static public IAssetGenerator Wrap(IdentifierFilter filter, IAssetGenerator inner){
		if (filter == IdentifierFilter.NOOP)
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
			if (!filter.test(entry.getValue().radical()))
				iterator.remove();
		}

		return result;
	}
}
