package fr.estecka.variantscit.assetgen;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.BuiltInMetadata;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.Pack.Metadata;
import net.minecraft.server.packs.repository.Pack.Position;
import net.minecraft.server.packs.repository.Pack.ResourcesSupplier;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.flag.FeatureFlagSet;

public class GeneratedResourcePack
implements PackResources
{
	static public final GeneratedResourcePack INSTANCE = new GeneratedResourcePack();

	static public final Component PACK_TITLE = Component.literal("Variants-CIT Mod");
	static public final Component PACK_DESC  = Component.literal("Runtime-generated assets");

	static private final PackLocationInfo PACK_INFO = new PackLocationInfo("variants-cit:assetgen", PACK_TITLE, PackSource.BUILT_IN, Optional.empty());
	static private final PackSelectionConfig POSITION = new PackSelectionConfig(true, Position.BOTTOM, true);
	static private final Metadata METADATA = new Metadata(PACK_DESC, PackCompatibility.COMPATIBLE, FeatureFlagSet.of(), List.of());
	static private final PackMetadataSection PACK_METADATA = new PackMetadataSection(
		Component.literal("PackMetadata"), // TODO: Figure out what this does.
		SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES),
		Optional.empty()
	);

	static private final ResourcesSupplier FACTORY = new ResourcesSupplier() {
		public PackResources openPrimary(PackLocationInfo var1) { return INSTANCE; };
		public PackResources openFull(PackLocationInfo var1, Metadata var2) { return INSTANCE; };
	};

	static public final Pack PROFILE = new Pack(PACK_INFO, FACTORY, METADATA, POSITION);

	private Map<Identifier, IoSupplier<InputStream>> resources;
	{
		this.Reset();
	}

	/**
	 * Clears the pack  and returns  a mutable map  that can be used  to add new
	 * assets to the pack.
	 */
	public Map<Identifier, IoSupplier<InputStream>> Reset(){
		this.resources = new IdentityHashMap<>();
		return this.resources;
	}

	/**
	 * @return An immutable copy of the pack's content.
	 */
	public Map<Identifier, IoSupplier<InputStream>> GetAll(){
		return Map.copyOf(this.resources);
	}

	@Override
	public PackLocationInfo location() {
		return PACK_INFO;
	}

	@Override
	public <T> T getMetadataSection(MetadataSectionType<T> reader) throws IOException {
		return BuiltInMetadata.of(PackMetadataSection.TYPE, PACK_METADATA).get(reader);
	}

	static private IoSupplier<InputStream> GetIcon(){
		ModContainer mod = FabricLoader.getInstance().getModContainer("variants-cit").get();
		return mod.findPath("assets/variants-cit/icon.png")
			.map(IoSupplier::create)
			.orElse(null)
			;
	}

	@Override
	public @Nullable IoSupplier<@NotNull InputStream> getRootResource(String... segments) {
		String path = String.join("/", segments);
		switch (path) {
			default: return null;
			case "pack.png": return GetIcon();
		}
	}

	@Override
	public @Nullable IoSupplier<@NotNull InputStream> getResource(PackType type, Identifier resourceId) {
		if (type != PackType.CLIENT_RESOURCES)
			return null;

		return this.resources.get(resourceId);
	}

	@Override
	public void listResources(PackType type, String namespace, String prefix, ResourceOutput consumer) {
		if (type != PackType.CLIENT_RESOURCES)
			return;

		for (var entry : this.resources.entrySet()){
			Identifier id = entry.getKey();
			IoSupplier<InputStream> supplier = entry.getValue();

			if (id.getNamespace().equals(namespace) && id.getPath().startsWith(prefix)){
				consumer.accept(id, supplier);
			}
		}
	}

	@Override
	public Set<String> getNamespaces(PackType type) {
		var result = new HashSet<String>();
		for (Identifier id : this.resources.keySet())
			result.add(id.getNamespace());
		return result;
	}

	@Override
	public void close() {
	}
}
