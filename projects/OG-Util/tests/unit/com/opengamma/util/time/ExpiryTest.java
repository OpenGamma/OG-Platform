/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.LocalDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.junit.Test;
public class ExpiryTest {

  private static final FudgeContext s_fudgeContext = new FudgeContext();

  static {
    s_fudgeContext.getTypeDictionary().addType(ExpiryBuilder.SECONDARY_TYPE_INSTANCE);
    s_fudgeContext.getObjectDictionary().addBuilder(Expiry.class, new ExpiryBuilder());
  }

  private static FudgeFieldContainer cycleMessage(final FudgeFieldContainer message) {
    final byte[] encoded = s_fudgeContext.toByteArray(message);
    return s_fudgeContext.deserialize(encoded).getMessage();
  }

  private static void testExpiry(final Expiry expiry) {
    final FudgeSerializationContext serContext = new FudgeSerializationContext(s_fudgeContext);
    final MutableFudgeFieldContainer messageIn = serContext.newMessage();
    serContext.objectToFudgeMsg(messageIn, "test", null, expiry);
    final FudgeFieldContainer messageOut = cycleMessage(messageIn);
    final FudgeDeserializationContext dsrContext = new FudgeDeserializationContext(s_fudgeContext);
    final Expiry result = dsrContext.fieldValueToObject(Expiry.class, messageOut.getByName("test"));
    assertEquals(expiry, result);
    assertEquals(expiry.getExpiry().getZone(), result.getExpiry().getZone());
  }

  @Test
  public void testFudgeMessageUTC() {
    for (ExpiryAccuracy accuracy : ExpiryAccuracy.values()) {
      testExpiry(new Expiry(ZonedDateTime.of(LocalDateTime.nowSystemClock(), TimeZone.UTC), accuracy));
    }
  }

  @Test
  public void testFudgeMessageNonUTC() {
    for (ExpiryAccuracy accuracy : ExpiryAccuracy.values()) {
      testExpiry(new Expiry(ZonedDateTime.of(LocalDateTime.nowSystemClock(), TimeZone.of("GMT+02:00")), accuracy));
    }
  }

}
