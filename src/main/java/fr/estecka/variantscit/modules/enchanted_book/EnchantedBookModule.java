package fr.estecka.variantscit.modules.enchanted_book;

import fr.estecka.variantscit.ModuleDefinition;
import fr.estecka.variantscit.api.ACitModule;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class EnchantedBookModule
extends ACitModule
{
	private final Identifier citMulti;

	public EnchantedBookModule(ModuleDefinition definition){
		super(definition);
		citMulti = definition.specialModels().get("multi");
	}

	@Override
	public void onInitializeModelLoader(ModelLoadingPlugin.Context pluginContext){
		super.onInitializeModelLoader(pluginContext);
		pluginContext.addModels(citMulti);
	}

	@Override
	public Identifier GetItemVariant(ItemStack stack){
		ItemEnchantmentsComponent enchants = stack.get(DataComponentTypes.STORED_ENCHANTMENTS);
		if (enchants != null && enchants.getSize() == 1)
			return enchants.getEnchantments().iterator().next().getKey().get().getValue();
		else
			return null;
	}

	@Override
	public Identifier GetModelForItem(ItemStack stack){
		ItemEnchantmentsComponent enchants = stack.get(DataComponentTypes.STORED_ENCHANTMENTS);
		if (enchants != null && enchants.getSize() > 1)
			return citMulti;
		else
			return super.GetModelForItem(stack);
	}
}
