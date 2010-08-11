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
  private static final double[] DELTA_START = new double[] {0, 0, 0, 0};
  private static final double[] DELTA_END = new double[] {0, 0, 0, 0};
  private static final double[] SPREADS = new double[] {0, 0, 0, 0};
  private static final double NOTIONAL = 1;
  private static final String FUNDING = "Funding";
  private static final String LIBOR = "Libor";
  private static final VariableAnnuity ANNUITY1 = new VariableAnnuity(T, FUNDING, LIBOR);
  private static final VariableAnnuity ANNUITY2 = new VariableAnnuity(T, NOTIONAL, FUNDING, LIBOR);
  private static final VariableAnnuity ANNUITY3 = new VariableAnnuity(T, NOTIONAL, DELTA_START, DELTA_END, FUNDING, LIBOR);
  private static final VariableAnnuity ANNUITY4 = new VariableAnnuity(T, NOTIONAL, DELTA_START, DELTA_END, YEAR_FRACTIONS, SPREADS, FUNDING, LIBOR);

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
    new VariableAnnuity(null, NOTIONAL, DELTA_START, DELTA_END, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullPaymentTimes4() {
    new VariableAnnuity(null, NOTIONAL, DELTA_START, DELTA_END, YEAR_FRACTIONS, SPREADS, FUNDING, LIBOR);
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
    new VariableAnnuity(T, NOTIONAL, DELTA_START, DELTA_END, null, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFundingName4() {
    new VariableAnnuity(T, NOTIONAL, DELTA_START, DELTA_END, YEAR_FRACTIONS, SPREADS, null, LIBOR);
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
    new VariableAnnuity(T, NOTIONAL, DELTA_START, DELTA_END, FUNDING, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullLiborName4() {
    new VariableAnnuity(T, NOTIONAL, DELTA_START, DELTA_END, YEAR_FRACTIONS, SPREADS, FUNDING, null);
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
    new VariableAnnuity(new double[0], NOTIONAL, DELTA_START, DELTA_END, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyPaymentTimes4() {
    new VariableAnnuity(new double[0], NOTIONAL, DELTA_START, DELTA_END, YEAR_FRACTIONS, SPREADS, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullDeltaStart1() {
    new VariableAnnuity(T, NOTIONAL, null, DELTA_END, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyDeltaStart1() {
    new VariableAnnuity(T, NOTIONAL, new double[0], DELTA_END, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullDeltaStart2() {
    new VariableAnnuity(T, NOTIONAL, null, DELTA_END, YEAR_FRACTIONS, SPREADS, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyDeltaStart2() {
    new VariableAnnuity(T, NOTIONAL, new double[0], DELTA_END, YEAR_FRACTIONS, SPREADS, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullDeltaEnd1() {
    new VariableAnnuity(T, NOTIONAL, DELTA_START, null, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyDeltaEnd1() {
    new VariableAnnuity(T, NOTIONAL, DELTA_START, new double[0], FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullDeltaEnd2() {
    new VariableAnnuity(T, NOTIONAL, DELTA_START, null, YEAR_FRACTIONS, SPREADS, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyDeltaEnd2() {
    new VariableAnnuity(T, NOTIONAL, DELTA_START, new double[0], YEAR_FRACTIONS, SPREADS, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongDeltaStart1() {
    new VariableAnnuity(T, NOTIONAL, new double[] {1}, DELTA_END, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongDeltaStart2() {
    new VariableAnnuity(T, NOTIONAL, new double[] {1}, DELTA_END, YEAR_FRACTIONS, SPREADS, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongDeltaEnd1() {
    new VariableAnnuity(T, NOTIONAL, DELTA_START, new double[] {1}, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongDeltaEnd2() {
    new VariableAnnuity(T, NOTIONAL, DELTA_START, new double[] {1}, YEAR_FRACTIONS, SPREADS, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullYearFraction() {
    new VariableAnnuity(T, NOTIONAL, DELTA_START, DELTA_END, null, SPREADS, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyYearFraction() {
    new VariableAnnuity(T, NOTIONAL, DELTA_START, DELTA_END, new double[0], SPREADS, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongYearFraction() {
    new VariableAnnuity(T, NOTIONAL, DELTA_START, DELTA_END, new double[] {1}, SPREADS, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSpreads() {
    new VariableAnnuity(T, NOTIONAL, DELTA_START, DELTA_END, YEAR_FRACTIONS, null, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptySpreads() {
    new VariableAnnuity(T, NOTIONAL, DELTA_START, DELTA_END, YEAR_FRACTIONS, new double[0], FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongSpreads() {
    new VariableAnnuity(T, NOTIONAL, DELTA_START, DELTA_END, YEAR_FRACTIONS, new double[] {1}, FUNDING, LIBOR);
  }

  @Test
  public void testGetters() {
    final double[] t = new double[] {4, 5, 6};
    final double notional = 100;
    final double[] deltaStart = new double[] {.1, .2, .3};
    final double[] deltaEnd = new double[] {.4, .5, .6};
    final double[] yearFractions = new double[] {.5, .7, .5};
    final double[] spreads = new double[] {4, 6, 7};
    final VariableAnnuity annuity = new VariableAnnuity(t, notional, deltaStart, deltaEnd, yearFractions, spreads, FUNDING, LIBOR);
    assertArrayEquals(annuity.getDeltaEnd(), deltaEnd, 0);
    assertArrayEquals(annuity.getDeltaStart(), deltaStart, 0);
    assertEquals(annuity.getFundingCurveName(), FUNDING);
    assertEquals(annuity.getLiborCurveName(), LIBOR);
    assertEquals(annuity.getNotional(), notional, 0);
    assertEquals(annuity.getNumberOfPayments(), deltaEnd.length);
    assertArrayEquals(annuity.getPaymentTimes(), t, 0);
    assertArrayEquals(annuity.getSpreads(), spreads, 0);
    assertArrayEquals(annuity.getYearFractions(), yearFractions, 0);
  }

  @Test
  public void testHashCodeAndEquals() {
    VariableAnnuity other = new VariableAnnuity(T, NOTIONAL, DELTA_START, DELTA_END, YEAR_FRACTIONS, SPREADS, FUNDING, LIBOR);
    assertEquals(other, ANNUITY4);
    assertEquals(other.hashCode(), ANNUITY4.hashCode());
    assertEquals(ANNUITY1, ANNUITY4);
    assertEquals(ANNUITY2, ANNUITY4);
    assertEquals(ANNUITY3, ANNUITY4);
    final double[] data = new double[] {100, 100, 100, 5};
    other = new VariableAnnuity(data, NOTIONAL, DELTA_START, DELTA_END, YEAR_FRACTIONS, SPREADS, FUNDING, LIBOR);
    assertFalse(other.equals(ANNUITY4));
    other = new VariableAnnuity(T, NOTIONAL + 1, DELTA_START, DELTA_END, YEAR_FRACTIONS, SPREADS, FUNDING, LIBOR);
    assertFalse(other.equals(ANNUITY4));
    other = new VariableAnnuity(T, NOTIONAL, data, DELTA_END, YEAR_FRACTIONS, SPREADS, FUNDING, LIBOR);
    assertFalse(other.equals(ANNUITY4));
    other = new VariableAnnuity(T, NOTIONAL, DELTA_START, data, YEAR_FRACTIONS, SPREADS, FUNDING, LIBOR);
    assertFalse(other.equals(ANNUITY4));
    other = new VariableAnnuity(T, NOTIONAL, DELTA_START, DELTA_END, data, SPREADS, FUNDING, LIBOR);
    assertFalse(other.equals(ANNUITY4));
    other = new VariableAnnuity(T, NOTIONAL, DELTA_START, DELTA_END, YEAR_FRACTIONS, data, FUNDING, LIBOR);
    assertFalse(other.equals(ANNUITY4));
    other = new VariableAnnuity(T, NOTIONAL, DELTA_START, DELTA_END, YEAR_FRACTIONS, SPREADS, "X", LIBOR);
    assertFalse(other.equals(ANNUITY4));
    other = new VariableAnnuity(T, NOTIONAL, DELTA_START, DELTA_END, YEAR_FRACTIONS, SPREADS, FUNDING, "x");
    assertFalse(other.equals(ANNUITY4));
  }

  @Test
  public void testConversions() {
    final double[] spreads = new double[] {4, 6, 7, 8};
    final VariableAnnuity annuity = new VariableAnnuity(T, NOTIONAL, DELTA_START, DELTA_END, YEAR_FRACTIONS, spreads, FUNDING, LIBOR);
    assertEquals(annuity.withZeroSpread(), ANNUITY4);
    assertEquals(annuity.withUnitCoupons(), new FixedAnnuity(T, NOTIONAL, new double[] {1, 1, 1, 1}, YEAR_FRACTIONS, FUNDING));
  }
}
