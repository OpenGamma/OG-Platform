/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.fudgemsg.WriteReplaceHelper;

/**
 * Base class for testing OG-Analytics objects to and from Fudge messages.
 */
public class AnalyticsTestBase {

  private static final Logger s_logger = LoggerFactory.getLogger(AnalyticsTestBase.class);

  @Deprecated
  protected FudgeContext getFudgeContext() {
    return OpenGammaFudgeContext.getInstance();
  }

  private FudgeMsg cycleMessage(final FudgeMsg message) {
    final byte[] data = OpenGammaFudgeContext.getInstance().toByteArray(message);
    s_logger.info("{} bytes", data.length);
    return OpenGammaFudgeContext.getInstance().deserialize(data).getMessage();
  }

  @SuppressWarnings("unchecked")
  protected <T> T cycleObject(final Class<T> clazz, final T object) {
    s_logger.info("object {}", object);
    final FudgeSerializer fudgeSerializationContext = new FudgeSerializer(getFudgeContext());
    final FudgeDeserializer fudgeDeserializationContext = new FudgeDeserializer(getFudgeContext());
    FudgeMsg message = fudgeSerializationContext.objectToFudgeMsg(WriteReplaceHelper.writeReplace(object));
    assertNotNull(message);
    s_logger.info("message {}", message);
    message = cycleMessage(message);
    s_logger.info("message {}", message);
    final Object newObject = fudgeDeserializationContext.fudgeMsgToObject(message);
    assertNotNull(newObject);
    s_logger.info("object {}", newObject);
    assertTrue(clazz.isAssignableFrom(newObject.getClass()));
    assertEquals(object.getClass(), newObject.getClass());
    return (T) newObject;
  }

}
