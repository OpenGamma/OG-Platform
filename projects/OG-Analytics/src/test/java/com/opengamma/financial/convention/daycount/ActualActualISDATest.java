/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;


/**
 * Test ActualActualISDA.
 */
public class ActualActualISDATest extends DayCountTestCase {

  private static final ActualActualISDA DC = new ActualActualISDA();

  @Override
  protected DayCount getDayCount() {
    return DC;
  }

  @Test
  public void test() {
    assertEquals(COUPON * DC.getDayCountFraction(D1, D2), DC.getAccruedInterest(D1, D2, D3, COUPON, PAYMENTS), 0);
    assertEquals(DC.getConventionName(), "Actual/Actual ISDA");
  }

}
