/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
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
public class UniqueIdFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  public void test_simple() {
    FudgeContext context = new FudgeContext();
    context.getObjectDictionary().addBuilder(UniqueId.class, new UniqueIdFudgeBuilder());
    setContext(context);
    UniqueId object = UniqueId.of("A", "B");
    assertEncodeDecodeCycle(UniqueId.class, object);
  }

  public void test_versioned() {
    FudgeContext context = new FudgeContext();
    context.getObjectDictionary().addBuilder(UniqueId.class, new UniqueIdFudgeBuilder());
    setContext(context);
    UniqueId object = UniqueId.of("A", "B", "C");
    assertEncodeDecodeCycle(UniqueId.class, object);
  }

  public void test_secondaryType() {
    FudgeContext context = new FudgeContext();
    context.getTypeDictionary().addType(UniqueIdFudgeSecondaryType.INSTANCE);
    setContext(context);
    UniqueId object = UniqueId.of("A", "B");
    assertEncodeDecodeCycle(UniqueId.class, object);
  }

  public void test_toFudgeMsg() {
    UniqueId sample = UniqueId.of("A", "B", "C");
    assertNull(UniqueIdFudgeBuilder.toFudgeMsg(new FudgeSerializer(OpenGammaFudgeContext.getInstance()), null));
    assertNotNull(UniqueIdFudgeBuilder.toFudgeMsg(new FudgeSerializer(OpenGammaFudgeContext.getInstance()), sample));
  }

  public void test_fromFudgeMsg() {
    assertNull(UniqueIdFudgeBuilder.fromFudgeMsg(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), null));
  }

  @Test(expectedExceptions = RuntimeException.class)
  public void test_fromFudgeMsg_empty() {
    FudgeMsg msg = getFudgeContext().newMessage();
    assertNull(UniqueIdFudgeBuilder.fromFudgeMsg(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), msg));
  }

}
