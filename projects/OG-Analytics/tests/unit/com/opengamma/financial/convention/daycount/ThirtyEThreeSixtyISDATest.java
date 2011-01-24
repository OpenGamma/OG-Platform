/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
 * Test ThirtyEThreeSixtyISDA.
 */
public class ThirtyEThreeSixtyISDATest {

  private static final ThirtyEThreeSixtyISDA DC = new ThirtyEThreeSixtyISDA();
  protected static final ZonedDateTime D1 = DateUtil.getUTCDate(2010, 1, 1);
  protected static final ZonedDateTime D2 = DateUtil.getUTCDate(2010, 4, 1);
  protected static final ZonedDateTime D3 = DateUtil.getUTCDate(2010, 7, 1);
  protected static final double COUPON = 0.01;
  protected static final int PAYMENTS = 4;

  @Test(expected = NotImplementedException.class)
  public void testNoMaturityValue1() {
    DC.getAccruedInterest(D1, D2, D3, COUPON, PAYMENTS);
  }

  @Test(expected = NotImplementedException.class)
  public void testNoMaturityValue2() {
    DC.getDayCountFraction(D1, D2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFirstDate() {
    DC.getDayCountFraction(null, D2, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSecondDate() {
    DC.getDayCountFraction(D1, null, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongOrder() {
    DC.getDayCountFraction(D2, D1, true);
  }

  @Test
  public void testNoAccruedInterest() {
    assertEquals(DC.getAccruedInterest(D1, D1, COUPON, true), 0, 0);
    assertEquals(DC.getConventionName(), "30E/360 ISDA");
  }

}
