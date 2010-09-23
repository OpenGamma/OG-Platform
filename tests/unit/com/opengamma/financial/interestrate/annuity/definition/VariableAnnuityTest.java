/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.definition;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * 
 */
public class VariableAnnuityTest {
  private static final double[] T = new double[] {1, 2, 3, 4};
  private static final double[] YEAR_FRACTIONS = new double[] {1, 1, 1, 1};
  private static final double[] INDEX_FIXING = new double[] {0, 1, 2, 3};
  private static final double[] INDEX_MATURITY = new double[] {1, 2, 3, 4};
  private static final double[] SPREADS = new double[] {0, 0, 0, 0};
  private static final double NOTIONAL = 1;
  private static final double INITIAL_RATE = 0.05;
  private static final String FUNDING = "Funding";
  private static final String LIBOR = "Libor";
  private static final VariableAnnuity ANNUITY1 = new VariableAnnuity(T, FUNDING, LIBOR);
  private static final VariableAnnuity ANNUITY2 = new VariableAnnuity(T, NOTIONAL, FUNDING, LIBOR);
  private static final VariableAnnuity ANNUITY3 = new VariableAnnuity(T, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, NOTIONAL, INITIAL_RATE, FUNDING, LIBOR);
  private static final VariableAnnuity ANNUITY4 = new VariableAnnuity(T, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, SPREADS, NOTIONAL, INITIAL_RATE, FUNDING, LIBOR);

  @Test(expected = IllegalArgumentException.class)
  public void testNullPaymentTimes1() {
    new VariableAnnuity(null, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullPaymentTimes2() {
    new VariableAnnuity(null, NOTIONAL, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullPaymentTimes3() {
    new VariableAnnuity(null, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, NOTIONAL, INITIAL_RATE, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullPaymentTimes4() {
    new VariableAnnuity(null, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, SPREADS, NOTIONAL, INITIAL_RATE, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFundingName1() {
    new VariableAnnuity(T, null, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFundingName2() {
    new VariableAnnuity(T, NOTIONAL, null, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFundingName3() {
    new VariableAnnuity(T, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, NOTIONAL, INITIAL_RATE, null, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFundingName4() {
    new VariableAnnuity(T, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, SPREADS, NOTIONAL, INITIAL_RATE, null, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullLiborName1() {
    new VariableAnnuity(T, FUNDING, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullLiborName2() {
    new VariableAnnuity(T, NOTIONAL, FUNDING, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullLiborName3() {
    new VariableAnnuity(T, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, NOTIONAL, INITIAL_RATE, FUNDING, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullLiborName4() {
    new VariableAnnuity(T, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, SPREADS, NOTIONAL, INITIAL_RATE, FUNDING, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyPaymentTimes1() {
    new VariableAnnuity(new double[0], FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyPaymentTimes2() {
    new VariableAnnuity(new double[0], NOTIONAL, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyPaymentTimes3() {
    new VariableAnnuity(new double[0], INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, NOTIONAL, INITIAL_RATE, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyPaymentTimes4() {
    new VariableAnnuity(new double[0], INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, SPREADS, NOTIONAL, INITIAL_RATE, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullIndexFixing1() {
    new VariableAnnuity(T, null, INDEX_MATURITY, YEAR_FRACTIONS, NOTIONAL, INITIAL_RATE, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyIndexFixing1() {
    new VariableAnnuity(T, new double[0], INDEX_MATURITY, YEAR_FRACTIONS, NOTIONAL, INITIAL_RATE, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullIndexFixing2() {
    new VariableAnnuity(T, null, INDEX_MATURITY, YEAR_FRACTIONS, SPREADS, NOTIONAL, INITIAL_RATE, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyIndexFixing2() {
    new VariableAnnuity(T, new double[0], INDEX_MATURITY, YEAR_FRACTIONS, SPREADS, NOTIONAL, INITIAL_RATE, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullIndexMaturity1() {
    new VariableAnnuity(T, INDEX_FIXING, null, YEAR_FRACTIONS, NOTIONAL, INITIAL_RATE, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyIndexMaturity1() {
    new VariableAnnuity(T, INDEX_FIXING, new double[0], YEAR_FRACTIONS, NOTIONAL, INITIAL_RATE, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullIndexMaturity2() {
    new VariableAnnuity(T, INDEX_FIXING, null, YEAR_FRACTIONS, SPREADS, NOTIONAL, INITIAL_RATE, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyIndexMaturity2() {
    new VariableAnnuity(T, INDEX_FIXING, new double[0], YEAR_FRACTIONS, SPREADS, NOTIONAL, INITIAL_RATE, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongIndexFixing1() {
    new VariableAnnuity(T, new double[] {1}, INDEX_MATURITY, YEAR_FRACTIONS, NOTIONAL, INITIAL_RATE, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongIndexFixing2() {
    new VariableAnnuity(T, new double[] {1}, INDEX_MATURITY, YEAR_FRACTIONS, SPREADS, NOTIONAL, INITIAL_RATE, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongIndexFixing3() {
    new VariableAnnuity(T, new double[] {1, 2, 3.1, 4}, INDEX_MATURITY, YEAR_FRACTIONS, SPREADS, NOTIONAL, INITIAL_RATE, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongIndexMaturity1() {
    new VariableAnnuity(T, INDEX_FIXING, new double[] {1}, YEAR_FRACTIONS, NOTIONAL, INITIAL_RATE, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongIndexMaturity2() {
    new VariableAnnuity(T, INDEX_FIXING, new double[] {1}, YEAR_FRACTIONS, SPREADS, NOTIONAL, INITIAL_RATE, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullYearFraction1() {
    new VariableAnnuity(T, INDEX_FIXING, INDEX_MATURITY, null, NOTIONAL, INITIAL_RATE, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullYearFraction2() {
    new VariableAnnuity(T, INDEX_FIXING, INDEX_MATURITY, null, SPREADS, NOTIONAL, INITIAL_RATE, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyYearFraction1() {
    new VariableAnnuity(T, INDEX_FIXING, INDEX_MATURITY, new double[0], NOTIONAL, INITIAL_RATE, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyYearFraction2() {
    new VariableAnnuity(T, INDEX_FIXING, INDEX_MATURITY, new double[0], SPREADS, NOTIONAL, INITIAL_RATE, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongYearFraction1() {
    new VariableAnnuity(T, INDEX_FIXING, INDEX_MATURITY, new double[] {1}, NOTIONAL, INITIAL_RATE, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongYearFraction2() {
    new VariableAnnuity(T, INDEX_FIXING, INDEX_MATURITY, new double[] {1}, SPREADS, NOTIONAL, INITIAL_RATE, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSpreads() {
    new VariableAnnuity(T, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, null, NOTIONAL, INITIAL_RATE, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptySpreads() {
    new VariableAnnuity(T, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, new double[0], NOTIONAL, INITIAL_RATE, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongSpreads() {
    new VariableAnnuity(T, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, new double[] {1}, NOTIONAL, INITIAL_RATE, FUNDING, LIBOR);
  }

  @Test
  public void testGetters() {
    final double[] t = new double[] {4, 5, 6};
    final double notional = 100;
    final double[] indexFixing = new double[] {.1, 1.2, 2.3};
    final double[] indexMaturity = new double[] {1.4, 2.5, 3.6};
    final double[] yearFractions = new double[] {.5, .7, .5};
    final double[] spreads = new double[] {4, 6, 7};
    final double initialRate = 0.04;
    final VariableAnnuity annuity = new VariableAnnuity(t, indexFixing, indexMaturity, yearFractions, spreads, notional, initialRate, FUNDING, LIBOR);
    assertArrayEquals(annuity.getIndexFixingTimes(), indexFixing, 0);
    assertArrayEquals(annuity.getIndexMaturityTimes(), indexMaturity, 0);
    assertEquals(annuity.getFundingCurveName(), FUNDING);
    assertEquals(annuity.getLiborCurveName(), LIBOR);
    assertEquals(annuity.getNotional(), notional, 0);
    assertEquals(annuity.getNumberOfPayments(), indexMaturity.length);
    assertEquals(annuity.getInitialRate(), initialRate, 0);
    assertArrayEquals(annuity.getPaymentTimes(), t, 0);
    assertArrayEquals(annuity.getSpreads(), spreads, 0);
    assertArrayEquals(annuity.getYearFractions(), yearFractions, 0);

  }

  @Test
  public void testHashCodeAndEquals() {
    VariableAnnuity other = new VariableAnnuity(T, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, SPREADS, NOTIONAL, INITIAL_RATE, FUNDING, LIBOR);
    assertEquals(other, ANNUITY4);
    assertEquals(other.hashCode(), ANNUITY4.hashCode());
    // assertEquals(ANNUITY1, ANNUITY4);
    // assertEquals(ANNUITY2, ANNUITY4);
    assertEquals(ANNUITY3, ANNUITY4);

    final double[] data = new double[] {100, 100, 100, 5};
    other = new VariableAnnuity(data, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, SPREADS, NOTIONAL, INITIAL_RATE, FUNDING, LIBOR);
    assertFalse(other.equals(ANNUITY4));
    other = new VariableAnnuity(T, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, SPREADS, NOTIONAL + 1, INITIAL_RATE, FUNDING, LIBOR);
    assertFalse(other.equals(ANNUITY4));
    other = new VariableAnnuity(T, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, SPREADS, NOTIONAL, INITIAL_RATE + 0.01, FUNDING, LIBOR);
    assertFalse(other.equals(ANNUITY4));
    other = new VariableAnnuity(T, new double[] {0.1, 1.1, 2.1, 3.1}, INDEX_MATURITY, YEAR_FRACTIONS, SPREADS, NOTIONAL, INITIAL_RATE, FUNDING, LIBOR);
    assertFalse(other.equals(ANNUITY4));
    other = new VariableAnnuity(T, INDEX_FIXING, data, YEAR_FRACTIONS, SPREADS, NOTIONAL, INITIAL_RATE, FUNDING, LIBOR);
    assertFalse(other.equals(ANNUITY4));
    other = new VariableAnnuity(T, INDEX_FIXING, INDEX_MATURITY, data, SPREADS, NOTIONAL, INITIAL_RATE, FUNDING, LIBOR);
    assertFalse(other.equals(ANNUITY4));
    other = new VariableAnnuity(T, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, data, NOTIONAL, INITIAL_RATE, FUNDING, LIBOR);
    assertFalse(other.equals(ANNUITY4));
    other = new VariableAnnuity(T, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, SPREADS, NOTIONAL, INITIAL_RATE, "X", LIBOR);
    assertFalse(other.equals(ANNUITY4));
    other = new VariableAnnuity(T, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, NOTIONAL, INITIAL_RATE, "X", LIBOR);
    assertFalse(other.equals(ANNUITY4));
    other = new VariableAnnuity(T, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, SPREADS, NOTIONAL, INITIAL_RATE, FUNDING, "x");
    assertFalse(other.equals(ANNUITY4));
  }

  @Test
  public void testConversions() {
    final double[] spreads = new double[] {4, 6, 7, 8};
    final VariableAnnuity annuity = new VariableAnnuity(T, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, spreads, NOTIONAL, INITIAL_RATE, FUNDING, LIBOR);
    assertEquals(annuity.withZeroSpread(), ANNUITY4);
    assertEquals(annuity.withUnitCoupons(), new FixedAnnuity(T, NOTIONAL, new double[] {1, 1, 1, 1}, YEAR_FRACTIONS, FUNDING));
  }
}
