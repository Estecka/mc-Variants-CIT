package fr.estecka.variantscit.assetgen;

import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import com.mojang.serialization.Codec;
import fr.estecka.variantscit.reload.EAssetType;

public enum EAssetGenPass
implements StringRepresentable
{
	TRIMS       ("trims_from_textures",      EAssetType.TRIM_TEXTURE,  EAssetType.TRIM_MODEL ),
	EQUIPMENTS  ("equipments_from_textures", EAssetType.EQUIP_TEXTURE, EAssetType.EQUIPMENT  ),
	BAKED_MODELS("models_from_textures",     EAssetType.ITEM_TEXTURE,  EAssetType.BAKED_MODEL),
	ITEM_STATES ("items_from_models",        EAssetType.BAKED_MODEL,   EAssetType.ITEM_STATE ),
	;

	static public final Codec<EAssetGenPass> CODEC = StringRepresentable.fromEnum(EAssetGenPass::values);

	public final String name;
	public final EAssetType input, output;

	private EAssetGenPass(String name, EAssetType input, EAssetType output){
		this.name = name;
		this.input = input;
		this.output = output;
	}

	public Optional<ResourceLocation> GetShortInputId(ResourceLocation resourceId){
		String path = resourceId.getPath();
		if (!path.startsWith(input.packDirectory+"/") || !path.endsWith(input.suffix))
			return Optional.empty();

		return Optional.of(resourceId.withPath(
			p->p.substring(0, 0)
		));
	}

	public ResourceLocation GetOutputResourceId(ResourceLocation shortAssetId){
		return shortAssetId.withPath(path -> output.packDirectory+"/"+path+output.suffix);
	}

	@Override
	public String getSerializedName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
}
