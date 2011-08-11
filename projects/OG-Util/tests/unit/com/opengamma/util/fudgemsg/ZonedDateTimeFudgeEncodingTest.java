/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import javax.time.calendar.LocalDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * Test ZonedDateTime Fudge.
 */
@Test
public class ZonedDateTimeFudgeEncodingTest {

  private static final Logger s_logger = LoggerFactory.getLogger(ZonedDateTimeFudgeEncodingTest.class);
  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();

  public void test() {
    ZonedDateTime zdtUTC = ZonedDateTime.of(LocalDateTime.ofMidnight(2010, 7, 1), TimeZone.UTC);
    testFudgeMessage(zdtUTC);
    ZonedDateTime zdtPST = ZonedDateTime.ofInstant(zdtUTC.toInstant(), TimeZone.of("America/New_York"));
    assertTrue(zdtUTC.equalInstant(zdtPST));
    testFudgeMessage(zdtPST);
  }

  private void testFudgeMessage(final ZonedDateTime zonedDateTime) {
    final FudgeSerializer serializer = new FudgeSerializer(s_fudgeContext);
    FudgeMsg msg = serializer.objectToFudgeMsg(zonedDateTime);
    s_logger.debug("ZonedDateTime {}", zonedDateTime);
    s_logger.debug("Encoded to {}", msg);
    final byte[] bytes = s_fudgeContext.toByteArray(msg);
    msg = s_fudgeContext.deserialize(bytes).getMessage();
    s_logger.debug("Serialised to {}", msg);
    final ZonedDateTime decoded = s_fudgeContext.fromFudgeMsg(ZonedDateTime.class, msg);
    s_logger.debug("Decoded to {}", decoded);
    if (!zonedDateTime.equals(decoded)) {
      s_logger.warn("Expected {}", zonedDateTime);
      s_logger.warn("Received {}", decoded);
      fail();
    }
  }

}
