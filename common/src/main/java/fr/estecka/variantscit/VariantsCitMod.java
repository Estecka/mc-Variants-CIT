package fr.estecka.variantscit;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.reload.EModuleContext;
import fr.estecka.variantscit.reload.ModuleLoader;
import fr.estecka.variantscit.reload.MetaModule;
import fr.estecka.variantscit.modules.IBakedModule;


public class VariantsCitMod
{
	static public final String MODID = "variants-cit";
	static public final LabelledLogger LOGGER = new LabelledLogger();
	static private IModLoader MODLOADER;

	static public int reloadcount = 0;
	static public final EquippableCache EQUIPABLES = new EquippableCache();
	static private Map<Item, IBakedModule> ITEM_MODULES  = Map.of();
	static private Map<Item, IBakedModule> EQUIP_MODULES = Map.of();
	static private Map<Identifier, MetaModule> META = Map.of();

	static public Identifier Identifier(String path){
		return Identifier.of(MODID, path);
	}

	static public IModLoader GetModLoader(){
		return MODLOADER;
	}

	static public @Nullable IBakedModule GetItemModule(Item itemType){
		return ITEM_MODULES.get(itemType);
	}
	static public @Nullable IBakedModule GetEquipmentModule(Item itemType){
		return EQUIP_MODULES.get(itemType);
	}
	static public Map<Identifier,MetaModule> GetMeta(){
		return Map.copyOf(META);
	}
	static public IBakedModule GetModule(EModuleContext context, Identifier id){
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

	static public void initialize(IModLoader modloader){
		MODLOADER = modloader;
		MODLOADER.LetYourNameBeKnownToAll();
		// TODO: Create accessors
		// NumericProperties.ID_MAPPER.put(Identifier.of(MODID, "block_entity_data"), NbtNumberProperty.CreateCodec(DataComponentTypes.BLOCK_ENTITY_DATA));
		// NumericProperties.ID_MAPPER.put(Identifier.of(MODID, "bucket_entity_data"), NbtNumberProperty.CreateCodec(DataComponentTypes.BUCKET_ENTITY_DATA));
		// NumericProperties.ID_MAPPER.put(Identifier.of(MODID, "custom_data"), NbtNumberProperty.CreateCodec(DataComponentTypes.CUSTOM_DATA));
		// NumericProperties.ID_MAPPER.put(Identifier.of(MODID, "entity_data"), NbtNumberProperty.CreateCodec(DataComponentTypes.ENTITY_DATA));
		// NumericProperties.ID_MAPPER.put(Identifier.of(MODID, "stored_enchantment_level"), EnchantedBookLevelPredicate.CODEC);
		// ItemModelTypes.ID_MAPPER.put(Identifier.ofVanilla("range_dispatch"), DynamicRangeDispatchUnbaked.CODEC);
	}

	static public void OnResourceReload(ModuleLoader.Result result){
		++reloadcount;
		EQUIPABLES.Clear();
		ITEM_MODULES  = result.itemModules;
		EQUIP_MODULES = result.equipModules;
		META = result.allModules;
	}

}
