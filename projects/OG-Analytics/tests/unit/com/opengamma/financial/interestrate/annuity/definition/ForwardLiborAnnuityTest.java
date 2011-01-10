/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.definition;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.interestrate.payments.ForwardLiborPayment;

/**
 * 
 */
public class ForwardLiborAnnuityTest {
  private static final double[] T = new double[] {1, 2, 3, 4};
  private static final double[] YEAR_FRACTIONS = new double[] {1, 1, 1, 1};
  private static final double[] INDEX_FIXING = new double[] {0, 1, 2, 3};
  private static final double[] INDEX_MATURITY = new double[] {1, 2, 3, 4};
  private static final double[] SPREADS = new double[] {0, 0, 0, 0};
  private static final double NOTIONAL = 1;

  private static final String FUNDING = "Funding";
  private static final String LIBOR = "Libor";

  private static final ForwardLiborAnnuity ANNUITY3 = new ForwardLiborAnnuity(T, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, NOTIONAL, FUNDING, LIBOR);
  private static final ForwardLiborAnnuity ANNUITY4 = new ForwardLiborAnnuity(T, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, YEAR_FRACTIONS, SPREADS, NOTIONAL, FUNDING, LIBOR);

  @Test(expected = IllegalArgumentException.class)
  public void testNullPaymentTimes1() {
    new ForwardLiborAnnuity(null, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullPaymentTimes2() {
    new ForwardLiborAnnuity(null, NOTIONAL, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullPaymentTimes3() {
    new ForwardLiborAnnuity(null, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, NOTIONAL, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullPaymentTimes4() {
    new ForwardLiborAnnuity(null, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, YEAR_FRACTIONS, SPREADS, NOTIONAL, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFundingName1() {
    new ForwardLiborAnnuity(T, null, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFundingName2() {
    new ForwardLiborAnnuity(T, NOTIONAL, null, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFundingName3() {
    new ForwardLiborAnnuity(T, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, NOTIONAL, null, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFundingName4() {
    new ForwardLiborAnnuity(T, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, YEAR_FRACTIONS, SPREADS, NOTIONAL, null, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullLiborName1() {
    new ForwardLiborAnnuity(T, FUNDING, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullLiborName2() {
    new ForwardLiborAnnuity(T, NOTIONAL, FUNDING, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullLiborName3() {
    new ForwardLiborAnnuity(T, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, NOTIONAL, FUNDING, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullLiborName4() {
    new ForwardLiborAnnuity(T, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, YEAR_FRACTIONS, SPREADS, NOTIONAL, FUNDING, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyPaymentTimes1() {
    new ForwardLiborAnnuity(new double[0], FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyPaymentTimes2() {
    new ForwardLiborAnnuity(new double[0], NOTIONAL, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyPaymentTimes3() {
    new ForwardLiborAnnuity(new double[0], INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, NOTIONAL, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyPaymentTimes4() {
    new ForwardLiborAnnuity(new double[0], INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, YEAR_FRACTIONS, SPREADS, NOTIONAL, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullIndexFixing1() {
    new ForwardLiborAnnuity(T, null, INDEX_MATURITY, YEAR_FRACTIONS, NOTIONAL, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyIndexFixing1() {
    new ForwardLiborAnnuity(T, new double[0], INDEX_MATURITY, YEAR_FRACTIONS, NOTIONAL, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullIndexFixing2() {
    new ForwardLiborAnnuity(T, null, INDEX_MATURITY, YEAR_FRACTIONS, YEAR_FRACTIONS, SPREADS, NOTIONAL, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyIndexFixing2() {
    new ForwardLiborAnnuity(T, new double[0], INDEX_MATURITY, YEAR_FRACTIONS, YEAR_FRACTIONS, SPREADS, NOTIONAL, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullIndexMaturity1() {
    new ForwardLiborAnnuity(T, INDEX_FIXING, null, YEAR_FRACTIONS, NOTIONAL, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyIndexMaturity1() {
    new ForwardLiborAnnuity(T, INDEX_FIXING, new double[0], YEAR_FRACTIONS, NOTIONAL, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullIndexMaturity2() {
    new ForwardLiborAnnuity(T, INDEX_FIXING, null, YEAR_FRACTIONS, YEAR_FRACTIONS, SPREADS, NOTIONAL, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyIndexMaturity2() {
    new ForwardLiborAnnuity(T, INDEX_FIXING, new double[0], YEAR_FRACTIONS, YEAR_FRACTIONS, SPREADS, NOTIONAL, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongIndexFixing1() {
    new ForwardLiborAnnuity(T, new double[] {1}, INDEX_MATURITY, YEAR_FRACTIONS, NOTIONAL, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongIndexFixing2() {
    new ForwardLiborAnnuity(T, new double[] {1}, INDEX_MATURITY, YEAR_FRACTIONS, YEAR_FRACTIONS, SPREADS, NOTIONAL, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongIndexFixing3() {
    new ForwardLiborAnnuity(T, new double[] {1, 2, 3.1, 4}, INDEX_MATURITY, YEAR_FRACTIONS, YEAR_FRACTIONS, SPREADS, NOTIONAL, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongIndexMaturity1() {
    new ForwardLiborAnnuity(T, INDEX_FIXING, new double[] {1}, YEAR_FRACTIONS, NOTIONAL, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongIndexMaturity2() {
    new ForwardLiborAnnuity(T, INDEX_FIXING, new double[] {1}, YEAR_FRACTIONS, YEAR_FRACTIONS, SPREADS, NOTIONAL, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullYearFraction1() {
    new ForwardLiborAnnuity(T, INDEX_FIXING, INDEX_MATURITY, null, NOTIONAL, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullYearFraction2() {
    new ForwardLiborAnnuity(T, INDEX_FIXING, INDEX_MATURITY, null, YEAR_FRACTIONS, SPREADS, NOTIONAL, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullYearFraction3() {
    new ForwardLiborAnnuity(T, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, null, SPREADS, NOTIONAL, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyYearFraction1() {
    new ForwardLiborAnnuity(T, INDEX_FIXING, INDEX_MATURITY, new double[0], NOTIONAL, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyYearFraction2() {
    new ForwardLiborAnnuity(T, INDEX_FIXING, INDEX_MATURITY, new double[0], YEAR_FRACTIONS, SPREADS, NOTIONAL, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyYearFraction3() {
    new ForwardLiborAnnuity(T, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, new double[0], SPREADS, NOTIONAL, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongYearFraction1() {
    new ForwardLiborAnnuity(T, INDEX_FIXING, INDEX_MATURITY, new double[] {1}, NOTIONAL, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongYearFraction2() {
    new ForwardLiborAnnuity(T, INDEX_FIXING, INDEX_MATURITY, new double[] {1}, YEAR_FRACTIONS, SPREADS, NOTIONAL, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongYearFraction3() {
    new ForwardLiborAnnuity(T, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, new double[] {1}, SPREADS, NOTIONAL, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSpreads() {
    new ForwardLiborAnnuity(T, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, YEAR_FRACTIONS, null, NOTIONAL, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptySpreads() {
    new ForwardLiborAnnuity(T, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, YEAR_FRACTIONS, new double[0], NOTIONAL, FUNDING, LIBOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongSpreads() {
    new ForwardLiborAnnuity(T, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, YEAR_FRACTIONS, new double[] {1}, NOTIONAL, FUNDING, LIBOR);
  }

  @Test
  public void testGetters() {
    final double[] t = new double[] {4.5, 5, 5.5};
    final double notional = 100;
    final double[] indexFixing = new double[] {4.1, 4.55, 5.01};
    final double[] indexMaturity = new double[] {4.6, 5.0, 5.55};
    final double[] paymentYearFractions = new double[] {.5, .49, .5};
    final double[] forwardYearFractions = new double[] {.51, .58, .52};
    final double[] spreads = new double[] {4, 6, 7};
    final ForwardLiborAnnuity annuity = new ForwardLiborAnnuity(t, indexFixing, indexMaturity, paymentYearFractions, forwardYearFractions, spreads, notional, FUNDING, LIBOR);

    final int n = annuity.getNumberOfPayments();
    assertEquals(3, n, 0);
    int index = 0;
    for (final ForwardLiborPayment p : annuity.getPayments()) {
      assertEquals(p.getLiborFixingTime(), indexFixing[index], 0);
      assertEquals(p.getLiborMaturityTime(), indexMaturity[index], 0);
      assertEquals(p.getFundingCurveName(), FUNDING);
      assertEquals(p.getLiborCurveName(), LIBOR);
      assertEquals(p.getNotional(), notional, 0);
      assertEquals(p.getPaymentTime(), t[index], 0);
      assertEquals(p.getSpread(), spreads[index], 0);
      assertEquals(p.getPaymentYearFraction(), paymentYearFractions[index], 0);
      assertEquals(p.getForwardYearFraction(), forwardYearFractions[index], 0);
      index++;
    }
  }

  @Test
  public void testEqualsAndHashCode() {
    final ForwardLiborAnnuity other = new ForwardLiborAnnuity(T, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, NOTIONAL, FUNDING, LIBOR);
    assertEquals(other, ANNUITY3);
    assertEquals(other.hashCode(), ANNUITY3.hashCode());
    assertEquals(other, ANNUITY4);
    assertEquals(other.hashCode(), ANNUITY4.hashCode());
  }

}
