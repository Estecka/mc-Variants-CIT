package fr.estecka.variantscit;

import java.util.HashMap;
import java.util.Map;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public class ReloadableRepository<T>
{
	private final Map<ResourceLocation, T> entries = new HashMap<>();
	public final Codec<T> UNIT_CODEC = CodecUtil.Enum(CodecUtil.VCIT_IDENTIFIER, entries);

	private final Codec<T> resourceCodec;
	private final String directory;
	private final String suffix;

	/**
	 * @param codec The codec use to parse resources.
	 * @param directory The directory containing the resources, EXCLUDING the trailing slash.
	 * @param extension The file extension, EXCLUDING the dot.
	 */
	public ReloadableRepository(Codec<T> codec, String directory, String extension){
		this.resourceCodec = codec;
		this.directory = directory;
		this.suffix = "."+extension;
	}

	public DataResult<T> Get(ResourceLocation id){
		T result = entries.get(id);
		return (result != null) ?
			DataResult.success(result) :
			DataResult.error(()->"No such template: "+id.toString());
	}

	public void Reload(ResourceManager manager)
	{
		entries.clear();
		entries.putAll(CodecUtil.ReloadResources(manager, resourceCodec, directory, suffix));
	}
}
