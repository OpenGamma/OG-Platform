/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.market;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.CouponOISDefinition;
import com.opengamma.analytics.financial.interestrate.market.calculator.PresentValueCurveSensitivityMarketCalculator;
import com.opengamma.analytics.financial.interestrate.market.calculator.PresentValueMarketCalculator;
import com.opengamma.analytics.financial.interestrate.market.description.IMarketBundle;
import com.opengamma.analytics.financial.interestrate.market.description.MarketDiscountDataSets;
import com.opengamma.analytics.financial.interestrate.market.description.MultipleCurrencyCurveSensitivityMarket;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponOIS;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;

/**
 * Tests related to the pricing methods for OIS coupon in the discounting method with data in MarketBundle.
 */
public class CouponOISDiscountingMarketMethodTest {

  private static final IMarketBundle MARKET = MarketDiscountDataSets.createMarket1();

  private static final Calendar TARGET = new MondayToFridayCalendar("TARGET");
  private static final GeneratorSwapFixedON GENERATOR_SWAP_EONIA = GeneratorSwapFixedONMaster.getInstance().getGenerator("EUR1YEONIA", TARGET);
  private static final IndexON EONIA = MarketDiscountDataSets.getIndexesON()[0];
  private static final Currency EUR = EONIA.getCurrency();
  private static final ZonedDateTime EFFECTIVE_DATE = DateUtils.getUTCDate(2011, 5, 23);
  private static final Period TENOR = Period.ofMonths(3);
  private static final double NOTIONAL = 100000000; // 100m
  private static final CouponOISDefinition CPN_OIS_DEFINITION = CouponOISDefinition.from(EONIA, EFFECTIVE_DATE, TENOR, NOTIONAL, 2, GENERATOR_SWAP_EONIA.getBusinessDayConvention(),
      GENERATOR_SWAP_EONIA.isEndOfMonth());

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 12, 27);
  private static final String[] NOT_USED = new String[] {"Not used 1", "not used 2"};
  private static final CouponOIS CPN_OIS = CPN_OIS_DEFINITION.toDerivative(REFERENCE_DATE, NOT_USED);

  private static final CouponOISDiscountingMarketMethod METHOD_CPN_OIS = CouponOISDiscountingMarketMethod.getInstance();
  private static final PresentValueMarketCalculator PVC = PresentValueMarketCalculator.getInstance();
  private static final PresentValueCurveSensitivityMarketCalculator PVCSC = PresentValueCurveSensitivityMarketCalculator.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_DELTA = 1.0E+2;

  @Test
  public void presentValue() {
    MultipleCurrencyAmount pvComputed = METHOD_CPN_OIS.presentValue(CPN_OIS, MARKET);
    double forward = MARKET.getForwardRate(EONIA, CPN_OIS.getFixingPeriodStartTime(), CPN_OIS.getFixingPeriodEndTime(), CPN_OIS.getFixingPeriodAccrualFactor());
    double pvExpected = NOTIONAL * CPN_OIS.getFixingPeriodAccrualFactor() * forward * MARKET.getDiscountFactor(CPN_OIS.getCurrency(), CPN_OIS.getPaymentTime());
    assertEquals("CouponOISDiscountingMarketMethod: present value", pvExpected, pvComputed.getAmount(EONIA.getCurrency()), TOLERANCE_PV);
  }

  @Test
  public void presentValueStarted() {
    final double fixing = 0.0015;
    final ArrayZonedDateTimeDoubleTimeSeries TS_ON = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 5, 20), DateUtils.getUTCDate(2011, 5, 23)}, new double[] {
        0.0010, fixing});
    final ZonedDateTime referenceDate = ScheduleCalculator.getAdjustedDate(EFFECTIVE_DATE, 1, TARGET);
    final CouponOIS cpnOISStarted = (CouponOIS) CPN_OIS_DEFINITION.toDerivative(referenceDate, TS_ON, NOT_USED);
    double notionalAccrued = NOTIONAL * (1 + fixing * EONIA.getDayCount().getDayCountFraction(EFFECTIVE_DATE, referenceDate));
    assertEquals("CouponOISDiscountingMarketMethod: present value", notionalAccrued, cpnOISStarted.getNotionalAccrued(), TOLERANCE_PV);
    MultipleCurrencyAmount pvComputed = METHOD_CPN_OIS.presentValue(cpnOISStarted, MARKET);
    double forward = MARKET.getForwardRate(EONIA, cpnOISStarted.getFixingPeriodStartTime(), cpnOISStarted.getFixingPeriodEndTime(), cpnOISStarted.getFixingPeriodAccrualFactor());
    double pvExpected = (cpnOISStarted.getNotionalAccrued() * (1 + cpnOISStarted.getFixingPeriodAccrualFactor() * forward) - NOTIONAL)
        * MARKET.getDiscountFactor(cpnOISStarted.getCurrency(), cpnOISStarted.getPaymentTime());
    assertEquals("CouponOISDiscountingMarketMethod: present value", pvExpected, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  public void presentValueMethodVsCalculator() {
    MultipleCurrencyAmount pvMethod = METHOD_CPN_OIS.presentValue(CPN_OIS, MARKET);
    MultipleCurrencyAmount pvCalculator = PVC.visit(CPN_OIS, MARKET);
    assertEquals("CouponFixedDiscountingMarketMethod: present value", pvMethod.getAmount(EUR), pvCalculator.getAmount(EUR), TOLERANCE_PV);
  }

  // Testing note: the presentValueMarketSensitivity is tested in ParameterSensitivityMarketCalculatorTest

  @Test
  public void presentValueMarketSensitivityMethodVsCalculator() {
    MultipleCurrencyCurveSensitivityMarket pvcsMethod = METHOD_CPN_OIS.presentValueMarketSensitivity(CPN_OIS, MARKET);
    MultipleCurrencyCurveSensitivityMarket pvcsCalculator = PVCSC.visit(CPN_OIS, MARKET);
    AssertSensivityObjects.assertEquals("CouponFixedDiscountingMarketMethod: presentValueMarketSensitivity", pvcsMethod, pvcsCalculator, TOLERANCE_DELTA);
  }

}
