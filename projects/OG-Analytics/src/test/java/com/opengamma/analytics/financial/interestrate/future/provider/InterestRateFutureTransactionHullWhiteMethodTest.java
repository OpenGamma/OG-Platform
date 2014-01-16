/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.MarketQuoteHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.PresentValueCurveSensitivityHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.PresentValueHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.hullwhite.ParameterSensitivityHullWhiteDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests for the methods related to interest rate securities pricing with Hull-White model convexity adjustment.
 */
@Test(groups = TestGroup.UNIT)
public class InterestRateFutureTransactionHullWhiteMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex[] INDEX_LIST = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd();
  private static final IborIndex EURIBOR3M = INDEX_LIST[0];
  private static final Currency EUR = EURIBOR3M.getCurrency();
  private static final Calendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();
  // Future
  private static final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtils.getUTCDate(2012, 9, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, -EURIBOR3M.getSpotLag(), CALENDAR);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final String NAME = "ERU2";
  private static final int QUANTITY = 400;

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 5, 12);
  private static final ZonedDateTime TRADE_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, -1, CALENDAR);
  private static final double TRADE_PRICE = 0.99;
  private static final InterestRateFutureSecurityDefinition ERU2_SEC_DEFINITION = new InterestRateFutureSecurityDefinition(LAST_TRADING_DATE, EURIBOR3M, NOTIONAL, FUTURE_FACTOR, NAME, CALENDAR);
  private static final InterestRateFutureTransactionDefinition ERU2_TRA_DEFINITION = new InterestRateFutureTransactionDefinition(ERU2_SEC_DEFINITION, QUANTITY, TRADE_DATE, TRADE_PRICE);

  private static final double REFERENCE_PRICE = 0.98;
  private static final InterestRateFutureSecurity ERU2_SEC = ERU2_SEC_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final InterestRateFutureTransaction ERU2_TRA = ERU2_TRA_DEFINITION.toDerivative(REFERENCE_DATE, REFERENCE_PRICE);

  private static final double MEAN_REVERSION = 0.01;
  private static final double[] VOLATILITY = new double[] {0.01, 0.011, 0.012, 0.013, 0.014 };
  private static final double[] VOLATILITY_TIME = new double[] {0.5, 1.0, 2.0, 5.0 };
  private static final HullWhiteOneFactorPiecewiseConstantParameters MODEL_PARAMETERS = new HullWhiteOneFactorPiecewiseConstantParameters(MEAN_REVERSION, VOLATILITY, VOLATILITY_TIME);

  private static final HullWhiteOneFactorProviderDiscount HW_MULTICURVES = new HullWhiteOneFactorProviderDiscount(MULTICURVES, MODEL_PARAMETERS, EUR);

  private static final InterestRateFutureTransactionHullWhiteMethod METHOD_IRFUT_TRA_HW = InterestRateFutureTransactionHullWhiteMethod.getInstance();
  private static final InterestRateFutureSecurityHullWhiteMethod METHOD_IRFUT_SEC_HW = InterestRateFutureSecurityHullWhiteMethod.getInstance();

  private static final MarketQuoteHullWhiteCalculator MQHWC = MarketQuoteHullWhiteCalculator.getInstance();
  private static final PresentValueHullWhiteCalculator PVHWC = PresentValueHullWhiteCalculator.getInstance();
  private static final PresentValueCurveSensitivityHullWhiteCalculator PVCSHWC = PresentValueCurveSensitivityHullWhiteCalculator.getInstance();

  private static final double SHIFT_FD = 1.0E-6;
  private static final ParameterSensitivityParameterCalculator<HullWhiteOneFactorProviderInterface> PSHWC = new ParameterSensitivityParameterCalculator<>(PVCSHWC);
  private static final ParameterSensitivityHullWhiteDiscountInterpolatedFDCalculator PSHWC_FD = new ParameterSensitivityHullWhiteDiscountInterpolatedFDCalculator(PVHWC, SHIFT_FD);

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2;
  private static final double TOLERANCE_PRICE = 1.0E-10;

  @Test
  /**
   * Test the present value computed from the curves and HW parameters.
   */
  public void presentValue() {
    final MultipleCurrencyAmount pvComputed = METHOD_IRFUT_TRA_HW.presentValue(ERU2_TRA, HW_MULTICURVES);
    final double price = METHOD_IRFUT_SEC_HW.price(ERU2_SEC, HW_MULTICURVES);
    final double pvExpected = (price - REFERENCE_PRICE) * ERU2_SEC.getNotional() * ERU2_SEC.getPaymentAccrualFactor() * QUANTITY;
    assertEquals("InterestRateFutureSecurityHullWhiteProviderMethod: present value", pvExpected, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD_IRFUT_TRA_HW.presentValue(ERU2_TRA, HW_MULTICURVES);
    final MultipleCurrencyAmount pvCalculator = ERU2_TRA.accept(PVHWC, HW_MULTICURVES);
    assertEquals("InterestRateFutureSecurityHullWhiteProviderMethod: present value - calculator vs method", pvCalculator.getAmount(EUR), pvMethod.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Test the price as "MarketQuote"
   */
  public void marketQuote() {
    final double priceMethod = METHOD_IRFUT_SEC_HW.price(ERU2_SEC, HW_MULTICURVES);
    final double marketQuote = ERU2_TRA.accept(MQHWC, HW_MULTICURVES);
    assertEquals("InterestRateFutureSecurityHullWhiteProviderMethod: price", priceMethod, marketQuote, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests present value curve sensitivity.
   */
  public void presentValueCurveSensitivity() {
    final MultipleCurrencyParameterSensitivity pvpsDepositExact = PSHWC.calculateSensitivity(ERU2_TRA, HW_MULTICURVES, HW_MULTICURVES.getMulticurveProvider().getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsDepositFD = PSHWC_FD.calculateSensitivity(ERU2_TRA, HW_MULTICURVES);
    AssertSensivityObjects.assertEquals("CashDiscountingProviderMethod: presentValueCurveSensitivity ", pvpsDepositExact, pvpsDepositFD, TOLERANCE_PV_DELTA);
  }

  //  @Test
  //  /**
  //   * Compare the price with a price without convexity adjustment.
  //   */
  //  public void comparisonDiscounting() {
  //    final InterestRateFutureDiscountingMethod methodDiscounting = InterestRateFutureDiscountingMethod.getInstance();
  //    final double priceDiscounting = methodDiscounting.price(ERU2, BUNDLE_HW);
  //    final double priceHullWhite = METHOD.price(ERU2, BUNDLE_HW);
  //    assertTrue("Future price comparison with no convexity adjustment", priceDiscounting > priceHullWhite);
  //  }

}
