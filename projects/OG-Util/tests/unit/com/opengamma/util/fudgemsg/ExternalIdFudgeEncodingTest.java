/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.util.test.AbstractBuilderTestCase;

/**
 * Test Fudge encoding.
 */
@Test
public class ExternalIdFudgeEncodingTest extends AbstractBuilderTestCase {

  public void test() {
    ExternalId object = ExternalId.of("A", "B");
    assertEncodeDecodeCycle(ExternalId.class, object);
  }

  //-------------------------------------------------------------------------
  public void test_fudgeEncoding() {
    ExternalId object = ExternalId.of("id1", "value1");
    FudgeContext context = new FudgeContext();
    FudgeSerializer serializer = new FudgeSerializer(context);
    MutableFudgeMsg msg = serializer.newMessage();
    object.toFudgeMsg(serializer, msg);
    assertNotNull(msg);
    assertEquals(2, msg.getNumFields());
    ExternalId decoded = ExternalId.fromFudgeMsg(new FudgeDeserializer(context), msg);
    assertEquals(object, decoded);
  }

}
