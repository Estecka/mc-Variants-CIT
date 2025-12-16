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
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackCompatibility;
import net.minecraft.resource.ResourcePackInfo;
import net.minecraft.resource.ResourcePackPosition;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.ResourcePackProfile.InsertionPosition;
import net.minecraft.resource.ResourcePackProfile.Metadata;
import net.minecraft.resource.ResourcePackProfile.PackFactory;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.resource.metadata.ResourceMetadataMap;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class GeneratedResourcePack
implements ResourcePack
{
	static public final GeneratedResourcePack INSTANCE = new GeneratedResourcePack();

	static public final Text PACK_TITLE = Text.literal("Variants-CIT Mod");
	static public final Text PACK_DESC  = Text.literal("Runtime-generated assets");

	static private final ResourcePackInfo PACK_INFO = new ResourcePackInfo("variants-cit:assetgen", PACK_TITLE, ResourcePackSource.BUILTIN, Optional.empty());
	static private final ResourcePackPosition POSITION = new ResourcePackPosition(true, InsertionPosition.BOTTOM, true);
	static private final Metadata METADATA = new Metadata(PACK_DESC, ResourcePackCompatibility.COMPATIBLE, FeatureSet.empty(), List.of());
	static private final PackResourceMetadata PACK_METADATA = new PackResourceMetadata(
		Text.literal("PackMetadata"), // TODO: Figure out what this does.
		SharedConstants.getGameVersion().getResourceVersion(ResourceType.CLIENT_RESOURCES),
		Optional.empty()
	);

	static private final PackFactory FACTORY = new PackFactory() {
		public ResourcePack open(ResourcePackInfo var1) { return INSTANCE; };
		public ResourcePack openWithOverlays(ResourcePackInfo var1, Metadata var2) { return INSTANCE; };
	};

	static public final ResourcePackProfile PROFILE = new ResourcePackProfile(PACK_INFO, FACTORY, METADATA, POSITION);

	private Map<Identifier, InputSupplier<InputStream>> resources;
	{
		this.Reset();
	}

	/**
	 * Clears the pack  and returns a mutable pack  that can be used  to add new
	 * assets to the pack.
	 */
	public Map<Identifier, InputSupplier<InputStream>> Reset(){
		this.resources = new IdentityHashMap<>();
		return this.resources;
	}

	/**
	 * @return An immutable copy of the pack's content.
	 */
	public Map<Identifier, InputSupplier<InputStream>> GetAll(){
		return Map.copyOf(this.resources);
	}

	@Override
	public ResourcePackInfo getInfo() {
		return PACK_INFO;
	}

	@Override
	public <T> T parseMetadata(ResourceMetadataSerializer<T> reader) throws IOException {
		return ResourceMetadataMap.of(PackResourceMetadata.SERIALIZER, PACK_METADATA).get(reader);
	}

	static private InputSupplier<InputStream> GetIcon(){
		ModContainer mod = FabricLoader.getInstance().getModContainer("variants-cit").get();
		return mod.findPath("assets/variants-cit/icon.png")
			.map(InputSupplier::create)
			.orElse(null)
			;
	}

	@Override
	public @Nullable InputSupplier<@NotNull InputStream> openRoot(String... segments) {
		String path = String.join("/", segments);
		switch (path) {
			default: return null;
			case "pack.png": return GetIcon();
		}
	}

	@Override
	public @Nullable InputSupplier<@NotNull InputStream> open(ResourceType type, Identifier resourceId) {
		if (type != ResourceType.CLIENT_RESOURCES)
			return null;

		return this.resources.get(resourceId);
	}

	@Override
	public void findResources(ResourceType type, String namespace, String prefix, ResultConsumer consumer) {
		if (type != ResourceType.CLIENT_RESOURCES)
			return;

		for (var entry : this.resources.entrySet()){
			Identifier id = entry.getKey();
			InputSupplier<InputStream> supplier = entry.getValue();

			if (id.getNamespace().equals(namespace) && id.getPath().startsWith(prefix)){
				consumer.accept(id, supplier);
			}
		}
	}

	@Override
	public Set<String> getNamespaces(ResourceType type) {
		var result = new HashSet<String>();
		for (Identifier id : this.resources.keySet())
			result.add(id.getNamespace());
		return result;
	}

	@Override
	public void close() {
	}
}
