/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * 
 */
public class ActualActualAFBTest extends DayCountTestCase {
  private static final ActualActualAFB DC = new ActualActualAFB();

  @Override
  protected DayCount getDayCount() {
    return DC;
  }

  @Test
  public void test() {
    assertEquals(COUPON * DC.getDayCountFraction(D1, D2), DC.getAccruedInterest(D1, D2, D3, COUPON, PAYMENTS), 0);
    assertEquals(DC.getConventionName(), "Actual/Actual AFB");
  }
}
