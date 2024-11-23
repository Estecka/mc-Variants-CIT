package fr.estecka.variantscit.selectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.duck.DynamicModelResolverDuck;
import net.minecraft.client.render.item.model.BasicItemModel;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.model.RangeDispatchItemModel;
import net.minecraft.client.render.item.model.RangeDispatchItemModel.Entry;
import net.minecraft.client.render.model.ResolvableModel;
import net.minecraft.util.Identifier;

public class DynamicRangeDispatchUnbaked
implements ItemModel.Unbaked
{
	static public final MapCodec<DynamicRangeDispatchUnbaked> CODEC = RecordCodecBuilder.mapCodec( instance ->
		instance.group(
			RangeDispatchItemModel.Unbaked.CODEC.forGetter(u->u.inner),
			Identifier.CODEC.optionalFieldOf("variants-cit:baseId").forGetter(u->u.modelPrefix)
		)
		.apply(instance, DynamicRangeDispatchUnbaked::new)
	);

	private final Optional<Identifier> modelPrefix;
	private RangeDispatchItemModel.Unbaked inner;

	public DynamicRangeDispatchUnbaked(RangeDispatchItemModel.Unbaked inner, Optional<Identifier> modelPrefix){
		this.modelPrefix = modelPrefix;
		this.inner = inner;
	}

	@Override
	public MapCodec<DynamicRangeDispatchUnbaked> getCodec(){
		return CODEC;
	}

	@Override
	public void resolve(ResolvableModel.Resolver resolver){
		if (modelPrefix.isEmpty())
			; // no-op
		else if (resolver instanceof DynamicModelResolverDuck dyn){
			Map<Identifier,Float> models = dyn.<Float>variantscit$ResolveIf(this::GetThreshold);
			List<RangeDispatchItemModel.Entry> entries = new ArrayList<>(inner.entries());
			for (var entry : models.entrySet()){
				entries.add(new Entry(entry.getValue(), new BasicItemModel.Unbaked(entry.getKey(), List.of())));
			}
			inner = new RangeDispatchItemModel.Unbaked(inner.property(), inner.scale(), entries, inner.fallback());
		} else
			VariantsCitMod.LOGGER.error("Unable to dynamically resolve dispatch_range: {}", resolver.getClass());

		inner.resolve(resolver);
	}

	@Override
	public ItemModel bake(ItemModel.BakeContext context){
		return inner.bake(context);
	}

	private Optional<Float> GetThreshold(Identifier id){
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
