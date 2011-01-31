/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
  private static final String CURVE_NAME1 = "test1";
  private static final String CURVE_NAME2 = "test2";
  private static final double FWD_RATE = 0.05;
  private static final double SETTLEMENT = 2;
  private static final double MATURITY = 2.51;
  private static final double FIXING_DATE = 0;
  private static final double FORWARD_YEAR_FRACTION = 0.5;
  private static final double DISCOUNT_YEAR_FRACTION = 0.51;

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeSettlement1() {
    new ForwardRateAgreement(-SETTLEMENT, 2, FWD_RATE, CURVE_NAME1, CURVE_NAME2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeSettlement2() {
    new ForwardRateAgreement(-SETTLEMENT, 2, FWD_RATE, FIXING_DATE, FORWARD_YEAR_FRACTION, DISCOUNT_YEAR_FRACTION, CURVE_NAME1, CURVE_NAME2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeMaturity1() {
    new ForwardRateAgreement(SETTLEMENT, -2, FWD_RATE, CURVE_NAME1, CURVE_NAME1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeMaturity2() {
    new ForwardRateAgreement(SETTLEMENT, -2, FWD_RATE, FIXING_DATE, FORWARD_YEAR_FRACTION, DISCOUNT_YEAR_FRACTION, CURVE_NAME1, CURVE_NAME1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSettlementAfterMaturity1() {
    new ForwardRateAgreement(SETTLEMENT + 1, 2, FWD_RATE, CURVE_NAME1, CURVE_NAME1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSettlementAfterMaturity2() {
    new ForwardRateAgreement(SETTLEMENT + 1, 2, FWD_RATE, FIXING_DATE, FORWARD_YEAR_FRACTION, DISCOUNT_YEAR_FRACTION, CURVE_NAME1, CURVE_NAME1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFixingDateAfterSettlement1() {
    new ForwardRateAgreement(SETTLEMENT, MATURITY, SETTLEMENT + 0.1, FORWARD_YEAR_FRACTION, DISCOUNT_YEAR_FRACTION, FWD_RATE, CURVE_NAME1, CURVE_NAME1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeYearFraction1() {
    new ForwardRateAgreement(SETTLEMENT, MATURITY, SETTLEMENT, -FORWARD_YEAR_FRACTION, DISCOUNT_YEAR_FRACTION, FWD_RATE, CURVE_NAME1, CURVE_NAME1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeYearFraction2() {
    new ForwardRateAgreement(SETTLEMENT, MATURITY, SETTLEMENT, FORWARD_YEAR_FRACTION, -DISCOUNT_YEAR_FRACTION, FWD_RATE, CURVE_NAME1, CURVE_NAME1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCurveName1() {
    new ForwardRateAgreement(SETTLEMENT, MATURITY, FWD_RATE, null, CURVE_NAME2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCurveName2() {
    new ForwardRateAgreement(SETTLEMENT, MATURITY, FWD_RATE, CURVE_NAME1, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCurveName3() {
    new ForwardRateAgreement(SETTLEMENT, MATURITY, FIXING_DATE, FORWARD_YEAR_FRACTION, DISCOUNT_YEAR_FRACTION, FWD_RATE, null, CURVE_NAME2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCurveName4() {
    new ForwardRateAgreement(SETTLEMENT, MATURITY, FIXING_DATE, FORWARD_YEAR_FRACTION, DISCOUNT_YEAR_FRACTION, FWD_RATE, CURVE_NAME1, null);
  }

  @Test
  public void test() {
    final double start = 0.5;
    final double end = 0.75;
    final ForwardRateAgreement fra = new ForwardRateAgreement(start, end, FWD_RATE, CURVE_NAME1, CURVE_NAME2);
    assertEquals(fra.getSettlementDate(), start, 0);
    assertEquals(fra.getMaturity(), end, 0);
    assertEquals(fra.getFixingDate(), start, 0);
    assertEquals(fra.getDiscountingYearFraction(), end - start, 0);
    assertEquals(fra.getForwardYearFraction(), end - start, 0);
    assertEquals(fra.getFundingCurveName(), CURVE_NAME1);
    assertEquals(fra.getIndexCurveName(), CURVE_NAME2);
    assertEquals(fra.getStrike(), FWD_RATE, 0);
    ForwardRateAgreement other = new ForwardRateAgreement(start, end, FWD_RATE, CURVE_NAME1, CURVE_NAME2);
    assertEquals(fra, other);
    assertEquals(fra.hashCode(), other.hashCode());
    other = new ForwardRateAgreement(start, end + 0.01, FWD_RATE, CURVE_NAME1, CURVE_NAME2);
    assertFalse(other.equals(fra));
    other = new ForwardRateAgreement(start + 0.01, end, FWD_RATE, CURVE_NAME1, CURVE_NAME2);
    assertFalse(other.equals(fra));
    other = new ForwardRateAgreement(start, end, FWD_RATE + 0.01, CURVE_NAME1, CURVE_NAME2);
    assertFalse(other.equals(fra));
    other = new ForwardRateAgreement(start, end, FWD_RATE, "", CURVE_NAME2);
    assertFalse(other.equals(fra));
    other = new ForwardRateAgreement(start, end, FWD_RATE, CURVE_NAME1, "");
    assertFalse(other.equals(fra));
    other = new ForwardRateAgreement(start, end, start, end - start, end - start, FWD_RATE, CURVE_NAME1, CURVE_NAME2);
    assertEquals(fra, other);
    assertEquals(fra.hashCode(), other.hashCode());
    other = new ForwardRateAgreement(start, end, start - 0.01, end - start, end - start, FWD_RATE, CURVE_NAME1, CURVE_NAME2);
    assertFalse(other.equals(fra));
    other = new ForwardRateAgreement(start, end, start, 0.24, end - start, FWD_RATE, CURVE_NAME1, CURVE_NAME2);
    assertFalse(other.equals(fra));
    other = new ForwardRateAgreement(start, end, start, end - start, 0.24, FWD_RATE, CURVE_NAME1, CURVE_NAME2);
    assertFalse(other.equals(fra));
  }
}
