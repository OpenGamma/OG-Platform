/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Test the FudgeXXXProducer and FudgeXXXConsumer pairs by cycling
 * a Fudge message through them.
 */
@Test
public class FudgeProviders {

  private void testBeans(final MessageBodyWriter<FudgeMsgEnvelope> producer, final MessageBodyReader<FudgeMsgEnvelope> consumer) {
    final MutableFudgeFieldContainer msgIn = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msgIn.add("foo", "bar");
    msgIn.add("number", 42);
    final FudgeMsgEnvelope msgInEnv = new FudgeMsgEnvelope(msgIn, 0, 0);
    assertTrue(producer.isWriteable(msgInEnv.getClass(), null, null, null));
    final long predictedSize = producer.getSize(msgInEnv, null, null, null, null);
    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      producer.writeTo(msgInEnv, msgInEnv.getClass(), null, null, null, null, bos);
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("ioexception", e);
    }
    final byte[] data = bos.toByteArray();
    if (predictedSize != -1) {
      assertEquals(predictedSize, data.length);
    }
    assertTrue(consumer.isReadable(FudgeMsgEnvelope.class, null, null, null));
    final ByteArrayInputStream bis = new ByteArrayInputStream(data);
    final FudgeFieldContainer msgOut;
    try {
      final FudgeMsgEnvelope env = consumer.readFrom(FudgeMsgEnvelope.class, null, null, null, null, bis);
      assertNotNull(env);
      msgOut = env.getMessage();
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("ioexception", e);
    }
    assertNotNull(msgOut);
    assertEquals("bar", msgOut.getFieldValue(String.class, msgOut.getByName("foo")));
    assertEquals((Integer) 42, msgOut.getFieldValue(Integer.class, msgOut.getByName("number")));
  }

  public void testBinary() {
    testBeans(new FudgeBinaryProducer(), new FudgeBinaryConsumer());
  }

  public void testJSON() {
    testBeans(new FudgeJSONProducer(), new FudgeJSONConsumer());
  }

  @Test(enabled = false)
  public void testXML() {
    testBeans(new FudgeXMLProducer(), new FudgeXMLConsumer());
  }

}
