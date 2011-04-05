/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.LocalDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.testng.annotations.Test;

import com.opengamma.util.fudgemsg.ExpiryBuilder;

/**
 * Test Expiry.
 */
@Test
public class ExpiryTest {

  private static final FudgeContext s_fudgeContext = new FudgeContext();

  static {
    s_fudgeContext.getTypeDictionary().addType(ExpiryBuilder.SECONDARY_TYPE_INSTANCE);
    s_fudgeContext.getObjectDictionary().addBuilder(Expiry.class, new ExpiryBuilder());
  }

  private static FudgeMsg cycleMessage(final FudgeMsg message) {
    final byte[] encoded = s_fudgeContext.toByteArray(message);
    return s_fudgeContext.deserialize(encoded).getMessage();
  }

  private static void testExpiry(final Expiry expiry) {
    final FudgeSerializationContext serContext = new FudgeSerializationContext(s_fudgeContext);
    final MutableFudgeMsg messageIn = serContext.newMessage();
    serContext.addToMessage(messageIn, "test", null, expiry);
    final FudgeMsg messageOut = cycleMessage(messageIn);
    final FudgeDeserializationContext dsrContext = new FudgeDeserializationContext(s_fudgeContext);
    final Expiry result = dsrContext.fieldValueToObject(Expiry.class, messageOut.getByName("test"));
    assertEquals(expiry, result);
    assertEquals(expiry.getExpiry().getZone(), result.getExpiry().getZone());
  }

  public void testFudgeMessageUTC() {
    for (ExpiryAccuracy accuracy : ExpiryAccuracy.values()) {
      testExpiry(new Expiry(ZonedDateTime.of(LocalDateTime.now(), TimeZone.UTC), accuracy));
    }
  }

  public void testFudgeMessageNonUTC() {
    for (ExpiryAccuracy accuracy : ExpiryAccuracy.values()) {
      testExpiry(new Expiry(ZonedDateTime.of(LocalDateTime.now(), TimeZone.of("GMT+02:00")), accuracy));
    }
  }

}
