/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class FlexiDateTimeTest {

  private static final LocalDate DATE = LocalDate.of(2010, 7, 1);
  private static final LocalTime TIME = LocalTime.of(12, 30);
  private static final LocalTime TIME2 = LocalTime.of(13, 40);
  private static final ZoneOffset OFFSET = ZoneOffset.ofHours(2);
  private static final ZoneId ZONE = ZoneId.of("America/New_York");
  private static final ZoneId ZONE2 = ZoneId.of("America/Los_Angeles");
  private static final LocalDate LOCAL_DATE = DATE;
  private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(DATE, TIME);
  private static final OffsetDateTime OFFSET_DATE_TIME = OffsetDateTime.of(DATE, TIME, OFFSET);
  private static final ZonedDateTime ZONED_DATE_TIME = ZonedDateTime.of(DATE.atTime(TIME), ZONE);

  public void test_LD() {
    FlexiDateTime test = FlexiDateTime.of(LOCAL_DATE);
    assertState(test, DATE, null, null);
  }

  public void test_LD_LT() {
    FlexiDateTime test = FlexiDateTime.of(LOCAL_DATE, TIME);
    assertState(test, DATE, TIME, null);
  }

  public void test_LDT() {
    FlexiDateTime test = FlexiDateTime.of(LOCAL_DATE_TIME);
    assertState(test, DATE, TIME, null);
  }

  public void test_ODT() {
    FlexiDateTime test = FlexiDateTime.of(OFFSET_DATE_TIME);
    assertState(test, DATE, TIME, OFFSET);
  }

  public void test_ZDT() {
    FlexiDateTime test = FlexiDateTime.of(ZONED_DATE_TIME);
    assertState(test, DATE, TIME, ZONE);
  }

  private void assertState(FlexiDateTime test, LocalDate expectedDate, LocalTime expectedTime, ZoneId expectedZone) {
    assertEquals(expectedDate, test.getDate());
    assertEquals(expectedTime, test.getTime());
    assertEquals(expectedZone, test.getZone());
    
    // toLocalDateTime() and toLocalDateTime(LocalTime)
    if (expectedTime != null) {
      assertEquals(expectedDate.atTime(expectedTime), test.toLocalDateTime());
      assertEquals(expectedDate.atTime(expectedTime), test.toLocalDateTime(TIME2));
    } else {
      try {
        test.toLocalDateTime();
        fail();
      } catch (RuntimeException ex) {
        // expected
      }
      assertEquals(expectedDate.atTime(TIME2), test.toLocalDateTime(TIME2));
    }
    
    // toZonedDateTime() and toZonedDateTime(LocalTime,TimeZone)
    if (expectedZone != null) {
      assertEquals(true, test.isComplete());
      assertEquals(expectedDate.atTime(expectedTime).atZone(expectedZone).toOffsetDateTime(), test.toOffsetDateTime());
      assertEquals(expectedDate.atTime(expectedTime).atZone(expectedZone).toOffsetDateTime().toOffsetTime(), test.toOffsetTime());
      assertEquals(expectedDate.atTime(expectedTime).atZone(expectedZone), test.toZonedDateTime());
      assertEquals(expectedDate.atTime(expectedTime).atZone(expectedZone), test.toZonedDateTime(TIME2, ZONE2));
    } else {
      assertEquals(false, test.isComplete());
      try {
        test.toOffsetDateTime();
        fail();
      } catch (RuntimeException ex) {
        // expected
      }
      try {
        test.toOffsetTime();
        fail();
      } catch (RuntimeException ex) {
        // expected
      }
      try {
        test.toZonedDateTime();
        fail();
      } catch (RuntimeException ex) {
        // expected
      }
      if (expectedTime != null) {
        assertEquals(expectedDate.atTime(expectedTime).atZone(ZONE2), test.toZonedDateTime(TIME2, ZONE2));
      } else {
        assertEquals(expectedDate.atTime(TIME2).atZone(ZONE2), test.toZonedDateTime(TIME2, ZONE2));
      }
    }
    
    // toBest()
    if (expectedTime != null) {
      if (expectedZone != null) {
        if (expectedZone instanceof ZoneOffset) {
          assertEquals(expectedDate.atTime(expectedTime).atOffset((ZoneOffset) expectedZone), test.toBest());
        } else {
          assertEquals(expectedDate.atTime(expectedTime).atZone(expectedZone), test.toBest());
        }
      } else {
        assertEquals(LocalDateTime.of(expectedDate, expectedTime), test.toBest());
      }
    } else {
      assertEquals(expectedDate, test.toBest());
    }
    
    // equals
    assertEquals(true, test.equals(test));
    assertEquals(true, test.equals(FlexiDateTime.ofLenient(test.getDate(), test.getTime(), test.getZone())));
    assertEquals(false, test.equals(""));
    assertEquals(false, test.equals(null));
    assertEquals(false, test.equals(FlexiDateTime.of(DATE.minusDays(1))));
    assertEquals(false, test.equals(FlexiDateTime.of(DATE.minusDays(1), TIME)));
    assertEquals(false, test.equals(FlexiDateTime.ofLenient(DATE.minusDays(1), TIME, ZONE)));
  }

}
