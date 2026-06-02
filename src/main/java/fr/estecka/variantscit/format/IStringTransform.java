package fr.estecka.variantscit.format;

import java.text.Normalizer;
import java.util.function.Function;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import fr.estecka.variantscit.VCitRegistries;
import fr.estecka.variantscit.VariantsCitMod;
import net.minecraft.util.Identifier;

@FunctionalInterface
public interface IStringTransform
extends Function<String,String>
{
	static public final IStringTransform NOOP               = o->o;
	static public final IStringTransform NULL               = o->null;
	static public final IStringTransform SANITIZE_AUTO      = IStringTransform::AutoSanitize;
	static public final IStringTransform SANITIZE_PATH      = Sanitize("[^a-zA-Z0-9_./-]");
	static public final IStringTransform SANITIZE_NAMESPACE = Sanitize("[^a-zA-Z0-9_.-]");
	static public final IStringTransform SANITIZE_LEGACY    = Sanitize("[^a-zA-Z0-9_./:-]");
	static public final IStringTransform LOWERCASE          = String::toLowerCase;

	static public final Codec<IStringTransform> CODEC = VCitRegistries.TRANSFORMS.codec;
	@Deprecated
	static public final Codec<IStringTransform> LEGACY_CODEC = Codec.BOOL.xmap(
		lowercase -> lowercase ? IStringTransform.LOWERCASE : IStringTransform.NOOP,
		transform -> true
	).validate(_0 -> {
		VariantsCitMod.LOGGER.warn("The parameter `caseSensitive:true` is being deprecated. Use `transform:lowercase` instead.");
		return DataResult.success(_0);
	});

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

	static public String AutoSanitize(String input){
		input = SANITIZE_LEGACY.apply(input);

		if (Identifier.tryParse(input) != null)
			return input;
		else
			return SANITIZE_PATH.apply(input);
	}
}
