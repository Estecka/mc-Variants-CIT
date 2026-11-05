package fr.estecka.variantscit.reload;

import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.util.CodecUtil;
import fr.estecka.variantscit.VCitRegistries;
import fr.estecka.variantscit.assetgen.generators.IAssetGenerator;
import fr.estecka.variantscit.itemdata.preconditions.IItemPrecondition;


public record ModuleDefinition(
	@Deprecated Identifier type,
	IUnbakedModule parameters,
	List<EModuleHook> hooks,
	Optional<List<Identifier>> targets,
	Optional<IItemPrecondition> precondition,
	int priority,
	LibraryDefinition libraryDefinition,
	Optional<IAssetGenerator> assetGen
)
{
	static public final MapCodec<Optional<IAssetGenerator>> ASSETGEN_MAPCODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			IAssetGenerator.MODELPARENT_CODEC.optionalFieldOf("modelParent").forGetter(_0->Optional.empty()),
			IAssetGenerator.LIST_CODEC.optionalFieldOf("assetGen").forGetter(o->o)
		)
		.apply(builder, (assetGen,modelParent)->assetGen.isPresent() ? assetGen : modelParent)
	);

	static public final MapCodec<ModuleDefinition> CODEC = RecordCodecBuilder.<ModuleDefinition>mapCodec(builder->builder
		.group(
			Identifier.CODEC.fieldOf("type").forGetter(ModuleDefinition::type),
			VCitRegistries.MODULES.mapCodec.forGetter(ModuleDefinition::parameters),
			CodecUtil.WithDeprecatedAlias(CodecUtil.OneOrMany(EModuleHook.CODEC), "hook", "context").orElse(List.of(EModuleHook.ITEM_MODEL)).forGetter(ModuleDefinition::hooks),
			CodecUtil.OptionalWithAlias(CodecUtil.OneOrMany(Identifier.CODEC), "items", "item").forGetter(ModuleDefinition::targets),
			IItemPrecondition.CODEC.optionalFieldOf("precondition").forGetter(ModuleDefinition::precondition),
			Codec.INT.fieldOf("priority").orElse(0).forGetter(ModuleDefinition::priority),
			LibraryDefinition.MAP_CODEC.forGetter(ModuleDefinition::libraryDefinition),
			ASSETGEN_MAPCODEC.forGetter(ModuleDefinition::assetGen)
		)
		.apply(builder, ModuleDefinition::new)
	);
}
