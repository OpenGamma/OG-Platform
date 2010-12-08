/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.util.time.DateUtil;

/**
 * Test DayCount.
 */
public abstract class DayCountTestCase {

  protected static final ZonedDateTime D1 = DateUtil.getUTCDate(2010, 1, 1);
  protected static final ZonedDateTime D2 = DateUtil.getUTCDate(2010, 4, 1);
  protected static final ZonedDateTime D3 = DateUtil.getUTCDate(2010, 7, 1);
  protected static final double COUPON = 0.01;
  protected static final int PAYMENTS = 4;

  protected abstract DayCount getDayCount();

  @Test(expected = IllegalArgumentException.class)
  public void testNullFirstDate() {
    getDayCount().getDayCountFraction(null, D2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSecondDate() {
    getDayCount().getDayCountFraction(D1, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongOrder() {
    getDayCount().getDayCountFraction(D2, D1);
  }

  @Test
  public void testNoAccruedInterest() {
    assertEquals(getDayCount().getAccruedInterest(D1, D1, D3, COUPON, PAYMENTS), 0, 0);
  }

}
