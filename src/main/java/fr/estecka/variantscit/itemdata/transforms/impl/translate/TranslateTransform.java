package fr.estecka.variantscit.itemdata.transforms.impl.translate;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;
import fr.estecka.variantscit.itemdata.containers.RawDataContainer;
import fr.estecka.variantscit.itemdata.transforms.IDataTransform;
import fr.estecka.variantscit.util.CodecUtil;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;


public record TranslateTransform(Language language)
implements IDataTransform.NullSafe
{
	static public final MapCodec<TranslateTransform> MAPCODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			Codec.STRING.listOf().fieldOf("languages").forGetter(CodecUtil.NoGetter("langCodes")),
			Codec.BOOL.optionalFieldOf("defaultRightToLeft", false).forGetter(CodecUtil.NoGetter("right2Left"))
		)
		.apply(builder, TranslateTransform::new)
	);

	public TranslateTransform(
		List<String> langCodes,
		boolean right2Left
	){
		this(LanguageRepository.Instance().GetLanguage(langCodes, right2Left));
	}

	@Override
	public @Nullable IDataContainer NullSafeTransform(@NotNull IDataContainer input) {
		if (input.value() instanceof Component text)
			return RawDataContainer.OfNullable(this.Translate(text));
		else
			return null;
	}

	public String Translate(Component text){
		Language clientLanguage = Language.getInstance();
		try {
			Language.inject(this.language);
			return text.getString();
		}
		finally	{
			Language.inject(clientLanguage);
		}
	}
}
