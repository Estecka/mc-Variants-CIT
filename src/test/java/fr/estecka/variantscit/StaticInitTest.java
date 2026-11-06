package fr.estecka.variantscit;

import fr.estecka.variantscit.itemdata.extractors.IDataExtractor;
import fr.estecka.variantscit.itemdata.extractors.impl.ItemComponentProperty;
import fr.estecka.variantscit.itemdata.preconditions.ConditionList;
import fr.estecka.variantscit.itemdata.preconditions.IItemPrecondition;
import fr.estecka.variantscit.itemdata.preconditions.MonostringConditionBuilder;
import fr.estecka.variantscit.itemdata.transforms.IDataTransform;
import fr.estecka.variantscit.itemdata.transforms.SuccessiveTransform;
import fr.estecka.variantscit.modules.impl.GroupModule;
import fr.estecka.variantscit.modules.impl.MultiComponentFormatModule;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class StaticInitTest
{
	@BeforeAll
	static public void Init(){
		// Required to initialize @linkplain{ItemComponentProperty#MAP_CODEC}
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	/**
	 * Any of these being null  indicates a faulty static initialization, caused
	 * by cirular dependencies between classes.
	 */
	@Test
	public void Test(){
		Assertions.assertNotNull(SuccessiveTransform.CHAIN_CODEC);
		Assertions.assertNotNull(SuccessiveTransform.MAPCODEC);
		Assertions.assertNotNull(IDataTransform.NOOP);
		Assertions.assertNotNull(IDataTransform.SINGLE_CODEC);

		Assertions.assertNotNull(IDataExtractor.CODEC);
		Assertions.assertNotNull(IDataExtractor.MAPCODEC);
		Assertions.assertNotNull(IDataExtractor.MONOSTRING_DECODER);
		Assertions.assertNotNull(ItemComponentProperty.MONOSTRING_DECODER);
		Assertions.assertNotNull(MultiComponentFormatModule.SANITIZED_MONOSTRING_DECODER);

		Assertions.assertNotNull(ConditionList.MATCHALL_CODEC);
		Assertions.assertNotNull(ConditionList.MATCHANY_CODEC);
		Assertions.assertNotNull(MonostringConditionBuilder.MONOSTRINGMAP_CODEC);
		Assertions.assertNotNull(IItemPrecondition.CANONICAL_MAPCODEC);
		Assertions.assertNotNull(IItemPrecondition.CODEC);

		Assertions.assertNotNull(GroupModule.Unbaked.MAPCODEC);
	}
}
