/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.fra.definition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * 
 */
public class ForwardRateAgreementTest {
  public static final String CURVE_NAME = "test";
  public static final double fWD_RATE = 0.05;

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeSettlement() {
    new ForwardRateAgreement(-2, 2, fWD_RATE, CURVE_NAME, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeMaturity() {
    new ForwardRateAgreement(2, -2, fWD_RATE, CURVE_NAME, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSettlementAfterMaturity() {
    new ForwardRateAgreement(3, 2, fWD_RATE, CURVE_NAME, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFixingDateAfterSettlement() {
    new ForwardRateAgreement(2, 3, 2.1, 1.0, 1.0, fWD_RATE, CURVE_NAME, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeYearFraction() {
    new ForwardRateAgreement(2, 3, 2, -1.0, 1.0, fWD_RATE, CURVE_NAME, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeYearFraction2() {
    new ForwardRateAgreement(2, 3, 2.0, 1.0, -1.0, fWD_RATE, CURVE_NAME, CURVE_NAME);
  }

  @Test
  public void test() {
    final double start = 0.5;
    final double end = 0.75;
    final ForwardRateAgreement fra = new ForwardRateAgreement(start, end, fWD_RATE, CURVE_NAME, CURVE_NAME);
    assertEquals(fra.getSettlementDate(), start, 0);
    assertEquals(fra.getMaturity(), end, 0);
    assertEquals(fra.getFixingDate(), start, 0);
    assertEquals(fra.getDiscountingYearFraction(), end - start, 0);
    assertEquals(fra.getForwardYearFraction(), end - start, 0);

    ForwardRateAgreement other = new ForwardRateAgreement(start, end, fWD_RATE, CURVE_NAME, CURVE_NAME);
    assertEquals(fra, other);
    assertEquals(fra.hashCode(), other.hashCode());
    other = new ForwardRateAgreement(start, end + 0.01, fWD_RATE, CURVE_NAME, CURVE_NAME);
    assertFalse(other.equals(fra));
    other = new ForwardRateAgreement(start + 0.01, end, fWD_RATE, CURVE_NAME, CURVE_NAME);
    assertFalse(other.equals(fra));
    other = new ForwardRateAgreement(start, end, fWD_RATE + 0.01, CURVE_NAME, CURVE_NAME);
    assertFalse(other.equals(fra));
    other = new ForwardRateAgreement(start, end, fWD_RATE, "", CURVE_NAME);
    assertFalse(other.equals(fra));
    other = new ForwardRateAgreement(start, end, fWD_RATE, CURVE_NAME, "");
    assertFalse(other.equals(fra));
    other = new ForwardRateAgreement(start, end, start, end - start, end - start, fWD_RATE, CURVE_NAME, CURVE_NAME);
    assertEquals(fra, other);
    other = new ForwardRateAgreement(start, end, start - 0.01, end - start, end - start, fWD_RATE, CURVE_NAME, CURVE_NAME);
    assertFalse(other.equals(fra));
    other = new ForwardRateAgreement(start, end, start, 0.24, end - start, fWD_RATE, CURVE_NAME, CURVE_NAME);
    assertFalse(other.equals(fra));
    other = new ForwardRateAgreement(start, end, start, end - start, 0.24, fWD_RATE, CURVE_NAME, CURVE_NAME);
    assertFalse(other.equals(fra));
  }
}
