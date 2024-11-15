package fr.estecka.variantscit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.render.item.property.numeric.NumericProperties;
import net.minecraft.client.render.item.property.numeric.NumericProperty;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

@Mixin(NumericProperties.class)
public interface NumericPropertiesAccessor
{
	@Accessor("ID_MAPPER") static public Codecs.IdMapper<Identifier, MapCodec<? extends NumericProperty>> ID_MAPPER(){ throw new AssertionError(); }
}
