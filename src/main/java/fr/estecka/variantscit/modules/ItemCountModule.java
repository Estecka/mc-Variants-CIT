package fr.estecka.variantscit.modules;

import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.modulebakers.ILinearLibrary;
import fr.estecka.variantscit.modulebakers.LinearLibrary.ILinearCitModule;
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
	public Identifier GetItemModel(ItemStack stack, ILinearLibrary library){
		return library.GetOrLesser(stack.getCount());
	}
}
