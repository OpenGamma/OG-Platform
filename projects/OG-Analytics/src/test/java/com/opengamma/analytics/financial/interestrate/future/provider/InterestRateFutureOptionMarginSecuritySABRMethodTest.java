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
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.SABRDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSTIRFuturesProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests the method for interest rate future option with SABR volatility parameter surfaces.
 */
@Test(groups = TestGroup.UNIT)
public class InterestRateFutureOptionMarginSecuritySABRMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex[] IBOR_INDEXES = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd();
  private static final IborIndex EURIBOR3M = IBOR_INDEXES[0];
  private static final Calendar TARGET = MulticurveProviderDiscountDataSets.getEURCalendar();

  private static final SABRInterestRateParameters SABR_PARAMETERS = SABRDataSets.createSABR1();
  private static final SABRSTIRFuturesProviderDiscount SABR_MULTICURVES = new SABRSTIRFuturesProviderDiscount(MULTICURVES, SABR_PARAMETERS, EURIBOR3M);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 8, 18);
  // Future
  private static final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtils.getUTCDate(2012, 9, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, -EURIBOR3M.getSpotLag(), TARGET);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final String NAME = "EDU2";
  private static final double STRIKE = 0.9850;
  private static final InterestRateFutureSecurityDefinition EDU2_DEFINITION = new InterestRateFutureSecurityDefinition(LAST_TRADING_DATE, EURIBOR3M, NOTIONAL, FUTURE_FACTOR, NAME, TARGET);
  private static final InterestRateFutureSecurity EDU2 = EDU2_DEFINITION.toDerivative(REFERENCE_DATE);
  // Option
  private static final ZonedDateTime EXPIRATION_DATE = DateUtils.getUTCDate(2011, 9, 16);
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;
  private static final double EXPIRATION_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, EXPIRATION_DATE);
  private static final boolean IS_CALL = true;
  private static final InterestRateFutureOptionMarginSecurity OPTION_EDU2 = new InterestRateFutureOptionMarginSecurity(EDU2, EXPIRATION_TIME, STRIKE, IS_CALL);

  private static final InterestRateFutureOptionMarginSecuritySABRMethod METHOD_OPT_FUT_SEC_SABR = InterestRateFutureOptionMarginSecuritySABRMethod.getInstance();
  private static final InterestRateFutureSecurityDiscountingMethod METHOD_DSC_FUT = InterestRateFutureSecurityDiscountingMethod.getInstance();

  @Test
  /**
   * Test the option price from the future price. Mid-curve one year option.
   */
  public void priceFromFuturePriceMidCurve() {
    final double priceFuture = 0.9905;
    final double priceOption = METHOD_OPT_FUT_SEC_SABR.priceFromFuturePrice(OPTION_EDU2, SABR_MULTICURVES, priceFuture);
    final double delay = EDU2.getTradingLastTime() - EXPIRATION_TIME;
    final double volatility = SABR_PARAMETERS.getVolatility(EXPIRATION_TIME, delay, 1 - STRIKE, 1 - priceFuture);
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final BlackFunctionData dataBlack = new BlackFunctionData(1 - priceFuture, 1.0, volatility);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(1 - STRIKE, EXPIRATION_TIME, !IS_CALL);
    final double priceOptionExpected = blackFunction.getPriceFunction(option).evaluate(dataBlack);
    assertEquals("Future option with SABR volatilities: option price from future price", priceOptionExpected, priceOption);
  }

  @Test
  /**
   * Test the option price from the future price. Standard option.
   */
  public void priceFromFuturePriceStandard() {
    final double expirationTime = ACT_ACT.getDayCountFraction(REFERENCE_DATE, LAST_TRADING_DATE);
    final InterestRateFutureOptionMarginSecurity optionEDU2Standard = new InterestRateFutureOptionMarginSecurity(EDU2, expirationTime, STRIKE, IS_CALL);
    final double priceFuture = 0.9905;
    final double priceOption = METHOD_OPT_FUT_SEC_SABR.priceFromFuturePrice(optionEDU2Standard, SABR_MULTICURVES, priceFuture);
    final double delay = 0.0;
    final double volatility = SABR_PARAMETERS.getVolatility(expirationTime, delay, 1 - STRIKE, 1 - priceFuture);
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final BlackFunctionData dataBlack = new BlackFunctionData(1 - priceFuture, 1.0, volatility);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(1 - STRIKE, expirationTime, !IS_CALL);
    final double priceOptionExpected = blackFunction.getPriceFunction(option).evaluate(dataBlack);
    assertEquals("Future option with SABR volatilities: option price from future price", priceOptionExpected, priceOption);
  }

  @Test
  /**
   * Test the option price from the future price. Standard option.
   */
  public void priceStandard() {
    final double expirationTime = ACT_ACT.getDayCountFraction(REFERENCE_DATE, LAST_TRADING_DATE);
    final InterestRateFutureOptionMarginSecurity optionEDU2Standard = new InterestRateFutureOptionMarginSecurity(EDU2, expirationTime, STRIKE, IS_CALL);
    final double priceOption = METHOD_OPT_FUT_SEC_SABR.price(optionEDU2Standard, SABR_MULTICURVES);
    final double priceFuture = METHOD_DSC_FUT.price(EDU2, MULTICURVES);
    final double priceOptionExpected = METHOD_OPT_FUT_SEC_SABR.priceFromFuturePrice(optionEDU2Standard, SABR_MULTICURVES, priceFuture);
    assertEquals("Future option with SABR volatilities: option price", priceOptionExpected, priceOption);
  }

}
