package fr.estecka.variantscit.format;

import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import net.minecraft.nbt.NbtElement;

public class NbtAdapter
{
	static public final MapCodec<NbtAdapter> MAPCODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			NbtPath.CODEC.optionalFieldOf("nbtPath", NbtPath.IDENTITY).forGetter(adp -> adp.nbtPath),
			INbtInput.GROUP_CODEC.optionalFieldOf("expect", INbtInput.AUTO).forGetter(adp -> adp.type)
		)
		.apply(builder, NbtAdapter::new)
	);

	static public final Codec<NbtAdapter> CODEC = Codec.withAlternative(
		MAPCODEC.codec(),
		NbtPath.CODEC.xmap(path->new NbtAdapter(path, INbtInput.AUTO), adp->adp.nbtPath)
	);

	@Deprecated
	static public final MapCodec<NbtAdapter> LEGACY_MAPCODEC = CodecUtil.MapWithAlternatives(
		NbtPath.CODEC.fieldOf("nbtPath"),
		NbtPath.DOT_SEPARATED_CODEC.fieldOf("nbtPath"),
		NbtPath.NBTKEY_CODEC.fieldOf("nbtKey")
	).xmap((path)->new NbtAdapter(path,INbtInput.AUTO), (adp)->adp.nbtPath);


	private final NbtPath nbtPath;
	private final INbtInput type;

	public NbtAdapter(NbtPath nbtPath, INbtInput type){
		this.nbtPath = nbtPath;
		this.type = type;
	}

	public final @Nullable String ResolveData(NbtElement nbt){
		nbt = this.nbtPath.Resolve(nbt);
		if (nbt == null)
			return null;

		String data = this.type.apply(nbt);
		if (data == null)
			return null;

		return data;
	}
}
