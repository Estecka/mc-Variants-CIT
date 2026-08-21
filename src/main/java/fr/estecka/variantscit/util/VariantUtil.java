package fr.estecka.variantscit.util;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import fr.estecka.variantscit.VariantsCitMod;
import net.minecraft.resources.Identifier;
import static fr.estecka.variantscit.VariantsCitMod.MODID;

public final class VariantUtil
{
	static public final Identifier FALLBACK_VARIANT_ID = VariantsCitMod.Identifier("fallback");
	static public final Pattern INTRINSIC_PATTERN = Pattern.compile(MODID+":intrinsic/(?<namespace>.+?)/(?<path>.+)");
	static public final Pattern SPECIAL_PATTERN   = Pattern.compile(MODID+":special/(?<name>.+)");

	static public Identifier SpecialVariantId(String specialName){
		return VariantsCitMod.Identifier("special/"+specialName);
	}

	static public Identifier IntrinsicVariantId(Identifier modelId){
		return VariantsCitMod.Identifier("intrinsic/"+modelId.getNamespace()+"/"+modelId.getPath());
	}

	static public boolean IsVariantIntrinsic(Identifier variantId){
		return variantId.getNamespace().equals(MODID)
		    && variantId.getPath().startsWith("intrinsic/")
		    ;
	}

	static public Optional<Identifier> GetIntrinsicModelId(Identifier variantId){
		Identifier result = null;
		Matcher match = INTRINSIC_PATTERN.matcher(variantId.toString());

		if (match.matches())
			result = Identifier.fromNamespaceAndPath(match.group("namespace"), match.group("path"));

		return Optional.ofNullable(result);
	}
}
