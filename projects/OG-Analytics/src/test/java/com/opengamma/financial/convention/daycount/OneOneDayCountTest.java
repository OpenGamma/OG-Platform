/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test OneOneDatCount.
 */
@Test(groups = TestGroup.UNIT)
public class OneOneDayCountTest {

  private static final ZonedDateTime D1 = DateUtils.getUTCDate(2010, 1, 1);
  private static final ZonedDateTime D2 = DateUtils.getUTCDate(2011, 1, 1);
  private static final OneOneDayCount DC = new OneOneDayCount();

  @Test
  public void test() {
    assertEquals(DC.getDayCountFraction(D1, D2), 1, 0);
    final double coupon = 0.04;
    final int paymentsPerYear = 4;
    assertEquals(DC.getAccruedInterest(D1, D2, D2, coupon, paymentsPerYear), coupon / paymentsPerYear, 0);
    assertEquals(DC.getName(), "1/1");
  }

}
