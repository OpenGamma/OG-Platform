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

import com.opengamma.id.UniqueId;
import com.opengamma.util.test.AbstractBuilderTestCase;

/**
 * Test Fudge encoding.
 */
@Test
public class UniqueIdFudgeEncodingTest extends AbstractBuilderTestCase {

  public void test_simple() {
    UniqueId object = UniqueId.of("A", "B");
    assertEncodeDecodeCycle(UniqueId.class, object);
  }

  public void test_versioned() {
    UniqueId object = UniqueId.of("A", "B", "C");
    assertEncodeDecodeCycle(UniqueId.class, object);
  }

  //-------------------------------------------------------------------------
  public void test_fudgeEncoding() {
    UniqueId object = UniqueId.of("id1", "value1", "version1");
    FudgeContext context = new FudgeContext();
    FudgeSerializer serializer = new FudgeSerializer(context);
    MutableFudgeMsg msg = serializer.newMessage();
    object.toFudgeMsg(serializer, msg);
    assertNotNull(msg);
    assertEquals(3, msg.getNumFields());
    UniqueId decoded = UniqueId.fromFudgeMsg(new FudgeDeserializer(context), msg);
    assertEquals(object, decoded);
  }

}
