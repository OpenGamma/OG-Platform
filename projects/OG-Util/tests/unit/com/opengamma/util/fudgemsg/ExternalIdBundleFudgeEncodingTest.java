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
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.AbstractBuilderTestCase;

/**
 * Test Fudge encoding.
 */
@Test
public class ExternalIdBundleFudgeEncodingTest extends AbstractBuilderTestCase {

  public void test() {
    ExternalIdBundle object = ExternalIdBundle.of(
        ExternalId.of("id1", "value1"),
        ExternalId.of("id2", "value2"));
    assertEncodeDecodeCycle(ExternalIdBundle.class, object);
  }

  //-------------------------------------------------------------------------
  public void fudgeEncoding() {
    ExternalIdBundle object = ExternalIdBundle.of(
        ExternalId.of("id1", "value1"),
        ExternalId.of("id2", "value2"));
    FudgeContext context = new FudgeContext();
    FudgeSerializer serializer = new FudgeSerializer(context);
    MutableFudgeMsg msg = serializer.newMessage();
    object.toFudgeMsg(serializer, msg);
    assertNotNull(msg);
    assertEquals(2, msg.getNumFields());
    ExternalIdBundle decoded = ExternalIdBundle.fromFudgeMsg(new FudgeDeserializer(context), msg);
    assertEquals(object, decoded);
  }

}
