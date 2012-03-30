/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponOISDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorOIS;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.analytics.financial.interestrate.payments.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponFixedDiscountingMethod;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.DayOfWeek;
import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

/**
 * 
 */
public class AnnuityCouponOISDefinitionTest {
  private static final Currency CCY = Currency.EUR;
  private static final Period PAYMENT_PERIOD = Period.ofMonths(6);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("Weekend");
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final boolean IS_EOM = true;
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2012, 2, 1);
  private static final ZonedDateTime MATURITY_DATE = DateUtils.getUTCDate(2022, 2, 1);
  private static final Period MATURITY_TENOR = Period.ofYears(10);
  private static final double NOTIONAL = 100000000;
  private static final IndexON INDEX = new IndexON("O/N", CCY, DAY_COUNT, 0, CALENDAR);
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final GeneratorOIS GENERATOR = new GeneratorOIS("OIS", INDEX, PAYMENT_PERIOD, DAY_COUNT, BUSINESS_DAY, IS_EOM, 1);
  private static final boolean IS_PAYER = true;
  private static final AnnuityCouponOISDefinition DEFINITION = AnnuityCouponOISDefinition.from(SETTLEMENT_DATE, MATURITY_DATE, NOTIONAL, GENERATOR, IS_PAYER);
  private static final int NUM_PAYMENTS = DEFINITION.getNumberOfPayments();
  private static final ZonedDateTime FINAL_PAYMENT_DATE = DEFINITION.getNthPayment(NUM_PAYMENTS - 1).getPaymentDate();

  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final String[] CURVES_NAMES = CURVES.getAllNames().toArray(new String[0]);

  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2012, 3, 15);

  // Utility to create a time series of fixings
  static ArrayZonedDateTimeDoubleTimeSeries createFixingSeries(ZonedDateTime startDate, ZonedDateTime endDate) {
    final List<ZonedDateTime> dates = new ArrayList<ZonedDateTime>();
    final List<Double> data = new ArrayList<Double>();
    ZonedDateTime dt = startDate;
    while (dt.isBefore(endDate) || dt.equals(endDate)) {
      dates.add(dt);
      data.add(0.05 + Math.sin(dt.getDayOfYear()) / 100);
      dt = dt.plusDays(1);
      if (dt.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
        dt = dt.plusDays(2);
      }
    }
    return new ArrayZonedDateTimeDoubleTimeSeries(dates, data);
  }

  private static final DoubleTimeSeries<ZonedDateTime> FIXING_TS = createFixingSeries(SETTLEMENT_DATE, FINAL_PAYMENT_DATE);

  @Test
  /**
   * Tests the toDerivative method on the payment date. valuation is at noon, payment set at midnight...
   */
  public void toDerivativeOnDateOfFinalPayment() {

    ZonedDateTime valuationTimeIsNoon = FINAL_PAYMENT_DATE.withTime(12, 0);
    assertTrue("valuationTimeIsNoon usedn        to be after paymentDate, which was midnight. Confirm behaviour",
        valuationTimeIsNoon.isAfter(FINAL_PAYMENT_DATE));
    GenericAnnuity<? extends Coupon> derivative = DEFINITION.toDerivative(valuationTimeIsNoon, FIXING_TS, CURVES_NAMES);
    assertEquals("On the payment date, we expect the derivative to have the same number of payments as its definition", 1, derivative.getNumberOfPayments());
    assertTrue("CouponOIS should be of type CouponFixed on the payment date", derivative.getNthPayment(0) instanceof CouponFixed);

    CurrencyAmount pv = CouponFixedDiscountingMethod.getInstance().presentValue((CouponFixed) derivative.getNthPayment(0), CURVES);
    assertEquals("CouponOIS definition: toDerivative", pv, CurrencyAmount.of(CCY, -2571693.2212814568));

  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCoupons() {
    new AnnuityCouponOISDefinition(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSettlementDate1() {
    AnnuityCouponOISDefinition.from(null, MATURITY_TENOR, NOTIONAL, GENERATOR, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPaymentFrequency() {
    AnnuityCouponOISDefinition.from(SETTLEMENT_DATE, (Period) null, NOTIONAL, GENERATOR, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullGenerator1() {
    AnnuityCouponOISDefinition.from(SETTLEMENT_DATE, MATURITY_TENOR, NOTIONAL, null, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSettlementDate2() {
    AnnuityCouponOISDefinition.from(null, MATURITY_DATE, NOTIONAL, GENERATOR, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMaturityDate() {
    AnnuityCouponOISDefinition.from(SETTLEMENT_DATE, (ZonedDateTime) null, NOTIONAL, GENERATOR, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullGenerator2() {
    AnnuityCouponOISDefinition.from(SETTLEMENT_DATE, MATURITY_DATE, NOTIONAL, null, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNoIndexTS() {
    DEFINITION.toDerivative(DATE, "A", "B");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDate() {
    DEFINITION.toDerivative(null, FIXING_TS, "A", "B");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndexTS() {
    DEFINITION.toDerivative(DATE, (DoubleTimeSeries<ZonedDateTime>) null, "A", "B");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNames() {
    DEFINITION.toDerivative(DATE, FIXING_TS, (String[]) null);
  }

  @Test
  public void testHashCodeAndEquals() {
    AnnuityCouponOISDefinition definition = AnnuityCouponOISDefinition.from(SETTLEMENT_DATE, MATURITY_DATE, NOTIONAL, GENERATOR, IS_PAYER);
    assertEquals(DEFINITION, definition);
    assertEquals(DEFINITION.hashCode(), definition.hashCode());
    definition = AnnuityCouponOISDefinition.from(SETTLEMENT_DATE, MATURITY_TENOR, NOTIONAL, GENERATOR, IS_PAYER);
    assertEquals(DEFINITION, definition);
    assertEquals(DEFINITION.hashCode(), definition.hashCode());
    definition = AnnuityCouponOISDefinition.from(SETTLEMENT_DATE.plusDays(1), MATURITY_DATE, NOTIONAL, GENERATOR, IS_PAYER);
    assertFalse(DEFINITION.equals(definition));
    definition = AnnuityCouponOISDefinition.from(SETTLEMENT_DATE, MATURITY_DATE.plusDays(1), NOTIONAL, GENERATOR, IS_PAYER);
    assertFalse(DEFINITION.equals(definition));
    definition = AnnuityCouponOISDefinition.from(SETTLEMENT_DATE, MATURITY_DATE, NOTIONAL / 2, GENERATOR, IS_PAYER);
    assertFalse(DEFINITION.equals(definition));
    definition = AnnuityCouponOISDefinition.from(SETTLEMENT_DATE, MATURITY_DATE, NOTIONAL, new GeneratorOIS("OIS", INDEX, PAYMENT_PERIOD, DAY_COUNT, BUSINESS_DAY, IS_EOM, 0), IS_PAYER);
    assertFalse(DEFINITION.equals(definition));
    definition = AnnuityCouponOISDefinition.from(SETTLEMENT_DATE, MATURITY_DATE, NOTIONAL, GENERATOR, !IS_PAYER);
    assertFalse(DEFINITION.equals(definition));
  }
}
