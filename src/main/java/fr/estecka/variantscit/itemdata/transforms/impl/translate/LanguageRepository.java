package fr.estecka.variantscit.itemdata.transforms.impl.translate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.exception.ExceptionUtils;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.assetgen.HotswappableResourceManager;
import fr.estecka.variantscit.util.CollectionUtil;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.locale.Language;


public final class LanguageRepository
{
	static private final Language EMPTY_LANG = new ClientLanguage(Map.of(), false);

	/**
	 * I don't know  what would be  the effect  of keeping  the resource manager
	 * around indefinitely, but  this is probably  not good  for performance. In
	 * doubt, this needs to be cleared after resource reload ends.
	 */
	static HotswappableResourceManager resourceManager = null;
	static private LanguageRepository instance = new LanguageRepository();

	private final Map<List<String>, Language> loadedLanguages = new HashMap<>();


	static public LanguageRepository Instance(){
		return instance;
	}

	static public LanguageRepository Clear(){
		return instance = new LanguageRepository();
	}

	static public void SetResourceManager(HotswappableResourceManager manager){
		LanguageRepository.resourceManager = manager;
	}


	public Language GetLanguage(List<String> codes, boolean right2Left){
		return loadedLanguages.computeIfAbsent(codes, k -> this.CreateLanguage(codes, right2Left));
	}

	public Language CreateLanguage(List<String> codes, boolean right2Left){
		String printableCodes = CollectionUtil.ConcatToString(codes);

		if (resourceManager == null) {
			String trace = ExceptionUtils.getStackTrace(new RuntimeException());
			VariantsCitMod.LOGGER.error("Attempted to access new language after resource reload: {}\n{}", printableCodes, trace);
			return EMPTY_LANG;
		}

		VariantsCitMod.LOGGER.info("Loading additional language set: {}", printableCodes);
		return ClientLanguage.loadFrom(resourceManager.Get(), codes.reversed(), right2Left);
	}

}
