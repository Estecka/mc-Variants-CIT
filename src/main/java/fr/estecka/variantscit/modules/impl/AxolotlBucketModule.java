package fr.estecka.variantscit.modules.impl;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.modules.libraries.VariantLibrary;
import fr.estecka.variantscit.reload.IUnbakedModule;
import fr.estecka.variantscit.modules.libraries.GenericBakedModule;
import fr.estecka.variantscit.modules.libraries.IVariantCitModule;
import fr.estecka.variantscit.format.Substitution;
import fr.estecka.variantscit.itemdata.extractors.IDataExtractor;
import fr.estecka.variantscit.itemdata.extractors.impl.AxolotlVariantProperty;
import fr.estecka.variantscit.itemdata.extractors.impl.EntityAgeMapProperty;
import fr.estecka.variantscit.modules.IBakedModule;

public record AxolotlBucketModule(String adultSuffix, String babySuffix)
implements IUnbakedModule
{
	static public final MapCodec<AxolotlBucketModule> UNBAKED_MAPCODEC = RecordCodecBuilder.mapCodec(builder->
		builder.group(
			CodecUtil.IDENTIFIER_PATH.optionalFieldOf("adultSuffix", "").forGetter(AxolotlBucketModule::adultSuffix),
			CodecUtil.IDENTIFIER_PATH.optionalFieldOf("babySuffix", "_baby").forGetter(AxolotlBucketModule::babySuffix)
		)
		.apply(builder, AxolotlBucketModule::new)
	);

	static private final Substitution agedFormat = Substitution.Parse("${variant}${age}").getOrThrow();
	static private final Map<String,IDataExtractor> ageInvariantVariables = Map.of("variant", AxolotlVariantProperty.UNIT);


	@Override
	public boolean AcceptsVariant(ResourceLocation variantId) {
		return variantId.getPath().endsWith(adultSuffix)
			|| variantId.getPath().endsWith(babySuffix)
			;
	};

	@Override
	public IBakedModule Bake(VariantLibrary library) {
		return new GenericBakedModule<>(
			library,
			AxolotlBucketModule.Create(adultSuffix, babySuffix)
		);
	}

	static private IVariantCitModule Create(String adult, String baby){
		IVariantCitModule result = CreateAgeInvariantModule(adult);

		if (!adult.equals(baby))
			result = new FallbackModule(CreateAgedModule(adult, baby), result);

		return result;
	}

	static private MultiComponentFormatModule CreateAgedModule(String adult, String baby){
		var variables = new HashMap<String, IDataExtractor>();
		variables.put("variant", AxolotlVariantProperty.UNIT);
		variables.put("age", new EntityAgeMapProperty(adult, baby));

		return new MultiComponentFormatModule(agedFormat, variables);
	}

	static private MultiComponentFormatModule CreateAgeInvariantModule(String suffix){
		return new MultiComponentFormatModule(Substitution.Parse("${variant}"+suffix).getOrThrow(), ageInvariantVariables);
	}
}
