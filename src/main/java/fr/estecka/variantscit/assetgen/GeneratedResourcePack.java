package fr.estecka.variantscit.assetgen;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;
// import net.fabricmc.fabric.impl.resource.loader.PlaceholderResourcePack;
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
	static private final GeneratedResourcePack INSTANCE = new GeneratedResourcePack();

	static private final ResourcePackInfo PACK_INFO = new ResourcePackInfo("variants-cit:assetgen", Text.literal("Variants-CIT Mod"), ResourcePackSource.BUILTIN, Optional.empty());
	static private final ResourcePackPosition POSITION = new ResourcePackPosition(true, InsertionPosition.BOTTOM, true);
	static private final Metadata METADATA = new Metadata(Text.literal("Runtime-generated assets"), ResourcePackCompatibility.COMPATIBLE, FeatureSet.empty(), List.of());
	static private final PackResourceMetadata PACK_METADATA = new PackResourceMetadata(
		Text.literal("PackMetadata"),
		SharedConstants.getGameVersion().getResourceVersion(ResourceType.CLIENT_RESOURCES),
		Optional.empty()
	);

	// static private final PlaceholderResourcePack PLACEHOLDER = new PlaceholderResourcePack(ResourceType.CLIENT_RESOURCES, PACK_INFO);
	static private final PackFactory FACTORY = new PackFactory() {
		public ResourcePack open(ResourcePackInfo var1) { return INSTANCE; };
		public ResourcePack openWithOverlays(ResourcePackInfo var1, Metadata var2) { return INSTANCE; };
	};

	static public final ResourcePackProfile PROFILE = new ResourcePackProfile(PACK_INFO, FACTORY, METADATA, POSITION);

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
	public InputSupplier<InputStream> openRoot(String... segments) {
		String path = String.join("/", segments);
		switch (path) {
			default: return null;
			case "pack.png": return GetIcon();
		}
	}

	@Override
	public InputSupplier<InputStream> open(ResourceType type, Identifier var2) {
		if (type != ResourceType.CLIENT_RESOURCES)
			return null;

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void findResources(ResourceType type, String namepsace, String prefix, ResultConsumer consumer) {
		// TODO Auto-generated method stub
	}

	@Override
	public Set<String> getNamespaces(ResourceType type) {
		// TODO Auto-generated method stub
		return Set.of();
	}

	@Override
	public void close() {
	}
}
