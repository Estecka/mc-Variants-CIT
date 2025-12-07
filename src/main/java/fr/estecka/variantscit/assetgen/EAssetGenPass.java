package fr.estecka.variantscit.assetgen;

import com.mojang.serialization.Codec;
import fr.estecka.variantscit.reload.EAssetType;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

public enum EAssetGenPass
implements StringIdentifiable
{
	BAKED_MODELS(1, "models", EAssetType.TEXTURE,     EAssetType.BAKED_MODEL),
	ITEM_STATES (2, "items" , EAssetType.BAKED_MODEL, EAssetType.ITEM_STATE ),
	;

	static public final Codec<EAssetGenPass> CODEC = StringIdentifiable.createCodec(EAssetGenPass::values);

	public final int order;
	public final String name;
	public final EAssetType input, output;

	private EAssetGenPass(int order, String name, EAssetType input, EAssetType output){
		this.order = order;
		this.name = name;
		this.input = input;
		this.output = output;
	}

	public Identifier GetOutputResourceId(Identifier assetId){
		return assetId.withPath(path -> output.directory+"/"+path+output.suffix);
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
