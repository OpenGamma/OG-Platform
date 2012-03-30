/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.annuity.definition;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;

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
  private static final Currency CUR = Currency.USD;

  private static final Period TENOR = Period.ofMonths(6);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);

  private static final AnnuityCouponIbor ANNUITY3 = new AnnuityCouponIbor(CUR, T, INDEX_FIXING, INDEX, INDEX_MATURITY, YEAR_FRACTIONS, NOTIONAL, FUNDING, LIBOR, true);
  private static final AnnuityCouponIbor ANNUITY4 = new AnnuityCouponIbor(CUR, T, INDEX_FIXING, INDEX, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, YEAR_FRACTIONS, SPREADS, NOTIONAL, FUNDING, LIBOR,
      true);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPaymentTimes1() {
    new AnnuityCouponIbor(CUR, null, INDEX, FUNDING, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPaymentTimes2() {
    new AnnuityCouponIbor(CUR, null, INDEX, NOTIONAL, FUNDING, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPaymentTimes3() {
    new AnnuityCouponIbor(CUR, null, INDEX_FIXING, INDEX, INDEX_MATURITY, YEAR_FRACTIONS, NOTIONAL, FUNDING, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPaymentTimes4() {
    new AnnuityCouponIbor(CUR, null, INDEX_FIXING, INDEX, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, YEAR_FRACTIONS, SPREADS, NOTIONAL, FUNDING, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFundingName1() {
    new AnnuityCouponIbor(CUR, T, INDEX, null, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFundingName2() {
    new AnnuityCouponIbor(CUR, T, INDEX, NOTIONAL, null, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFundingName3() {
    new AnnuityCouponIbor(CUR, T, INDEX_FIXING, INDEX, INDEX_MATURITY, YEAR_FRACTIONS, NOTIONAL, null, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFundingName4() {
    new AnnuityCouponIbor(CUR, T, INDEX_FIXING, INDEX, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, YEAR_FRACTIONS, SPREADS, NOTIONAL, null, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullLiborName1() {
    new AnnuityCouponIbor(CUR, T, INDEX, FUNDING, null, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullLiborName2() {
    new AnnuityCouponIbor(CUR, T, INDEX, NOTIONAL, FUNDING, null, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullLiborName3() {
    new AnnuityCouponIbor(CUR, T, INDEX_FIXING, INDEX, INDEX_MATURITY, YEAR_FRACTIONS, NOTIONAL, FUNDING, null, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullLiborName4() {
    new AnnuityCouponIbor(CUR, T, INDEX_FIXING, INDEX, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, YEAR_FRACTIONS, SPREADS, NOTIONAL, FUNDING, null, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyPaymentTimes1() {
    new AnnuityCouponIbor(CUR, new double[0], INDEX, FUNDING, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyPaymentTimes2() {
    new AnnuityCouponIbor(CUR, new double[0], INDEX, NOTIONAL, FUNDING, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyPaymentTimes3() {
    new AnnuityCouponIbor(CUR, new double[0], INDEX_FIXING, INDEX, INDEX_MATURITY, YEAR_FRACTIONS, NOTIONAL, FUNDING, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyPaymentTimes4() {
    new AnnuityCouponIbor(CUR, new double[0], INDEX_FIXING, INDEX, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, YEAR_FRACTIONS, SPREADS, NOTIONAL, FUNDING, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndexFixing1() {
    new AnnuityCouponIbor(CUR, T, null, INDEX, INDEX_MATURITY, YEAR_FRACTIONS, NOTIONAL, FUNDING, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyIndexFixing1() {
    new AnnuityCouponIbor(CUR, T, new double[0], INDEX, INDEX_MATURITY, YEAR_FRACTIONS, NOTIONAL, FUNDING, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndexFixing2() {
    new AnnuityCouponIbor(CUR, T, null, INDEX, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, YEAR_FRACTIONS, SPREADS, NOTIONAL, FUNDING, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyIndexFixing2() {
    new AnnuityCouponIbor(CUR, T, new double[0], INDEX, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, YEAR_FRACTIONS, SPREADS, NOTIONAL, FUNDING, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndexMaturity1() {
    new AnnuityCouponIbor(CUR, T, INDEX_FIXING, INDEX, null, YEAR_FRACTIONS, NOTIONAL, FUNDING, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyIndexMaturity1() {
    new AnnuityCouponIbor(CUR, T, INDEX_FIXING, INDEX, new double[0], YEAR_FRACTIONS, NOTIONAL, FUNDING, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndexMaturity2() {
    new AnnuityCouponIbor(CUR, T, INDEX_FIXING, INDEX, INDEX_FIXING, null, YEAR_FRACTIONS, YEAR_FRACTIONS, SPREADS, NOTIONAL, FUNDING, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyIndexMaturity2() {
    new AnnuityCouponIbor(CUR, T, INDEX_FIXING, INDEX, INDEX_FIXING, new double[0], YEAR_FRACTIONS, YEAR_FRACTIONS, SPREADS, NOTIONAL, FUNDING, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongIndexFixing1() {
    new AnnuityCouponIbor(CUR, T, new double[] {1}, INDEX, INDEX_MATURITY, YEAR_FRACTIONS, NOTIONAL, FUNDING, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongIndexFixing2() {
    new AnnuityCouponIbor(CUR, T, new double[] {1}, INDEX, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, YEAR_FRACTIONS, SPREADS, NOTIONAL, FUNDING, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongIndexFixing3() {
    new AnnuityCouponIbor(CUR, T, new double[] {1, 2, 3.1, 4}, INDEX, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, YEAR_FRACTIONS, SPREADS, NOTIONAL, FUNDING, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongIndexMaturity1() {
    new AnnuityCouponIbor(CUR, T, INDEX_FIXING, INDEX, new double[] {1}, YEAR_FRACTIONS, NOTIONAL, FUNDING, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongIndexMaturity2() {
    new AnnuityCouponIbor(CUR, T, INDEX_FIXING, INDEX, INDEX_FIXING, new double[] {1}, YEAR_FRACTIONS, YEAR_FRACTIONS, SPREADS, NOTIONAL, FUNDING, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullYearFraction1() {
    new AnnuityCouponIbor(CUR, T, INDEX_FIXING, INDEX, INDEX_MATURITY, null, NOTIONAL, FUNDING, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullYearFraction2() {
    new AnnuityCouponIbor(CUR, T, INDEX_FIXING, INDEX, INDEX_FIXING, INDEX_MATURITY, null, YEAR_FRACTIONS, SPREADS, NOTIONAL, FUNDING, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullYearFraction3() {
    new AnnuityCouponIbor(CUR, T, INDEX_FIXING, INDEX, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, null, SPREADS, NOTIONAL, FUNDING, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyYearFraction1() {
    new AnnuityCouponIbor(CUR, T, INDEX_FIXING, INDEX, INDEX_MATURITY, new double[0], NOTIONAL, FUNDING, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyYearFraction2() {
    new AnnuityCouponIbor(CUR, T, INDEX_FIXING, INDEX, INDEX_FIXING, INDEX_MATURITY, new double[0], YEAR_FRACTIONS, SPREADS, NOTIONAL, FUNDING, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyYearFraction3() {
    new AnnuityCouponIbor(CUR, T, INDEX_FIXING, INDEX, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, new double[0], SPREADS, NOTIONAL, FUNDING, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongYearFraction1() {
    new AnnuityCouponIbor(CUR, T, INDEX_FIXING, INDEX, INDEX_MATURITY, new double[] {1}, NOTIONAL, FUNDING, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongYearFraction2() {
    new AnnuityCouponIbor(CUR, T, INDEX_FIXING, INDEX, INDEX_FIXING, INDEX_MATURITY, new double[] {1}, YEAR_FRACTIONS, SPREADS, NOTIONAL, FUNDING, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongYearFraction3() {
    new AnnuityCouponIbor(CUR, T, INDEX_FIXING, INDEX, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, new double[] {1}, SPREADS, NOTIONAL, FUNDING, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSpreads() {
    new AnnuityCouponIbor(CUR, T, INDEX_FIXING, INDEX, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, YEAR_FRACTIONS, null, NOTIONAL, FUNDING, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptySpreads() {
    new AnnuityCouponIbor(CUR, T, INDEX_FIXING, INDEX, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, YEAR_FRACTIONS, new double[0], NOTIONAL, FUNDING, LIBOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongSpreads() {
    new AnnuityCouponIbor(CUR, T, INDEX_FIXING, INDEX, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACTIONS, YEAR_FRACTIONS, new double[] {1}, NOTIONAL, FUNDING, LIBOR, true);
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
    final AnnuityCouponIbor annuity = new AnnuityCouponIbor(CUR, t, indexFixing, INDEX, indexFixing, indexMaturity, paymentYearFractions, forwardYearFractions, spreads, notional, FUNDING, LIBOR,
        false);

    final int n = annuity.getNumberOfPayments();
    assertEquals(3, n, 0);
    int index = 0;
    for (final CouponIbor p : annuity.getPayments()) {
      assertEquals(p.getFixingTime(), indexFixing[index], 0);
      assertEquals(p.getFixingPeriodEndTime(), indexMaturity[index], 0);
      assertEquals(p.getFundingCurveName(), FUNDING);
      assertEquals(p.getForwardCurveName(), LIBOR);
      assertEquals(p.getNotional(), notional, 0);
      assertEquals(p.getPaymentTime(), t[index], 0);
      assertEquals(p.getSpread(), spreads[index], 0);
      assertEquals(p.getPaymentYearFraction(), paymentYearFractions[index], 0);
      assertEquals(p.getFixingYearFraction(), forwardYearFractions[index], 0);
      index++;
    }
  }

  @Test
  public void testEqualsAndHashCode() {
    final AnnuityCouponIbor other = new AnnuityCouponIbor(CUR, T, INDEX_FIXING, INDEX, INDEX_MATURITY, YEAR_FRACTIONS, NOTIONAL, FUNDING, LIBOR, true);
    assertEquals(other, ANNUITY3);
    assertEquals(other.hashCode(), ANNUITY3.hashCode());
    assertEquals(other, ANNUITY4);
    assertEquals(other.hashCode(), ANNUITY4.hashCode());
  }

}
