package fr.estecka.variantscit.assetgen;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.DataResult;
import net.minecraft.resource.InputSupplier;
import net.minecraft.util.Identifier;

public interface IAssetGenerator
{
	@Nullable InputSupplier<InputStream> AcceptAsset(EAssetGenPass pass, Identifier assetId);

	static public IAssetGenerator NOOP = (_0,_1)->null;

	static public IAssetGenerator OfList(final IAssetGenerator... generators){
		return OfList(List.of(generators));
	}

	static public IAssetGenerator OfList(final List<IAssetGenerator> generators){
		return (pass, assetId)->{
			for (IAssetGenerator generator : generators){
				InputSupplier<InputStream> result = generator.AcceptAsset(pass, assetId);
				if (result != null)
					return result;
			}
			return null;
		};
	}

	static public interface Builder
	extends Supplier<DataResult<IAssetGenerator>>
	{}

	static public record ListBuilder(Builder[] builders)
	implements Builder
	{
		@SafeVarargs
		static public ListBuilder Of(Builder... builders){
			return new ListBuilder(builders);
		}
	
		@Override
		public DataResult<IAssetGenerator> get() {
			List<IAssetGenerator> generators = new ArrayList<>(builders.length);
			for (var b : builders){
				DataResult<IAssetGenerator> r = b.get();
				if (r.isError())
					return r;
				else
					generators.add(r.getOrThrow());
			}

			return DataResult.success(OfList(generators));
		}
	}
}
