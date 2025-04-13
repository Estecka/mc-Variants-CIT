package fr.estecka.variantscit.format;

import java.text.Normalizer;
import java.util.List;
import java.util.function.Function;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.VCitRegistries;
import fr.estecka.variantscit.VariantsCitMod;

@FunctionalInterface
public interface IStringTransform
extends Function<String,String>
{
	static public final IStringTransform[] EMPTY = new IStringTransform[0];

	static public final Codec<IStringTransform> CODEC = VCitRegistries.TRANSFORMS.codec;
	static public final Codec<IStringTransform[]> ARRAY_CODEC = CodecUtil.OneOrMany(CODEC).xmap(list->list.toArray(IStringTransform[]::new), array->List.<IStringTransform>of(array));
	@Deprecated
	static public final Codec<IStringTransform[]> LEGACY_CODEC = Codec.BOOL.xmap(
		lowercase -> lowercase ? new IStringTransform[]{String::toLowerCase} : IStringTransform.EMPTY,
		transform -> true
	).validate(_0 -> {
		VariantsCitMod.LOGGER.warn("The parameter `caseSensitive:true` is being deprecated. Use `transform:lowercase` instead.");
		return DataResult.success(_0);
	});

	static public String Transform(IStringTransform[] transforms, String input){
		for (IStringTransform t : transforms) {
			input = t.apply(input);
			if (input == null)
				break;
		}
		return input;
	}

	static public IStringTransform Sanitize(String charset){
		return input -> Normalizer.normalize(input, Normalizer.Form.NFD)
			.replace(' ', '_')
			.toLowerCase()
			.replaceAll(charset, "")
			;
	}

	static public String DiscardNamespace(String s){
		int split = s.lastIndexOf(':');
		return (split < 0) ? s : s.substring(split+1);
	}

	static public String DiscardPath(String s) {
		int split = s.lastIndexOf(':');
		return (split < 0) ? "" : s.substring(0, split);
	}

}
