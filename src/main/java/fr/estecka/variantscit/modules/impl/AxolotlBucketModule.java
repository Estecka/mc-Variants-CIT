package fr.estecka.variantscit.modules.impl;

import java.util.HashMap;
import net.minecraft.resources.ResourceLocation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.modules.libraries.VariantLibrary;
import fr.estecka.variantscit.api.ICitModule;
import fr.estecka.variantscit.format.Substitution;
import fr.estecka.variantscit.format.properties.AxolotlVariantProperty;
import fr.estecka.variantscit.format.properties.EntityAgeMapProperty;
import fr.estecka.variantscit.format.properties.IStringProperty;
import fr.estecka.variantscit.modules.IBakedModule;
import fr.estecka.variantscit.modules.IModuleBaker;

public final class AxolotlBucketModule
{
	static private record Params(ICitModule module, String adult, String baby) {}

	static public final IModuleBaker<Params> BAKER = new IModuleBaker<>() {
		@Override
		public IBakedModule Bake(VariantLibrary library, Params parameters) {
			return library.Bake(parameters.module);
		};
		@Override
		public boolean AcceptVariant(ResourceLocation variantId, Params parameters) {
			return variantId.getPath().endsWith(parameters.adult)
			    || variantId.getPath().endsWith(parameters.baby)
			    ;
		};
	};

	// TODO: Proper getters
	static public final MapCodec<Params> CODEC = RecordCodecBuilder.mapCodec(builder->
		builder.group(
			Codec.BOOL.fieldOf("debug").orElse(false).forGetter(o->false),
			CodecUtil.IDENTIFIER_PATH.optionalFieldOf("adultSuffix", "").forGetter(o->""),
			CodecUtil.IDENTIFIER_PATH.optionalFieldOf("babySuffix", "_baby").forGetter(o->"")
		)
		.apply(builder, (debug,adult,baby) -> new Params(
			AxolotlBucketModule.Create(debug,adult,baby),
			adult,
			baby
		))
	);

	static private final Substitution agedFormat = Substitution.Parse("${variant}${age}").getOrThrow();

	static private final HashMap<String,IStringProperty> ageInvariantVariables;
	static {
		ageInvariantVariables = new HashMap<>();
		ageInvariantVariables.put("variant", AxolotlVariantProperty.UNIT);
	}

	static public ICitModule Create(boolean debug, String adult, String baby){
		ICitModule result = CreateAgeInvariantModule(debug, adult);

		if (!adult.equals(baby))
			result = new FallbackModule(CreateAgedModule(debug, adult, baby), result);

		return result;
	}

	static public MultiComponentFormatModule CreateAgedModule(boolean debug, String adult, String baby){
		var variables = new HashMap<String, IStringProperty>();
		variables.put("variant", AxolotlVariantProperty.UNIT);
		variables.put("age", new EntityAgeMapProperty(adult, baby));

		return new MultiComponentFormatModule(debug, agedFormat, variables);
	}

	static public MultiComponentFormatModule CreateAgeInvariantModule(boolean debug, String suffix){
		return new MultiComponentFormatModule(debug, Substitution.Parse("${variant}"+suffix).getOrThrow(), ageInvariantVariables);
	}
}
