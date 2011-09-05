/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import static org.testng.AssertJUnit.assertTrue;

import javax.time.calendar.LocalDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractBuilderTestCase;

/**
 * Test Fudge encoding.
 */
@Test
public class ZonedDateTimeFudgeEncodingTest extends AbstractBuilderTestCase {

  public void test_UTC() {
    ZonedDateTime zdtUTC = ZonedDateTime.of(LocalDateTime.ofMidnight(2010, 7, 1), TimeZone.UTC);
    assertEncodeDecodeCycle(ZonedDateTime.class, zdtUTC);
  }

  public void test_newYork() {
    ZonedDateTime zdtUTC = ZonedDateTime.of(LocalDateTime.ofMidnight(2010, 7, 1), TimeZone.UTC);
    ZonedDateTime zdtPST = ZonedDateTime.ofInstant(zdtUTC.toInstant(), TimeZone.of("America/New_York"));
    assertTrue(zdtUTC.equalInstant(zdtPST));
    assertEncodeDecodeCycle(ZonedDateTime.class, zdtPST);
  }

}
