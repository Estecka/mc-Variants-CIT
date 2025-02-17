package fr.estecka.variantscit;

import java.util.List;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;

public class CodecUtil
{
	static private final MinecraftClient client = MinecraftClient.getInstance();

	static public <T> Codec<List<T>> OneOrMany(Codec<T> original){
		var listCodec = original.listOf();
		return Codec.withAlternative(
			listCodec,
			Codec.of(listCodec, original.map(t->List.of(t)))
		);
	}

	static public <T> MapCodec<T> MapWithAlternative(MapCodec<T> primary, MapCodec<T> alternative){
		return MapCodec.assumeMapUnsafe(Codec.withAlternative(primary.codec(), alternative.codec()));
	}

	static public <T> @Nullable NbtElement GetComponentNbt(ItemStack stack, ComponentType<T> type){
		T component = stack.get(type);
		if (component == null)
			return null;
		else
			return GetComponentNbt(component, type.getCodecOrThrow());
	}

	static public <T> @Nullable NbtElement GetComponentNbt(T component, Codec<T> codec){
		DynamicOps<NbtElement> nbtOps = NbtOps.INSTANCE;
		// Enables encoding of data from dynamic registries
		if (client.world != null)
			nbtOps = client.world.getRegistryManager().getOps(nbtOps);

		var dataResult = codec.encodeStart(nbtOps, component);
		if (dataResult.isSuccess())
			return dataResult.getOrThrow();
		else {
			VariantsCitMod.LOGGER.error("Unable to serialize component: {}", dataResult.error().get().message() );
			return null;
		}
	}
}
