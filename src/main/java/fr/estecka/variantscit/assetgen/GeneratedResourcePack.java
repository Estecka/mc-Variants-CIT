package fr.estecka.variantscit.assetgen;

import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.impl.resource.loader.PlaceholderResourcePack;
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
import net.minecraft.text.Text;

public class GeneratedResourcePack
{
	static private final ResourcePackInfo PACK_INFO = new ResourcePackInfo("variants-cit:assetgen", Text.literal("Variants-CIT Generators"), ResourcePackSource.BUILTIN, Optional.empty());
	static private final ResourcePackPosition POSITION = new ResourcePackPosition(true, InsertionPosition.BOTTOM, true);
	static private final Metadata METADATA = new Metadata(Text.literal("Runtime-generated assets"), ResourcePackCompatibility.COMPATIBLE, FeatureSet.empty(), List.of());

	static private final PlaceholderResourcePack PLACEHOLDER = new PlaceholderResourcePack(ResourceType.CLIENT_RESOURCES, PACK_INFO);
	static private final PackFactory FACTORY = new PackFactory() {
		public ResourcePack open(ResourcePackInfo var1) { return PLACEHOLDER; };
		public ResourcePack openWithOverlays(ResourcePackInfo var1, Metadata var2) { return PLACEHOLDER; };
	};

	static public final ResourcePackProfile PROFILE = new ResourcePackProfile(PACK_INFO, FACTORY, METADATA, POSITION);
}
