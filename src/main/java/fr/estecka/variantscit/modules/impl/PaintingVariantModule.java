package fr.estecka.variantscit.modules.impl;

import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.level.Level;
import fr.estecka.variantscit.itemdata.extractors.impl.PaintingVariantProperty;
import fr.estecka.variantscit.modules.cache.ECachePolicy;
import fr.estecka.variantscit.modules.libraries.IVariantLibrary;

public class PaintingVariantModule
extends AMonoComponentModule<Holder<PaintingVariant>>
{
	static public final PaintingVariantModule UNIT = new PaintingVariantModule();

	public PaintingVariantModule(){
		super(DataComponents.PAINTING_VARIANT, ECachePolicy.AVOID);
	}

	static public Optional<Registry<PaintingVariant>> GetPaintingRegistry(){
		Level world = Minecraft.getInstance().level;
		if (world != null)
			return world.registryAccess().lookup(Registries.PAINTING_VARIANT);
		else
			return Optional.empty();
	}

	public Identifier GetModelForComponent(Holder<PaintingVariant> component, IVariantLibrary models){
		if (component == null)
			return null;

		Identifier variantId = PaintingVariantProperty.UNIT.GetPropertyId(component);

		var registry = GetPaintingRegistry();
		if (registry.isPresent() && !registry.get().containsKey(variantId))
			return models.GetSpecialModel("invalid");

		return models.GetVariantModel(variantId);
	}

}
