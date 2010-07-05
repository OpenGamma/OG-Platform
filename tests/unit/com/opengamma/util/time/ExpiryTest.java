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
    s_fudgeContext.getTypeDictionary().addType(ExpiryFieldType.INSTANCE);
  }

  private static FudgeFieldContainer cycleMessage(final FudgeFieldContainer message) {
    final byte[] encoded = s_fudgeContext.toByteArray(message);
    return s_fudgeContext.deserialize(encoded).getMessage();
  }

  @Test
  public void testFudgeMessage () {
    final Expiry expiry = new Expiry(ZonedDateTime.of(LocalDateTime.ofMidnight(2010, 7, 1), TimeZone.UTC),
        ExpiryAccuracy.MONTH_YEAR);
    final FudgeSerializationContext serContext = new FudgeSerializationContext(s_fudgeContext);
    final MutableFudgeFieldContainer messageIn = serContext.newMessage();
    serContext.objectToFudgeMsg(messageIn, "test", null, expiry);
    final FudgeFieldContainer messageOut = cycleMessage(messageIn);
    final FudgeDeserializationContext dsrContext = new FudgeDeserializationContext(s_fudgeContext);
    final Expiry result = dsrContext.fieldValueToObject(Expiry.class, messageOut.getByName("test"));
    assertEquals(expiry, result);
  }

}
