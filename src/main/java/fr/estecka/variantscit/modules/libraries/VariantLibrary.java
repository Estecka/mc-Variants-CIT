package fr.estecka.variantscit.modules.libraries;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.commands.CommandLogger;

public record VariantLibrary(
	@Nullable ResourceLocation fallbackModel,
	Map<ResourceLocation, ResourceLocation> variantModels,
	Map<String, ResourceLocation> specialModels
)
implements IVariantLibrary, IDebuggableLibrary<IVariantLibrary>
{
	@Override
	public boolean HasVariantModel(ResourceLocation variant){
		return this.variantModels.containsKey(variant);
	}

	@Override
	public @Nullable ResourceLocation GetVariantModel(ResourceLocation variant){
		if (variant == null)
			return null;
		else
			return this.variantModels.getOrDefault(variant, this.fallbackModel);
	}

	@Override
	public @Nullable ResourceLocation GetSpecialModel(String key){
		return this.specialModels.get(key);
	}

	public int GetVariantCount(){
		return this.variantModels.size();
	}

	public boolean isEmpty(){
		return this.variantModels.isEmpty()
		    && this.specialModels.isEmpty()
		    && this.fallbackModel == null
		    ;
	}

	public VariantLibrary GetSubLibrary(String subPrefix){
		Map<ResourceLocation,ResourceLocation> subVariants = new HashMap<>();
		for (var entry : this.variantModels.entrySet())
		if  (entry.getKey().getPath().startsWith(subPrefix))
		{
			subVariants.put(
				entry.getKey().withPath(path->path.substring(subPrefix.length())),
				entry.getValue()
			);
		}

		return new VariantLibrary(
			this.fallbackModel,
			Map.copyOf(subVariants),
			this.specialModels
		);
	}


/******************************************************************************/
/* # Debug Commands                                                           */
/******************************************************************************/

	@Override
	public void Summary(CommandLogger logger){
		logger.Info("This module handles {} variants.", this.variantModels.size());
	}

	@Override
	public void Dump(CommandLogger logger){
		if (this.variantModels.isEmpty())
			logger.Info("This module does not have any variant.");
		else for (var entry : this.variantModels.entrySet())
		{
			logger.Info("{} -> {} ",
				CommandLogger.ItemData(entry.getKey()),
				CommandLogger.PackData(entry.getValue())
			);
		}
	}

	@Override
	public Snitch<IVariantLibrary> CreateSnitch(CommandLogger logger) {
		return new SnitchingLibrary(logger);
	}

	private class SnitchingLibrary
	extends IDebuggableLibrary.Snitch<IVariantLibrary>
	implements IVariantLibrary
	{
		public SnitchingLibrary (CommandLogger logger){
			super(logger);
		}

		@Override
		public boolean HasVariantModel(@Nullable ResourceLocation variantId) {
			boolean r = VariantLibrary.this.HasVariantModel(variantId);
			this.OnTriedVariant(variantId, r);
			return r;
		}

		@Override
		public @Nullable ResourceLocation GetVariantModel(ResourceLocation variantId) {
			this.HasVariantModel(variantId);
			return VariantLibrary.this.GetVariantModel(variantId);
		}

		@Override
		public @Nullable ResourceLocation GetSpecialModel(String key) {
			ResourceLocation r = VariantLibrary.this.GetSpecialModel(key);
			this.OnTriedSpecial(key, r != null);
			return r;
		}

		@Override
		protected void OnTriedVariant(ResourceLocation variantId, boolean exists) {
			logger.Info("Tested variant ID: {}", CommandLogger.ItemData(variantId));
			super.OnTriedVariant(variantId, exists);
		}
	}

}
