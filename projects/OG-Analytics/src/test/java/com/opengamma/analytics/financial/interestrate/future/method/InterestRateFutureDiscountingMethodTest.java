/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.method;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.ParSpreadMarketQuoteCalculator;
import com.opengamma.analytics.financial.interestrate.ParSpreadRateCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.future.calculator.PriceFromCurvesDiscountingCalculator;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests for the methods related to interest rate securities pricing without convexity adjustment.
 * @deprecated This class tests deprecated functionality
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class InterestRateFutureDiscountingMethodTest {
  // EURIBOR 3M Index
  private static final Calendar TARGET = new MondayToFridayCalendar("TARGET");
  private static final IborIndex EURIBOR3M = IndexIborMaster.getInstance().getIndex("EURIBOR3M");
  // Future
  private static final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtils.getUTCDate(2012, 9, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, -EURIBOR3M.getSpotLag(), TARGET);
  private static final ZonedDateTime FIXING_END_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, EURIBOR3M, TARGET);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final double REFERENCE_PRICE = 0.99;
  private static final String NAME = "ERU2";
  private static final int QUANTITY = 123;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 5, 12);
  private static final double LAST_TRADING_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, LAST_TRADING_DATE);
  private static final double FIXING_START_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, SPOT_LAST_TRADING_DATE);
  private static final double FIXING_END_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, FIXING_END_DATE);
  private static final double FIXING_ACCRUAL = EURIBOR3M.getDayCount().getDayCountFraction(SPOT_LAST_TRADING_DATE, FIXING_END_DATE);
  private static final String DISCOUNTING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final InterestRateFutureSecurity ERU2_SEC = new InterestRateFutureSecurity(LAST_TRADING_TIME, EURIBOR3M, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, NOTIONAL,
      FUTURE_FACTOR, NAME, DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME);
  private static final InterestRateFutureTransaction ERU2_TRA = new InterestRateFutureTransaction(ERU2_SEC, REFERENCE_PRICE, QUANTITY);

  private static final InterestRateFutureSecurityDiscountingMethod METHOD_FUT_SEC = InterestRateFutureSecurityDiscountingMethod.getInstance();
  private static final InterestRateFutureTransactionDiscountingMethod METHOD_FUT_TRA = InterestRateFutureTransactionDiscountingMethod.getInstance();

  private static final PriceFromCurvesDiscountingCalculator PRICE_CALCULATOR = PriceFromCurvesDiscountingCalculator.getInstance();
  private static final ParRateCurveSensitivityCalculator PRCSC = ParRateCurveSensitivityCalculator.getInstance();
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final PresentValueCurveSensitivityCalculator PVCSC = PresentValueCurveSensitivityCalculator.getInstance();
  private static final ParSpreadMarketQuoteCalculator PSMQC = ParSpreadMarketQuoteCalculator.getInstance();
  private static final ParSpreadRateCalculator PSRC = ParSpreadRateCalculator.getInstance();

  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2;
  private static final double TOLERANCE_PRICE = 1.0E-10;
  private static final double TOLERANCE_PRICE_DELTA = 1.0E-8;

  @Test
  /**
   * Test the price computed from the curves
   */
  public void price() {
    final double price = METHOD_FUT_SEC.price(ERU2_SEC, CURVES);
    final YieldAndDiscountCurve forwardCurve = CURVES.getCurve(FORWARD_CURVE_NAME);
    final double forward = (forwardCurve.getDiscountFactor(FIXING_START_TIME) / forwardCurve.getDiscountFactor(FIXING_END_TIME) - 1) / FIXING_ACCRUAL;
    final double expectedPrice = 1.0 - forward;
    assertEquals("Future price from curves", expectedPrice, price, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests the method versus the calculator for the price.
   */
  public void priceMethodVsCalculator() {
    final double priceMethod = METHOD_FUT_SEC.price(ERU2_SEC, CURVES);
    final double priceCalculator = ERU2_SEC.accept(PRICE_CALCULATOR, CURVES);
    assertEquals("Bond future security Discounting: Method vs calculator", priceMethod, priceCalculator, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Test the present value computed from the curves
   */
  public void presentValue() {
    final double price = METHOD_FUT_SEC.price(ERU2_SEC, CURVES);
    final double pvExpected = (price - ERU2_TRA.getReferencePrice()) * NOTIONAL * FUTURE_FACTOR * QUANTITY;
    final CurrencyAmount pvComputed = METHOD_FUT_TRA.presentValue(ERU2_TRA, CURVES);
    assertEquals("InterestRateFutureXXXDiscountingMethod: present value", pvComputed.getAmount(), pvExpected, TOLERANCE_PV);
  }

  @Test
  public void presentValueMethodVsCalculator() {
    final CurrencyAmount pvMethod = METHOD_FUT_TRA.presentValue(ERU2_TRA, CURVES);
    final double pvCalculator = ERU2_TRA.accept(PVC, CURVES);
    assertEquals("InterestRateFutureXXXDiscountingMethod: present value", pvMethod.getAmount(), pvCalculator, TOLERANCE_PV);
  }

  @Test
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final InterestRateCurveSensitivity pvcsMethod = METHOD_FUT_TRA.presentValueCurveSensitivity(ERU2_TRA, CURVES);
    final InterestRateCurveSensitivity pvcsCalculator = new InterestRateCurveSensitivity(ERU2_TRA.accept(PVCSC, CURVES));
    AssertSensitivityObjects.assertEquals("InterestRateFutureXXXDiscountingMethod: present value", pvcsMethod, pvcsCalculator, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Test the rate computed from the curves
   */
  public void parRate() {
    final double rate = METHOD_FUT_SEC.parRate(ERU2_SEC, CURVES);
    final YieldAndDiscountCurve forwardCurve = CURVES.getCurve(FORWARD_CURVE_NAME);
    final double expectedRate = (forwardCurve.getDiscountFactor(FIXING_START_TIME) / forwardCurve.getDiscountFactor(FIXING_END_TIME) - 1) / FIXING_ACCRUAL;
    assertEquals("Future price from curves", expectedRate, rate, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Test the rate computed from the method and from the calculator.
   */
  public void parRateMethodVsCalculator() {
    final double rateMethod = METHOD_FUT_SEC.parRate(ERU2_SEC, CURVES);
    final ParRateCalculator calculator = ParRateCalculator.getInstance();
    final double rateCalculator = ERU2_SEC.accept(calculator, CURVES);
    assertEquals("Future price from curves", rateMethod, rateCalculator, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Test the rate computed from the curves
   */
  public void parRateCurveSensitivityMethodVsCalculator() {
    final InterestRateCurveSensitivity prSensiMethod = METHOD_FUT_SEC.parRateCurveSensitivity(ERU2_SEC, CURVES);
    final InterestRateCurveSensitivity prSensiCalculator = new InterestRateCurveSensitivity(ERU2_SEC.accept(PRCSC, CURVES));
    AssertSensitivityObjects.assertEquals("", prSensiMethod, prSensiCalculator, TOLERANCE_PV_DELTA);
    final InterestRateCurveSensitivity prSensiCalculator2 = new InterestRateCurveSensitivity(ERU2_TRA.accept(PRCSC, CURVES));
    AssertSensitivityObjects.assertEquals("", prSensiMethod, prSensiCalculator2, TOLERANCE_PRICE_DELTA);
  }

  @Test
  /**
   * Test the par spread for market quote.
   */
  public void parSpreadMarketQuote() {
    final double parSpread = ERU2_TRA.accept(PSMQC, CURVES);
    final InterestRateFutureTransaction futures0 = new InterestRateFutureTransaction(ERU2_SEC, REFERENCE_PRICE + parSpread, QUANTITY);
    final CurrencyAmount pv0 = METHOD_FUT_TRA.presentValue(futures0, CURVES);
    assertEquals("Future par spread market quote", pv0.getAmount(), 0, TOLERANCE_PV);
  }

  @Test
  /**
   * Test the par spread for rate.
   */
  public void parSpreadRate() {
    final double parSpread = ERU2_TRA.accept(PSRC, CURVES);
    final InterestRateFutureTransaction futures0 = new InterestRateFutureTransaction(ERU2_SEC, REFERENCE_PRICE - parSpread, QUANTITY);
    final CurrencyAmount pv0 = METHOD_FUT_TRA.presentValue(futures0, CURVES);
    assertEquals("Future par spread rate", pv0.getAmount(), 0, TOLERANCE_PV);
    final double parSpreadMQ = ERU2_TRA.accept(PSMQC, CURVES);
    assertEquals("InterestRateFutureXXXDiscountingMethod: par spread", parSpread, -parSpreadMQ, TOLERANCE_PRICE);
  }

}
