package fr.estecka.variantscit.modules.impl;

import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.modules.libraries.LinearLibrary;
import fr.estecka.variantscit.modules.libraries.LinearLibrary.ILinearCitModule;
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
