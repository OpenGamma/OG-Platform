/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.OffsetDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZoneOffset;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;

/**
 * Test Fudge encoding.
 */
@Test
public class FlexiDateTimeFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  public void test_LD() {
    FlexiDateTime ld = FlexiDateTime.of(LocalDate.of(2010, 7, 1));
    assertEncodeDecodeCycle(FlexiDateTime.class, ld);
  }

  public void test_LDT() {
    FlexiDateTime ldt = FlexiDateTime.of(LocalDateTime.of(2010, 7, 1, 13, 0, 0));
    assertEncodeDecodeCycle(FlexiDateTime.class, ldt);
  }

  public void test_ODT() {
    FlexiDateTime odt = FlexiDateTime.of(OffsetDateTime.of(2010, 7, 1, 13, 0, 0, ZoneOffset.ofHours(3)));
    assertEncodeDecodeCycle(FlexiDateTime.class, odt);
  }

  public void test_ZDT_UTC() {
    FlexiDateTime zdtUTC = FlexiDateTime.of(ZonedDateTime.of(2010, 7, 1, 13, 0, 0, 0, TimeZone.UTC));
    assertEncodeDecodeCycle(FlexiDateTime.class, zdtUTC);
  }

  public void test_ZDT_newYork() {
    FlexiDateTime zdtPST = FlexiDateTime.of(ZonedDateTime.of(2010, 7, 1, 13, 0, 0, 0, TimeZone.of("America/New_York")));
    assertEncodeDecodeCycle(FlexiDateTime.class, zdtPST);
  }

}
