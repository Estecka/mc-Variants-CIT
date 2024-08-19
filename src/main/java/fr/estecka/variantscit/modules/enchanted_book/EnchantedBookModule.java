package fr.estecka.variantscit.modules.enchanted_book;

import fr.estecka.variantscit.api.ACitModule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class EnchantedBookModule
extends ACitModule
implements ClientModInitializer, ModelLoadingPlugin
{
	static public final Identifier CIT_MULTI = Identifier.of("enchants-cit", "item/multi_enchanted_book");

	public EnchantedBookModule(){
		super("item/enchanted_book", Items.ENCHANTED_BOOK);
	}

	@Override
	public void onInitializeClient(){
		ModelLoadingPlugin.register(this);
		ModelPredicateProviderRegistry.register(Items.ENCHANTED_BOOK, Identifier.ofVanilla("level"), new LevelPredicate());
	}

	@Override
	public void onInitializeModelLoader(ModelLoadingPlugin.Context pluginContext){
		pluginContext.addModels(CIT_MULTI);
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
			return CIT_MULTI;
		else
			return super.GetModelForItem(stack);
	}
}
