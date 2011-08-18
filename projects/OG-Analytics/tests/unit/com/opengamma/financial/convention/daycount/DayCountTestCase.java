/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.time.DateUtils;

/**
 * Test DayCount.
 */
public abstract class DayCountTestCase {

  protected static final ZonedDateTime D1 = DateUtils.getUTCDate(2010, 1, 1);
  protected static final ZonedDateTime D2 = DateUtils.getUTCDate(2010, 4, 1);
  protected static final ZonedDateTime D3 = DateUtils.getUTCDate(2010, 7, 1);
  protected static final double COUPON = 0.01;
  protected static final int PAYMENTS = 4;

  protected abstract DayCount getDayCount();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFirstDate() {
    getDayCount().getDayCountFraction(null, D2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecondDate() {
    getDayCount().getDayCountFraction(D1, null);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongOrder() {
    getDayCount().getDayCountFraction(D2, D1);
  }

  @Test
  public void testNoAccruedInterest() {
    assertEquals(getDayCount().getAccruedInterest(D1, D1, D3, COUPON, PAYMENTS), 0, 0);
  }

}
