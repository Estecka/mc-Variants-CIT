package fr.estecka.variantscit.itemdata.transforms;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import fr.estecka.variantscit.VCitRegistries;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;
import fr.estecka.variantscit.itemdata.containers.RawDataContainer;


@FunctionalInterface
public interface IDataTransform
{
	static public final Codec<IDataTransform> CODEC = VCitRegistries.TRANSFORMS.codec;
	static public final IDataTransform NOOP = o->o;
	static public final IDataTransform NULL = o->null;

	@Nullable IDataContainer LooseTypedTransform(@Nullable IDataContainer input);

	static boolean Test(IDataTransform transform, Object data){
		return transform.LooseTypedTransform(RawDataContainer.OfNullable(data)) != null;
	}

	public interface NullSafe
	extends IDataTransform
	{
		@Nullable IDataContainer NullSafeTransform(@NotNull IDataContainer input);

		@Override
		default IDataContainer LooseTypedTransform(IDataContainer input) {
			if (input == null)
				return null;
			else
				return this.NullSafeTransform(input);
		}
	}
}
