/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.Test;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding.
 */
@Test(groups = TestGroup.UNIT)
public class ExternalIdFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  public void test_builder() {
    FudgeContext context = new FudgeContext();
    context.getObjectDictionary().addBuilder(ExternalId.class, new ExternalIdFudgeBuilder());
    setContext(context);
    ExternalId object = ExternalId.of("A", "B");
    assertEncodeDecodeCycle(ExternalId.class, object);
  }

  public void test_secondaryType() {
    FudgeContext context = new FudgeContext();
    context.getTypeDictionary().addType(ExternalIdFudgeSecondaryType.INSTANCE);
    setContext(context);
    ExternalId object = ExternalId.of("A", "B");
    assertEncodeDecodeCycle(ExternalId.class, object);
  }

  public void test_toFudgeMsg() {
    ExternalId sample = ExternalId.of("A", "B");
    assertNull(ExternalIdFudgeBuilder.toFudgeMsg(new FudgeSerializer(OpenGammaFudgeContext.getInstance()), null));
    assertNotNull(ExternalIdFudgeBuilder.toFudgeMsg(new FudgeSerializer(OpenGammaFudgeContext.getInstance()), sample));
  }

  public void test_fromFudgeMsg() {
    assertNull(ExternalIdFudgeBuilder.fromFudgeMsg(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), null));
  }

}
