/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@SuppressWarnings("deprecation")
@Test(groups = TestGroup.UNIT)
public class TenorUtilsTest {

  @Test(expectedExceptions = IllegalStateException.class)
  public void testGetDaysInTenor() {
    assertEquals(1, TenorUtils.getDaysInTenor(Tenor.ONE_DAY), 0);
    assertEquals(2, TenorUtils.getDaysInTenor(Tenor.TWO_DAYS), 0);
    assertEquals(7, TenorUtils.getDaysInTenor(Tenor.ONE_WEEK), 0);
    // the next two tests show unintuitive behaviour, but testing to make sure any changes are noticed
    assertEquals(0, TenorUtils.getDaysInTenor(Tenor.ONE_MONTH), 0);
    assertEquals(0, TenorUtils.getDaysInTenor(Tenor.ONE_YEAR), 0);
    TenorUtils.getDaysInTenor(Tenor.SN); // no period in business day tenors
  }

  @Test
  public void testOffsetDate() {
    ZonedDateTime dateTime = DateUtils.getUTCDate(2013, 12, 31);
    assertEquals(DateUtils.getUTCDate(2012, 12, 31), TenorUtils.getDateWithTenorOffset(dateTime, Tenor.ONE_YEAR));
    assertEquals(DateUtils.getUTCDate(2013, 11, 30), TenorUtils.getDateWithTenorOffset(dateTime, Tenor.ONE_MONTH));
    assertEquals(DateUtils.getUTCDate(2013, 12, 30), TenorUtils.getDateWithTenorOffset(dateTime, Tenor.ONE_DAY));
    assertEquals(DateUtils.getUTCDate(2012, 12, 31), TenorUtils.getDateWithTenorOffset(DateUtils.getUTCDate(2013, 1, 1), Tenor.ONE_DAY));
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void testTenorsInTenor() {
    assertEquals(7, TenorUtils.getTenorsInTenor(Tenor.ONE_WEEK, Tenor.ONE_DAY), 0);
    assertEquals(3.5, TenorUtils.getTenorsInTenor(Tenor.ONE_WEEK, Tenor.TWO_DAYS), 0);
    assertEquals(7, TenorUtils.getTenorsInTenor(Tenor.THREE_WEEKS, Tenor.THREE_DAYS), 0);
    // the next two tests show unintuitive behaviour, but testing to make sure changes are noticed
    assertEquals(0, TenorUtils.getTenorsInTenor(Tenor.ONE_MONTH, Tenor.ONE_DAY), 0);
    assertEquals(0, TenorUtils.getTenorsInTenor(Tenor.ONE_YEAR, Tenor.ONE_DAY), 0);
    TenorUtils.getTenorsInTenor(Tenor.TWO_WEEKS, Tenor.ON); // no period in business day tenors
  }

}
