package fr.estecka.variantscit;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.reload.EModuleContext;
import fr.estecka.variantscit.reload.ModuleLoader;
import fr.estecka.variantscit.reload.MetaModule;
import fr.estecka.variantscit.commands.AssetGenCommands;
import fr.estecka.variantscit.commands.ModuleCommands;
import fr.estecka.variantscit.modules.IBakedModule;
import fr.estecka.variantscit.vanilla.*;


public class VariantsCitMod
implements ClientModInitializer
{
	static public final String MODID = "variants-cit";
	public static final LabelledLogger LOGGER = new LabelledLogger();

	static public int reloadcount = 0;
	static public final EquippableCache EQUIPABLES = new EquippableCache();
	static private Map<Item, IBakedModule> ITEM_MODULES  = Map.of();
	static private Map<Item, IBakedModule> EQUIP_MODULES = Map.of();
	static private Map<ResourceLocation, MetaModule> META = Map.of();

	static public ResourceLocation Identifier(String path){
		return ResourceLocation.fromNamespaceAndPath(MODID, path);
	}

	static public @Nullable IBakedModule GetItemModule(Item itemType){
		return ITEM_MODULES.get(itemType);
	}
	static public @Nullable IBakedModule GetEquipmentModule(Item itemType){
		return EQUIP_MODULES.get(itemType);
	}
	static public Map<ResourceLocation,MetaModule> GetMeta(){
		return Map.copyOf(META);
	}
	static public IBakedModule GetModule(EModuleContext context, ResourceLocation id){
		MetaModule meta = META.get(id);
		if (meta == null)
			return null;
		else {
			return switch (context) {
				default -> throw new IllegalArgumentException();
				case ITEM_MODEL -> meta.itemModule().orElse(null);
				case EQUIPPABLE -> meta.equipModule().orElse(null);
			};
		}
	}

	@Override
	public void onInitializeClient(){
		RangeSelectItemModelProperties.ID_MAPPER.put(ResourceLocation.fromNamespaceAndPath(MODID, "block_entity_data"), NbtNumberProperty.CreateCodec(DataComponents.BLOCK_ENTITY_DATA));
		RangeSelectItemModelProperties.ID_MAPPER.put(ResourceLocation.fromNamespaceAndPath(MODID, "bucket_entity_data"), NbtNumberProperty.CreateCodec(DataComponents.BUCKET_ENTITY_DATA));
		RangeSelectItemModelProperties.ID_MAPPER.put(ResourceLocation.fromNamespaceAndPath(MODID, "custom_data"), NbtNumberProperty.CreateCodec(DataComponents.CUSTOM_DATA));
		RangeSelectItemModelProperties.ID_MAPPER.put(ResourceLocation.fromNamespaceAndPath(MODID, "entity_data"), NbtNumberProperty.CreateCodec(DataComponents.ENTITY_DATA));
		RangeSelectItemModelProperties.ID_MAPPER.put(ResourceLocation.fromNamespaceAndPath(MODID, "stored_enchantment_level"), EnchantedBookLevelPredicate.CODEC);
		ItemModels.ID_MAPPER.put(ResourceLocation.withDefaultNamespace("range_dispatch"), DynamicRangeDispatchUnbaked.CODEC);

		ModuleCommands.Register();
		AssetGenCommands.Register();
	}

	static public void OnResourceReload(ModuleLoader.Result result){
		++reloadcount;
		EQUIPABLES.Clear();
		ITEM_MODULES  = result.itemModules;
		EQUIP_MODULES = result.equipModules;
		META = result.allModules;
	}

}
