/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.Arrays;

import javax.time.calendar.Period;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.analytics.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.analytics.financial.interestrate.payments.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.CouponIbor;
import com.opengamma.analytics.financial.interestrate.swap.definition.FixedFloatSwap;
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
public class FixedFloatSwapTest {
  public static final String FUNDING_CURVE_NAME = "funding";
  public static final String LIBOR_CURVE_NAME = "Libor";
  private static final double[] FIXED_PAYMENTS = new double[] {1.5, 2, 3, 4, 5, 6};
  private static final double COUPON_RATE = 0.04;
  private static final double[] FLOAT_PAYMENTS = new double[] {1.5, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
  private static final double[] INDEX_FIXING = new double[] {0.0, 1.5, 2., 3., 4., 5., 6., 7., 8., 9., 10., 11.};
  private static final double[] INDEX_MATURITY = new double[] {1.5, 2, 3., 4., 5., 6., 7., 8., 9, 10., 11., 12.};
  private static final double[] YEAR_FRACS = new double[] {1.5, 0.5, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
  private static final Currency CUR = Currency.USD;

  private static final Period TENOR = Period.ofMonths(6);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);

  private static final FixedFloatSwap SWAP = new FixedFloatSwap(CUR, FIXED_PAYMENTS, FLOAT_PAYMENTS, INDEX, COUPON_RATE, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME, true);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFixedPayments() {
    new FixedFloatSwap(CUR, null, FLOAT_PAYMENTS, INDEX, COUPON_RATE, FUNDING_CURVE_NAME, FUNDING_CURVE_NAME, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFloatPayments() {
    new FixedFloatSwap(CUR, FIXED_PAYMENTS, null, INDEX, COUPON_RATE, FUNDING_CURVE_NAME, FUNDING_CURVE_NAME, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFundingCurveName() {
    new FixedFloatSwap(CUR, FIXED_PAYMENTS, FLOAT_PAYMENTS, INDEX, COUPON_RATE, null, FUNDING_CURVE_NAME, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullLiborCurveName() {
    new FixedFloatSwap(CUR, FIXED_PAYMENTS, FLOAT_PAYMENTS, INDEX, COUPON_RATE, FUNDING_CURVE_NAME, null, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyFixedPayments() {
    new FixedFloatSwap(CUR, new double[0], FLOAT_PAYMENTS, INDEX, COUPON_RATE, FUNDING_CURVE_NAME, FUNDING_CURVE_NAME, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyFloatPayments() {
    new FixedFloatSwap(CUR, FIXED_PAYMENTS, new double[0], INDEX, COUPON_RATE, FUNDING_CURVE_NAME, FUNDING_CURVE_NAME, true);
  }

  @Test
  public void testHashCodeAndEquals() {
    FixedFloatSwap other = new FixedFloatSwap(CUR, Arrays.copyOf(FIXED_PAYMENTS, FIXED_PAYMENTS.length), Arrays.copyOf(FLOAT_PAYMENTS, FLOAT_PAYMENTS.length), INDEX, COUPON_RATE, FUNDING_CURVE_NAME,
        LIBOR_CURVE_NAME, true);
    assertEquals(other, SWAP);
    assertEquals(other.hashCode(), SWAP.hashCode());
    other = new FixedFloatSwap(CUR, new double[] {1, 2, 3, 4, 5, 6}, FLOAT_PAYMENTS, INDEX, COUPON_RATE, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME, true);
    assertFalse(other.equals(SWAP));
    other = new FixedFloatSwap(CUR, FIXED_PAYMENTS, new double[] {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13}, INDEX, COUPON_RATE, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME, true);
    assertFalse(other.equals(SWAP));
    other = new FixedFloatSwap(CUR, FIXED_PAYMENTS, FLOAT_PAYMENTS, INDEX, 0.03, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME, true);
    assertFalse(other.equals(SWAP));
    other = new FixedFloatSwap(CUR, FIXED_PAYMENTS, FLOAT_PAYMENTS, INDEX, COUPON_RATE, LIBOR_CURVE_NAME, LIBOR_CURVE_NAME, true);
    assertFalse(other.equals(SWAP));
    other = new FixedFloatSwap(CUR, FIXED_PAYMENTS, FLOAT_PAYMENTS, INDEX, COUPON_RATE, FUNDING_CURVE_NAME, FUNDING_CURVE_NAME, true);
    assertFalse(other.equals(SWAP));

    AnnuityCouponFixed fixed = new AnnuityCouponFixed(CUR, FIXED_PAYMENTS, 1.0, COUPON_RATE, FUNDING_CURVE_NAME, true);
    AnnuityCouponIbor floating = new AnnuityCouponIbor(CUR, FLOAT_PAYMENTS, INDEX_FIXING, INDEX, INDEX_MATURITY, YEAR_FRACS, 1.0, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME, false);
    other = new FixedFloatSwap(fixed, floating);
    assertEquals(other.getFixedLeg(), SWAP.getFixedLeg());
    assertEquals(other.getFloatingLeg().getNthPayment(0), SWAP.getFloatingLeg().getNthPayment(0));
    assertEquals(other.getFloatingLeg(), SWAP.getFloatingLeg());
    assertEquals(other, SWAP);
    assertEquals(other.hashCode(), SWAP.hashCode());
  }

  @Test
  public void testGetters() {
    GenericAnnuity<CouponFixed> fixedLeg = SWAP.getFixedLeg();
    assertEquals(fixedLeg.getNumberOfPayments(), FIXED_PAYMENTS.length, 0);
    for (int i = 0; i < FIXED_PAYMENTS.length; i++) {
      assertEquals(fixedLeg.getNthPayment(i).getPaymentTime(), FIXED_PAYMENTS[i], 0);
      assertEquals(fixedLeg.getNthPayment(i).getFixedRate(), COUPON_RATE, 0);
    }

    GenericAnnuity<CouponIbor> floatLeg = SWAP.getFloatingLeg();
    assertEquals(floatLeg.getNumberOfPayments(), FLOAT_PAYMENTS.length, 0);
    for (int i = 0; i < FLOAT_PAYMENTS.length; i++) {
      assertEquals(floatLeg.getNthPayment(i).getPaymentTime(), FLOAT_PAYMENTS[i], 0);
      assertEquals(floatLeg.getNthPayment(i).getFixingTime(), INDEX_FIXING[i], 0);
      assertEquals(floatLeg.getNthPayment(i).getFixingPeriodEndTime(), INDEX_MATURITY[i], 0);
      assertEquals(floatLeg.getNthPayment(i).getPaymentYearFraction(), YEAR_FRACS[i], 0);
      assertEquals(floatLeg.getNthPayment(i).getFixingYearFraction(), YEAR_FRACS[i], 0);
      assertEquals(floatLeg.getNthPayment(i).getSpread(), 0.0, 0);
    }

  }

}
