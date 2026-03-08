package fr.estecka.variantscit.modules.impl;

import java.util.HashMap;
import net.minecraft.resources.ResourceLocation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.modules.libraries.VariantLibrary;
import fr.estecka.variantscit.modules.libraries.IVariantCitModule;
import fr.estecka.variantscit.format.Substitution;
import fr.estecka.variantscit.itemdata.extractors.IDataExtractor;
import fr.estecka.variantscit.itemdata.extractors.impl.AxolotlVariantProperty;
import fr.estecka.variantscit.itemdata.extractors.impl.EntityAgeMapProperty;
import fr.estecka.variantscit.modules.IBakedModule;
import fr.estecka.variantscit.modules.IModuleBaker;

public final class AxolotlBucketModule
{
	static private record Params(IVariantCitModule module, String adult, String baby) {}

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

	static public final MapCodec<Params> CODEC = RecordCodecBuilder.mapCodec(builder->
		builder.group(
			CodecUtil.IDENTIFIER_PATH.optionalFieldOf("adultSuffix", "").forGetter(CodecUtil.NoGetter("AxolotlBucketModule")),
			CodecUtil.IDENTIFIER_PATH.optionalFieldOf("babySuffix", "_baby").forGetter(CodecUtil.NoGetter("AxolotlBucketModule"))
		)
		.apply(builder, (adult,baby) -> new Params(
			AxolotlBucketModule.Create(adult,baby),
			adult,
			baby
		))
	);

	static private final Substitution agedFormat = Substitution.Parse("${variant}${age}").getOrThrow();

	static private final HashMap<String,IDataExtractor> ageInvariantVariables;
	static {
		ageInvariantVariables = new HashMap<>();
		ageInvariantVariables.put("variant", AxolotlVariantProperty.UNIT);
	}

	static public IVariantCitModule Create(String adult, String baby){
		IVariantCitModule result = CreateAgeInvariantModule(adult);

		if (!adult.equals(baby))
			result = new FallbackModule(CreateAgedModule(adult, baby), result);

		return result;
	}

	static public MultiComponentFormatModule CreateAgedModule(String adult, String baby){
		var variables = new HashMap<String, IDataExtractor>();
		variables.put("variant", AxolotlVariantProperty.UNIT);
		variables.put("age", new EntityAgeMapProperty(adult, baby));

		return new MultiComponentFormatModule(agedFormat, variables);
	}

	static public MultiComponentFormatModule CreateAgeInvariantModule(String suffix){
		return new MultiComponentFormatModule(Substitution.Parse("${variant}"+suffix).getOrThrow(), ageInvariantVariables);
	}
}
