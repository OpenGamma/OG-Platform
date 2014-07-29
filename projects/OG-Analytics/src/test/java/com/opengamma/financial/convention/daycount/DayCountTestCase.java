/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test DayCount.
 */
@Test(groups = TestGroup.UNIT)
public abstract class DayCountTestCase {
  protected static final ZonedDateTime D1 = DateUtils.getUTCDate(2010, 1, 1);
  protected static final ZonedDateTime D2 = DateUtils.getUTCDate(2010, 4, 1);
  protected static final ZonedDateTime D3 = DateUtils.getUTCDate(2010, 7, 1);
  protected static final LocalDate D4 = LocalDate.of(2010, 1, 1);
  protected static final LocalDate D5 = LocalDate.of(2010, 4, 1);
  protected static final LocalDate D6 = LocalDate.of(2010, 7, 1);
  protected static final double COUPON = 0.01;
  protected static final int PAYMENTS = 4;

  protected abstract DayCount getDayCount();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFirstDate1() {
    getDayCount().getDayCountFraction(null, D2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecondDate1() {
    getDayCount().getDayCountFraction(D1, null);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongOrder1() {
    getDayCount().getDayCountFraction(D2, D1);
  }

  @Test
  public void testNoAccruedInterest1() {
    assertEquals(getDayCount().getAccruedInterest(D1, D1, D3, COUPON, PAYMENTS), 0, 0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFirstDate2() {
    getDayCount().getDayCountFraction(null, D5);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecondDate2() {
    getDayCount().getDayCountFraction(D4, null);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongOrder2() {
    getDayCount().getDayCountFraction(D5, D4);
  }

  @Test
  public void testNoAccruedInterest2() {
    assertEquals(getDayCount().getAccruedInterest(D4, D4, D6, COUPON, PAYMENTS), 0, 0);
  }
}
