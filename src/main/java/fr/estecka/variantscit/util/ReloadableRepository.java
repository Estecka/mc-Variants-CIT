package fr.estecka.variantscit.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import fr.estecka.variantscit.util.CodecUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

public class ReloadableRepository<T>
{
	private final Map<Identifier, T> entries = new HashMap<>();
	public final Codec<T> UNIT_CODEC = CodecUtil.Enum(CodecUtil.VCIT_IDENTIFIER, entries);

	private final Codec<T> resourceCodec;
	private final String directory;
	private final String suffix;

	/**
	 * @param codec The codec used to parse resources.
	 * @param directory The directory containing the resources, EXCLUDING the trailing slash.
	 * @param extension The file extension, EXCLUDING the dot.
	 */
	public ReloadableRepository(Codec<T> codec, String directory, String extension){
		this.resourceCodec = codec;
		this.directory = directory;
		this.suffix = "."+extension;
	}

	public @Nullable T Get(Identifier id){
		return entries.get(id);
	}

	public Optional<T> GetOptional(Identifier id){
		return Optional.ofNullable(entries.get(id));
	}

	public void Reload(ResourceManager manager)
	{
		entries.clear();
		entries.putAll(CodecUtil.ReloadResources(manager, resourceCodec, directory, suffix));
	}
}
