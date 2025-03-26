package fr.estecka.variantscit.modules;

import java.util.HashMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.format.Substitution;
import fr.estecka.variantscit.format.properties.AxolotlVariantProperty;
import fr.estecka.variantscit.format.properties.EntityAgeMapProperty;
import fr.estecka.variantscit.format.properties.IStringProperty;

public final class AxolotlBucketModule
{
	// TODO: Proper getters
	static public final MapCodec<MultiComponentFormatModule> CODEC = RecordCodecBuilder.mapCodec(builder->
		builder.group(
			Codec.BOOL.fieldOf("debug").orElse(false).forGetter(o->o.debug),
			CodecUtil.IDENTIFIER_PATH.fieldOf("adultSuffix").orElse("").forGetter(o->""),
			CodecUtil.IDENTIFIER_PATH.fieldOf("babySuffix").orElse("").forGetter(o->"")
		)
		.apply(builder, AxolotlBucketModule::Create)
	);

	static private final Substitution format = Substitution.Parse("${variant}${age}").getOrThrow();

	static public MultiComponentFormatModule Create(boolean debug, String adult, String baby){
		var variables = new HashMap<String, IStringProperty>();
		variables.put("variant", AxolotlVariantProperty.UNIT);
		variables.put("age", new EntityAgeMapProperty(adult, baby));

		return new MultiComponentFormatModule(debug, format, variables);
	}
}
