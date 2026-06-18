package fr.estecka.variantscit.reload;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.VCitRegistries;
import fr.estecka.variantscit.assetgen.IAssetGenerator;
import fr.estecka.variantscit.itemdata.preconditions.IItemPrecondition;


public record ModuleDefinition(
	@Deprecated ResourceLocation type,
	IUnbakedModule parameters,
	List<EModuleHook> hooks,
	Optional<List<ResourceLocation>> targets,
	Optional<IItemPrecondition> precondition,
	int priority,
	LibraryDefinition libraryDefinition,
	Optional<ResourceLocation> modelParent,
	Optional<IAssetGenerator> assetGen
)
{
	static public final MapCodec<ModuleDefinition> CODEC = RecordCodecBuilder.<ModuleDefinition>mapCodec(builder->builder
		.group(
			ResourceLocation.CODEC.fieldOf("type").forGetter(ModuleDefinition::type),
			VCitRegistries.MODULES.mapCodec.forGetter(ModuleDefinition::parameters),
			CodecUtil.WithAlias(CodecUtil.OneOrMany(EModuleHook.CODEC), "hook", "context").orElse(List.of(EModuleHook.ITEM_MODEL)).forGetter(ModuleDefinition::hooks),
			CodecUtil.OneOrMany(ResourceLocation.CODEC).optionalFieldOf("items").forGetter(ModuleDefinition::targets),
			IItemPrecondition.CODEC.optionalFieldOf("precondition").forGetter(ModuleDefinition::precondition),
			Codec.INT.fieldOf("priority").orElse(0).forGetter(ModuleDefinition::priority),
			LibraryDefinition.MAP_CODEC.forGetter(ModuleDefinition::libraryDefinition),
			ResourceLocation.CODEC.optionalFieldOf("modelParent").forGetter(ModuleDefinition::modelParent),
			IAssetGenerator.CODEC.optionalFieldOf("assetGen").forGetter(ModuleDefinition::assetGen)
		)
		.apply(builder, ModuleDefinition::new)
	);
}
