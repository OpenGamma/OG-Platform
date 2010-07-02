/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.junit.Test;
public class ExpiryTest {

  @Test
  public void testFudgeMessage () {
    final Expiry expiry = new Expiry (ZonedDateTime.nowSystemClock (), ExpiryAccuracy.MONTH_YEAR);
    final FudgeMsgEnvelope env = FudgeContext.GLOBAL_DEFAULT.toFudgeMsg(expiry);
    final Expiry result = FudgeContext.GLOBAL_DEFAULT.fromFudgeMsg (Expiry.class, env.getMessage ());
    assertEquals(expiry, result);
  }
}
