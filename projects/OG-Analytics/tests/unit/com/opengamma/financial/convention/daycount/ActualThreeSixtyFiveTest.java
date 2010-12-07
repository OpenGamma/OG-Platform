/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


/**
 * Test ActualThreeSixtyFive.
 */
public class ActualThreeSixtyFiveTest extends DayCountTestCase {

  private static final ActualThreeSixtyFive DC = new ActualThreeSixtyFive();

  @Override
  protected DayCount getDayCount() {
    return DC;
  }

  @Test
  public void test() {
    assertEquals(COUPON * DC.getDayCountFraction(D1, D2), DC.getAccruedInterest(D1, D2, D3, COUPON, PAYMENTS), 0);
    assertEquals(DC.getConventionName(), "Actual/365");
  }

}
