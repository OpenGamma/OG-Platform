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

import com.opengamma.financial.analytics.securityconverters.StubType;
import com.opengamma.util.time.DateUtil;

/**
 * Test ActualActualICMA.
 */
public class ActualActualICMATest {

  protected static final ZonedDateTime D1 = DateUtil.getUTCDate(2010, 1, 1);
  protected static final ZonedDateTime D2 = DateUtil.getUTCDate(2010, 4, 1);
  protected static final ZonedDateTime D3 = DateUtil.getUTCDate(2010, 7, 1);
  protected static final double COUPON = 0.01;
  protected static final int PAYMENTS = 4;
  private static final ActualActualICMA DC1 = new ActualActualICMA();
  private static final ActualActualICMANormal DC2 = new ActualActualICMANormal();

  @Test(expected = IllegalArgumentException.class)
  public void testNullFirstDate1() {
    DC1.getAccruedInterest(null, D2, D3, COUPON, PAYMENTS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSecondDate1() {
    DC1.getAccruedInterest(D1, null, D3, COUPON, PAYMENTS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullThirdDate1() {
    DC1.getAccruedInterest(D1, D2, null, COUPON, PAYMENTS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongOrder1() {
    DC1.getAccruedInterest(D2, D1, D3, COUPON, PAYMENTS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongOrder2() {
    DC1.getAccruedInterest(D1, D3, D2, COUPON, PAYMENTS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullStubType1() {
    DC1.getAccruedInterest(D1, D2, D3, COUPON, PAYMENTS, null);
  }

  @Test(expected = NotImplementedException.class)
  public void testGetDayCount1() {
    DC1.getDayCountFraction(D1, D2);
  }

  @Test
  public void testNoAccruedInterest() {
    assertEquals(DC1.getAccruedInterest(D1, D1, D3, COUPON, PAYMENTS), 0, 0);
  }

  @Test
  public void test1() {
    assertEquals(DC1.getConventionName(), "Actual/Actual ICMA");
    assertEquals(DC1.getAccruedInterest(D1, D2, D3, COUPON, PAYMENTS), DC1.getAccruedInterest(D1, D2, D3, COUPON, PAYMENTS, StubType.NONE), 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFirstDate2() {
    DC2.getAccruedInterest(null, D2, D3, COUPON, PAYMENTS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSecondDate2() {
    DC2.getAccruedInterest(D1, null, D3, COUPON, PAYMENTS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullThirdDate2() {
    DC2.getAccruedInterest(D1, D2, null, COUPON, PAYMENTS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongOrder3() {
    DC2.getAccruedInterest(D2, D1, D3, COUPON, PAYMENTS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongOrder4() {
    DC2.getAccruedInterest(D1, D3, D2, COUPON, PAYMENTS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullStubType2() {
    DC2.getAccruedInterest(D1, D2, D3, COUPON, PAYMENTS, null);
  }

  @Test(expected = NotImplementedException.class)
  public void testGetDayCount2() {
    DC2.getDayCountFraction(D1, D2);
  }

  @Test
  public void testNoAccruedInterest2() {
    assertEquals(DC2.getAccruedInterest(D1, D1, D3, COUPON, PAYMENTS), 0, 0);
  }

  @Test
  public void test2() {
    assertEquals(DC2.getConventionName(), "Actual/Actual ICMA Normal");
    assertEquals(DC2.getAccruedInterest(D1, D2, D3, COUPON, PAYMENTS), DC1.getAccruedInterest(D1, D2, D3, COUPON, PAYMENTS, StubType.NONE), 0);
  }
}
