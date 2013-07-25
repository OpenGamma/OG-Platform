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
public class ObjectIdFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  public void test_builder() {
    FudgeContext context = new FudgeContext();
    context.getObjectDictionary().addBuilder(ObjectId.class, new ObjectIdFudgeBuilder());
    setContext(context);
    ObjectId object = ObjectId.of("A", "B");
    assertEncodeDecodeCycle(ObjectId.class, object);
  }

  public void test_secondaryType() {
    FudgeContext context = new FudgeContext();
    context.getTypeDictionary().addType(ObjectIdFudgeSecondaryType.INSTANCE);
    setContext(context);
    ObjectId object = ObjectId.of("A", "B");
    assertEncodeDecodeCycle(ObjectId.class, object);
  }

  public void test_toFudgeMsg() {
    ObjectId sample = ObjectId.of("A", "B");
    assertNull(ObjectIdFudgeBuilder.toFudgeMsg(new FudgeSerializer(OpenGammaFudgeContext.getInstance()), null));
    assertNotNull(ObjectIdFudgeBuilder.toFudgeMsg(new FudgeSerializer(OpenGammaFudgeContext.getInstance()), sample));
  }

  public void test_fromFudgeMsg() {
    assertNull(ObjectIdFudgeBuilder.fromFudgeMsg(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), null));
  }

  @Test(expectedExceptions = RuntimeException.class)
  public void test_fromFudgeMsg_empty() {
    FudgeMsg msg = getFudgeContext().newMessage();
    assertNull(ObjectIdFudgeBuilder.fromFudgeMsg(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), msg));
  }

}
