package fr.estecka.variantscit.util.identifier;

import java.util.function.Predicate;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.itemdata.transforms.IDataTransform;
import fr.estecka.variantscit.itemdata.transforms.SuccessiveTransform;
import fr.estecka.variantscit.itemdata.transforms.impl.RegexTransform;
import fr.estecka.variantscit.itemdata.transforms.impl.StringCompareTransform;
import fr.estecka.variantscit.reload.LibraryDefinition;
import fr.estecka.variantscit.util.CodecUtil;
import net.minecraft.resources.Identifier;

/**
 * TODO: Reduce code duplication with {@linkplain LibraryDefinition}; the fields
 * `modelPathes` and `modelNamespaces, could  be  refactored  to use  this class
 * instead.
 */
public record IdentifierFilter(
	IDataTransform namespaceFilter,
	IDataTransform pathFilter
)
implements Predicate<Identifier>
{
	static public final IdentifierFilter NOOP = new IdentifierFilter(IDataTransform.NOOP, IDataTransform.NOOP);

	@Override
	public boolean test(Identifier id) {
		return this.TestNamespace(id.getNamespace()) && this.TestPath(id.getPath());
	}

	public boolean TestNamespace(String namespace) {
		return IDataTransform.Test(this.namespaceFilter, namespace);
	}

	public boolean TestPath(String path) {
		return IDataTransform.Test(this.pathFilter, path);
	}


/******************************************************************************/
/* # Codecs                                                                   */
/******************************************************************************/

	static public final Codec<IDataTransform> NAMESPACE_FILTER_CODEC = CodecUtil.WithAlternatives(
		CodecUtil.IDENTIFIER_NAMESPACE.xmap(StringCompareTransform::new, StringCompareTransform::targetValue),
		RegexTransform.STRINGCODEC,
		SuccessiveTransform.CHAIN_CODEC
	);

	static public final Codec<IDataTransform> PATH_FILTER_CODEC = CodecUtil.WithAlternatives(
		CodecUtil.IDENTIFIER_PATH.xmap(StringCompareTransform::new, StringCompareTransform::targetValue),
		RegexTransform.STRINGCODEC,
		SuccessiveTransform.CHAIN_CODEC
	);

	static public final MapCodec<IdentifierFilter> MAPCODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			NAMESPACE_FILTER_CODEC.optionalFieldOf("namespace", IDataTransform.NOOP).forGetter(IdentifierFilter::namespaceFilter),
			PATH_FILTER_CODEC     .optionalFieldOf("path",      IDataTransform.NOOP).forGetter(IdentifierFilter::pathFilter)
		)
		.apply(builder, IdentifierFilter::new)
	);

}
