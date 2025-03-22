package fr.estecka.variantscit.nbt.properties;

import com.mojang.serialization.Codec;
import fr.estecka.variantscit.CodecUtil;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;

public class ComponentProperty<T>
implements INbtProperty
{
	private final ComponentType<T> type;
	private final Codec<T> codec;

	public ComponentProperty(ComponentType<T> type){
		this.type = type;
		this.codec = type.getCodecOrThrow();
	}

	public int getPropertyHash(ItemStack stack){
		Object cmp = stack.get(this.type);
		return (cmp!=null) ? cmp.hashCode() : 0;
	}

	public NbtElement getPropertyNbt(ItemStack stack){
		return CodecUtil.GetComponentNbt(stack.get(type), codec);
	}
}
