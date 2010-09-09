/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import static org.junit.Assert.fail;

import javax.time.calendar.LocalDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.fudge.OpenGammaFudgeContext;
public class ExpiryTest {
  private static final Logger s_logger = LoggerFactory.getLogger(ExpiryTest.class);  
  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();

  @Test
  public void testFudgeMessage () {
    final Expiry expiry = new Expiry(ZonedDateTime.of(LocalDateTime.ofMidnight(2010, 7, 1), TimeZone.UTC),
        ExpiryAccuracy.MONTH_YEAR);    
    final FudgeSerializationContext context = new FudgeSerializationContext(s_fudgeContext);
    FudgeFieldContainer msg = context.objectToFudgeMsg(expiry);
    s_logger.debug("Expiry {}", expiry);
    s_logger.debug("Encoded to {}", msg);
    final byte[] bytes = s_fudgeContext.toByteArray(msg);
    msg = s_fudgeContext.deserialize(bytes).getMessage();
    s_logger.debug("Serialised to {}", msg);
    final Expiry decoded = s_fudgeContext.fromFudgeMsg(Expiry.class, msg);
    s_logger.debug("Decoded to {}", decoded);
    if (!expiry.equals(decoded)) {
      s_logger.warn("Expected {}", expiry);
      s_logger.warn("Received {}", decoded);
      fail();
    }
    
  }

}
