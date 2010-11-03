/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.NotImplementedException;
import org.junit.Test;

import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class ActualThreeSixtyFiveLongTest {
  protected static final ZonedDateTime D1 = DateUtil.getUTCDate(2010, 1, 1);
  protected static final ZonedDateTime D2 = DateUtil.getUTCDate(2010, 4, 1);
  protected static final ZonedDateTime D3 = DateUtil.getUTCDate(2010, 7, 1);
  protected static final double COUPON = 0.01;
  protected static final int PAYMENTS = 4;
  private static final ActualThreeSixtyFiveLong DC = new ActualThreeSixtyFiveLong();

  @Test(expected = IllegalArgumentException.class)
  public void testNullFirstDate() {
    DC.getAccruedInterest(null, D2, D3, COUPON, PAYMENTS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSecondDate() {
    DC.getAccruedInterest(D1, null, D3, COUPON, PAYMENTS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullThirdDate() {
    DC.getAccruedInterest(D1, D2, null, COUPON, PAYMENTS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongOrder1() {
    DC.getAccruedInterest(D2, D1, D3, COUPON, PAYMENTS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongOrder2() {
    DC.getAccruedInterest(D1, D3, D2, COUPON, PAYMENTS);
  }

  @Test(expected = NotImplementedException.class)
  public void testGetDayCount() {
    DC.getDayCountFraction(D1, D2);
  }

  @Test
  public void testNoAccruedInterest() {
    assertEquals(DC.getAccruedInterest(D1, D1, D3, COUPON, PAYMENTS), 0, 0);
  }

  @Test
  public void test() {
    assertEquals(DC.getConventionName(), "Actual/365L");
  }
}
