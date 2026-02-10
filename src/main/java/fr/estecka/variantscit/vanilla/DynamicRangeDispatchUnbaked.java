package fr.estecka.variantscit.vanilla;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.RangeSelectItemModel;
import net.minecraft.client.renderer.item.RangeSelectItemModel.Entry;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.resources.ResourceLocation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.duck.DynamicModelResolverDuck;

public class DynamicRangeDispatchUnbaked
implements ItemModel.Unbaked
{
	static public final MapCodec<DynamicRangeDispatchUnbaked> CODEC = RecordCodecBuilder.mapCodec( instance ->
		instance.group(
			RangeSelectItemModel.Unbaked.MAP_CODEC.forGetter(u->u.inner),
			ResourceLocation.CODEC.optionalFieldOf("variants-cit:baseId").forGetter(u->u.modelPrefix)
		)
		.apply(instance, DynamicRangeDispatchUnbaked::new)
	);

	private final Optional<ResourceLocation> modelPrefix;
	private RangeSelectItemModel.Unbaked inner;

	public DynamicRangeDispatchUnbaked(RangeSelectItemModel.Unbaked inner, Optional<ResourceLocation> modelPrefix){
		this.modelPrefix = modelPrefix;
		this.inner = inner;
	}

	@Override
	public MapCodec<DynamicRangeDispatchUnbaked> type(){
		return CODEC;
	}

	@Override
	public void resolveDependencies(ResolvableModel.Resolver resolver){
		if (modelPrefix.isEmpty())
			; // no-op
		else if (resolver instanceof DynamicModelResolverDuck dyn){
			Map<ResourceLocation,Float> models = dyn.<Float>variantscit$ResolveIf(this::GetThreshold);
			List<RangeSelectItemModel.Entry> entries = new ArrayList<>(inner.entries());
			for (var entry : models.entrySet()){
				entries.add(new Entry(entry.getValue(), new BlockModelWrapper.Unbaked(entry.getKey(), List.of())));
			}
			inner = new RangeSelectItemModel.Unbaked(inner.property(), inner.scale(), entries, inner.fallback());
		} else
			VariantsCitMod.LOGGER.error("Unable to dynamically resolve dispatch_range: {}", resolver.getClass());

		inner.resolveDependencies(resolver);
	}

	@Override
	public ItemModel bake(ItemModel.BakingContext context){
		return inner.bake(context);
	}

	private Optional<Float> GetThreshold(ResourceLocation id){
		if (!modelPrefix.get().getNamespace().equals(id.getNamespace()))
			return Optional.empty();

		final String prefix = this.modelPrefix.get().getPath();
		String path = id.getPath();
		if (!path.startsWith(prefix))
			return Optional.empty();

		try {
			return Optional.of(Float.parseFloat(path.substring(prefix.length())));
		}
		catch (NumberFormatException e){
			return Optional.empty();
		}
	}

}
