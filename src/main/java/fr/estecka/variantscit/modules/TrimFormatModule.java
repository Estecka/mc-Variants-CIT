package fr.estecka.variantscit.modules;

import java.util.function.Predicate;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import static net.minecraft.component.DataComponentTypes.TRIM;

/**
 * TODO: An option to choose the namespace of the variant.
 * TODO: The name of the item is probably unnecessary.
 */
@Deprecated
public class TrimFormatModule
extends ASimpleItemCachingModule
{
	static public final MapCodec<TrimFormatModule> CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			Codec.STRING.fieldOf("variantFormat").orElse("${item}/${pattern}.${material}").forGetter(p->p.format)
		)
		.apply(builder, TrimFormatModule::new)
	);

	private final String format;

	private TrimFormatModule(String format)
	throws IllegalStateException
	{
		this.format = format;

		if (!Identifier.isPathValid(this.Substitute("a","b","c")))
			throw new IllegalStateException("Invalid path format: "+format);
	}


	@Override
	public Predicate<ItemStack> GetValidator(ItemStack stack){
		ArmorTrim trim = stack.get(TRIM);
		return (futureStack) -> futureStack.get(TRIM) != trim;
	}

	@Override
	public Identifier RecomputeItemVariant(ItemStack stack){
		ArmorTrim trim = stack.get(TRIM);
		if (trim == null)
			return null;

		Identifier type = Registries.ITEM.getEntry(stack.getItem()).getKey().get().getValue();
		Identifier pattern = trim.getPattern().getKey().get().getValue();
		String material = trim.getMaterial().value().assetName();

		return pattern.withPath( this.Substitute(type.getPath(), pattern.getPath(), material));
	}

	private String Substitute(String itemType, String pattern, String material){
		return this.format
			.replace("${item}", itemType)
			.replace("${pattern}", pattern)
			.replace("${material}", material)
			;
	}
}
