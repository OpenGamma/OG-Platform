/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.inflation.method;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationYearOnYearMonthlyDefinition;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearMonthly;
import com.opengamma.analytics.financial.provider.calculator.inflation.NetAmountInflationCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;

/**
 * Tests the present value and its sensitivities for year on year with reference index on the first of the month.
 */
public class CouponInflationYearOnYearMonthlyDiscountingMethodTest {

  private static final InflationIssuerProviderDiscount MARKET = MulticurveProviderDiscountDataSets.createMarket1();
  private static final IndexPrice[] PRICE_INDEXES = MARKET.getPriceIndexes().toArray(new IndexPrice[0]);
  private static final IndexPrice PRICE_INDEX_EUR = PRICE_INDEXES[0];
  private static final IborIndex[] IBOR_INDEXES = MARKET.getIndexesIbor().toArray(new IborIndex[0]);
  private static final IborIndex EURIBOR3M = IBOR_INDEXES[0];
  private static final Calendar CALENDAR_EUR = EURIBOR3M.getCalendar();
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final Period COUPON_TENOR = Period.ofYears(10);
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(START_DATE, COUPON_TENOR, BUSINESS_DAY, CALENDAR_EUR);
  private static final ZonedDateTime PAYMENT_DATE_MINUS1 = ScheduleCalculator.getAdjustedDate(START_DATE, Period.ofYears(9), BUSINESS_DAY, CALENDAR_EUR);
  private static final double NOTIONAL = 98765432;
  private static final int MONTH_LAG = 3;
  private static final ZonedDateTime PRICING_DATE = DateUtils.getUTCDate(2011, 8, 3);
  private static final CouponInflationYearOnYearMonthlyDefinition YEAR_ON_YEAR_NO_DEFINITION = CouponInflationYearOnYearMonthlyDefinition.from(PAYMENT_DATE_MINUS1, PAYMENT_DATE, NOTIONAL,
      PRICE_INDEX_EUR,
      MONTH_LAG, false);
  private static final CouponInflationYearOnYearMonthly YEAR_ON_YEAR_NO = YEAR_ON_YEAR_NO_DEFINITION.toDerivative(PRICING_DATE, "not used");
  private static final CouponInflationYearOnYearMonthlyDefinition YEAR_ON_YEAR_WITH_DEFINITION = CouponInflationYearOnYearMonthlyDefinition.from(PAYMENT_DATE_MINUS1, PAYMENT_DATE, NOTIONAL,
      PRICE_INDEX_EUR,
      MONTH_LAG, true);
  private static final CouponInflationYearOnYearMonthly YEAR_ON_YEAR_WITH = YEAR_ON_YEAR_WITH_DEFINITION.toDerivative(PRICING_DATE, "not used");
  private static final CouponInflationYearOnYearMonthlyDiscountingMethod METHOD = new CouponInflationYearOnYearMonthlyDiscountingMethod();
  private static final PresentValueDiscountingInflationCalculator PVIC = PresentValueDiscountingInflationCalculator.getInstance();
  private static final NetAmountInflationCalculator NAIC = NetAmountInflationCalculator.getInstance();

  @Test
  /**
   * Tests the present value.
   */
  public void presentValueNoNotional() {
    final MultipleCurrencyAmount pv = METHOD.presentValue(YEAR_ON_YEAR_NO, MARKET.getInflationProvider());
    final double df = MARKET.getCurve(YEAR_ON_YEAR_NO.getCurrency()).getDiscountFactor(YEAR_ON_YEAR_NO.getPaymentTime());
    final double finalIndex = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(YEAR_ON_YEAR_NO.getReferenceEndTime());
    final double initialIndex = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(YEAR_ON_YEAR_NO.getReferenceStartTime());
    final double pvExpected = (finalIndex / initialIndex - 1) * df * NOTIONAL;
    assertEquals("Year on year coupon inflation: Present value", pvExpected, pv.getAmount(YEAR_ON_YEAR_NO.getCurrency()), 1.0E-2);
  }

  @Test
  /**
   * Tests the net amount.
   */
  public void netAmountNoNotional() {
    final MultipleCurrencyAmount pv = METHOD.netAmount(YEAR_ON_YEAR_NO, MARKET.getInflationProvider());
    final double finalIndex = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(YEAR_ON_YEAR_NO.getReferenceEndTime());
    final double initialIndex = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(YEAR_ON_YEAR_NO.getReferenceStartTime());
    final double pvExpected = (finalIndex / initialIndex - 1) * NOTIONAL;
    assertEquals("Year on year coupon inflation: net amount", pvExpected, pv.getAmount(YEAR_ON_YEAR_NO.getCurrency()), 1.0E-2);
  }

  @Test
  /**
   * Tests the present value: Method vs Calculator.
   */
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD.presentValue(YEAR_ON_YEAR_NO, MARKET.getInflationProvider());
    final MultipleCurrencyAmount pvCalculator = YEAR_ON_YEAR_NO.accept(PVIC, MARKET.getInflationProvider());
    assertEquals("Year on year coupon inflation: Present value", pvMethod, pvCalculator);
  }

  @Test
  /**
   * Tests the present value.
   */
  public void presentValueWithNotional() {
    final MultipleCurrencyAmount pv = METHOD.presentValue(YEAR_ON_YEAR_WITH, MARKET.getInflationProvider());
    final double df = MARKET.getCurve(YEAR_ON_YEAR_WITH.getCurrency()).getDiscountFactor(YEAR_ON_YEAR_WITH.getPaymentTime());
    final double finalIndex = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(YEAR_ON_YEAR_WITH.getReferenceEndTime());
    final double initialIndex = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(YEAR_ON_YEAR_WITH.getReferenceStartTime());
    final double pvExpected = (finalIndex / initialIndex) * df * NOTIONAL;
    assertEquals("Year on year coupon inflation: Present value", pvExpected, pv.getAmount(YEAR_ON_YEAR_WITH.getCurrency()), 1.0E-2);
  }

  @Test
  /**
   * Tests the net amount.
   */
  public void netAmountWithNotional() {
    final MultipleCurrencyAmount pv = METHOD.netAmount(YEAR_ON_YEAR_WITH, MARKET.getInflationProvider());
    final double finalIndex = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(YEAR_ON_YEAR_WITH.getReferenceEndTime());
    final double initialIndex = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(YEAR_ON_YEAR_NO.getReferenceStartTime());
    final double pvExpected = (finalIndex / initialIndex) * NOTIONAL;
    assertEquals("Year on year coupon inflation: net amount", pvExpected, pv.getAmount(YEAR_ON_YEAR_WITH.getCurrency()), 1.0E-2);
  }

  @Test
  /**
   * Tests the net amount: Method vs Calculator.
   */
  public void netAmountMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD.netAmount(YEAR_ON_YEAR_NO, MARKET.getInflationProvider());
    final MultipleCurrencyAmount pvCalculator = YEAR_ON_YEAR_NO.accept(NAIC, MARKET.getInflationProvider());
    assertEquals("Year on year coupon inflation: Net amount", pvMethod, pvCalculator);
  }
}
