package com.opengamma.financial.convention.daycount;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

public class ActualThreeSixtyFiveTwoFiveTest extends DayCountTestCase {

  private static final ActualThreeSixtyFiveTwoFive DC = new ActualThreeSixtyFiveTwoFive();
  
  @Override
  protected DayCount getDayCount() {
    return DC;
  }

  @Test
  public void test() {
    assertEquals(COUPON * DC.getDayCountFraction(D1, D2), DC.getAccruedInterest(D1, D2, D3, COUPON, PAYMENTS), 0);
    assertEquals(DC.getName(), "Actual/365.25");
  }
}
