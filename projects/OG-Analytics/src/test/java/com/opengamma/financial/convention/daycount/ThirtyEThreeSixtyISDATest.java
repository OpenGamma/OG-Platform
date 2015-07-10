/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import static org.testng.AssertJUnit.assertEquals;

import org.apache.commons.lang.NotImplementedException;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test ThirtyEThreeSixtyISDA.
 */
@Test(groups = TestGroup.UNIT)
public class ThirtyEThreeSixtyISDATest {
  private static final ThirtyEThreeSixtyISDA DC = new ThirtyEThreeSixtyISDA();
  protected static final ZonedDateTime D1 = DateUtils.getUTCDate(2010, 1, 1);
  protected static final ZonedDateTime D2 = DateUtils.getUTCDate(2010, 4, 1);
  protected static final ZonedDateTime D3 = DateUtils.getUTCDate(2010, 7, 1);
  protected static final LocalDate D4 = LocalDate.of(2010, 1, 1);
  protected static final LocalDate D5 = LocalDate.of(2010, 4, 1);
  protected static final LocalDate D6 = LocalDate.of(2010, 7, 1);
  protected static final double COUPON = 0.01;
  protected static final int PAYMENTS = 4;

  @Test(expectedExceptions = NotImplementedException.class)
  public void testNoMaturityValue1() {
    DC.getAccruedInterest(D1, D2, D3, COUPON, PAYMENTS);
  }

  @Test(expectedExceptions = NotImplementedException.class)
  public void testNoMaturityValue2() {
    DC.getDayCountFraction(D1, D2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFirstDate1() {
    DC.getDayCountFraction(null, D2, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecondDate1() {
    DC.getDayCountFraction(D1, null, true);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongOrder1() {
    DC.getDayCountFraction(D2, D1, true);
  }

  @Test
  public void testNoAccruedInterest1() {
    assertEquals(DC.getAccruedInterest(D1, D1, COUPON, true), 0, 0);
    assertEquals(DC.getName(), "30E/360 ISDA");
  }

  @Test(expectedExceptions = NotImplementedException.class)
  public void testNoMaturityValue3() {
    DC.getAccruedInterest(D4, D5, D6, COUPON, PAYMENTS);
  }

  @Test(expectedExceptions = NotImplementedException.class)
  public void testNoMaturityValue4() {
    DC.getDayCountFraction(D4, D5);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFirstDate2() {
    DC.getDayCountFraction(null, D5, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecondDate2() {
    DC.getDayCountFraction(D4, null, true);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongOrder2() {
    DC.getDayCountFraction(D5, D4, true);
  }

  @Test
  public void testNoAccruedInterest2() {
    assertEquals(DC.getAccruedInterest(D4, D4, COUPON, true), 0, 0);
    assertEquals(DC.getName(), "30E/360 ISDA");
  }
}
