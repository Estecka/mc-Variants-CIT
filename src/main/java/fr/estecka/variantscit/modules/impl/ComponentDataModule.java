package fr.estecka.variantscit.modules.impl;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.modules.cache.CacheKeySet;
import fr.estecka.variantscit.modules.cache.ECachePolicy;
import fr.estecka.variantscit.modules.libraries.ISimpleCitModule;
import fr.estecka.variantscit.modules.libraries.IVariantLibrary;
import fr.estecka.variantscit.commands.CommandLogger;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;
import fr.estecka.variantscit.itemdata.extractors.IDataExtractor;
import fr.estecka.variantscit.itemdata.extractors.TransformableExtractor;


public class ComponentDataModule<P extends IDataExtractor>
implements ISimpleCitModule
{
	static public final MapCodec<ComponentDataModule<?>> CODEC = IDataExtractor.MAPCODEC.xmap(ComponentDataModule::new, o->o.property);

	private final P property;

	public ComponentDataModule(P property){
		this.property = property;
	}

	@Override
	public CacheKeySet GetCacheKeys() {
		return CacheKeySet.Of(property.GetCacheKey());
	}

	@Override
	public ECachePolicy GetCachePolicy() {
		return ECachePolicy.ALWAYS;
	}

	@Override
	public @Nullable ResourceLocation GetItemVariant(ItemStack stack) {
		String result = this.property.Extract(stack).asString();
		return (result!=null) ? ResourceLocation.tryParse(result) : null;
	}

	@Override
	public @Nullable ResourceLocation Walkthrough(ItemStack stack, IVariantLibrary library, CommandLogger logger) {
		IDataContainer raw = TransformableExtractor.Unwrap(this.property).Extract(stack);
		IDataContainer transformed = property.Extract(stack);

		logger.Info("Raw data: {}",    CommandLogger.ItemData(raw.value(), "Missing or invalid"));
		logger.Info("Transformed: {}", CommandLogger.ItemData(transformed.value()));

		return this.GetItemModel(stack, library);
	}
}
