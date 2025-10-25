package fr.estecka.variantscit.modules;

import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.modulebakers.LinearLibrary;
import fr.estecka.variantscit.modulebakers.LinearLibrary.ILinearCitModule;
// import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
// import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public record ItemCountModule(String namespace)
implements ILinearCitModule
{
	static public final MapCodec<ItemCountModule> CODEC = CodecUtil.IDENTIFIER_NAMESPACE
		.optionalFieldOf("namespace", "minecraft")
		.xmap(ItemCountModule::new, ItemCountModule::GetNamespace)
		;

	@Override
	public String GetNamespace() {
		return namespace;
	}

	@Override
	public Identifier GetItemModel(ItemStack stack, LinearLibrary library){
		return library.GetOrLesser(stack.getCount());
	}
}
