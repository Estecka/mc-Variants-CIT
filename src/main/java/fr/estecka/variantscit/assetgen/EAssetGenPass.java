package fr.estecka.variantscit.assetgen;

import java.util.Optional;
import com.mojang.serialization.Codec;
import fr.estecka.variantscit.reload.EAssetType;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

public enum EAssetGenPass
implements StringIdentifiable
{
	EQUIPMENTS  ("equipments_from_textures", EAssetType.EQUIP_TEXTURE, EAssetType.EQUIPMENT  ),
	BAKED_MODELS("models_from_textures",     EAssetType.ITEM_TEXTURE,  EAssetType.BAKED_MODEL),
	ITEM_STATES ("items_from_models",        EAssetType.BAKED_MODEL,   EAssetType.ITEM_STATE ),
	;

	static public final Codec<EAssetGenPass> CODEC = StringIdentifiable.createCodec(EAssetGenPass::values);

	public final String name;
	public final EAssetType input, output;

	private EAssetGenPass(String name, EAssetType input, EAssetType output){
		this.name = name;
		this.input = input;
		this.output = output;
	}

	public Optional<Identifier> GetShortInputId(Identifier resourceId){
		String path = resourceId.getPath();
		if (!path.startsWith(input.directory+"/") || !path.endsWith(input.suffix))
			return Optional.empty();

		return Optional.of(resourceId.withPath(
			p->p.substring(0, 0)
		));
	}

	public Identifier GetOutputResourceId(Identifier shortAssetId){
		return shortAssetId.withPath(path -> output.directory+"/"+path+output.suffix);
	}

	@Override
	public String asString() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
}
