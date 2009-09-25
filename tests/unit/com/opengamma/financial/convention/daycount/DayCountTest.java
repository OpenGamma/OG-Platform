/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.util.time.DateUtil;

/**
 * 
 * @author emcleod
 */

public class DayCountTest {
  private static final double EPS = 1e-9;

  @Test
  public void testOneOne() {
    final DayCount daycount = new OneOneDayCount();
    try {
      daycount.getBasis(null);
      fail();
    } catch (final UnsupportedOperationException e) {
      // Expected
    }
    assertEquals(daycount.getDayCountFraction(DateUtil.getUTCDate(2009, 1, 1), DateUtil.getUTCDate(2009, 1, 1)), 1, EPS);
  }

  @Test
  public void testThirtyThreeSixty() {
    final DayCount dayCount = new ThirtyThreeSixtyDayCount();
    ZonedDateTime d1 = DateUtil.getUTCDate(2002, 1, 1);
    ZonedDateTime d2 = DateUtil.getUTCDate(2003, 1, 1);
    assertFractionEquals(dayCount, d1, d2, 1);
    d1 = DateUtil.getUTCDate(2004, 1, 1);
    d2 = DateUtil.getUTCDate(2005, 1, 1);
    assertFractionEquals(dayCount, d1, d2, 1);
    d1 = DateUtil.getUTCDate(2009, 1, 1);
    d2 = DateUtil.getUTCDate(2009, 2, 1);
    final double monthFraction = 30. / 360;
    assertFractionEquals(dayCount, d1, d2, monthFraction);
    d1 = DateUtil.getUTCDate(2008, 2, 1);
    d2 = DateUtil.getUTCDate(2008, 6, 1);
    assertFractionEquals(dayCount, d1, d2, 4 * monthFraction);
  }

  private void assertFractionEquals(final DayCount convention, final ZonedDateTime d1, final ZonedDateTime d2, final double frac) {
    assertEquals(convention.getDayCountFraction(d1, d2), frac, EPS);
  }
}
