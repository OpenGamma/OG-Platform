/**
 * Copyright (C) 2016 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.provider.description.BlackDataSets;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSTIRFuturesSmileProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link InterestRateFutureOptionMarginSecurityBlackPriceMethod}.
 */
@Test(groups = TestGroup.UNIT)
public class InterestRateFutureOptionMarginSecurityBlackPriceMethodTest {
  
  private static final InterestRateFutureOptionMarginSecurityDefinition OPTION_ERU2_DEFINITION =
      InterestRateFutureOptionMarginBlackRateMethodTest.OPTION_ERU2_DEFINITION;
  private static final InterestRateFutureOptionMarginSecurity OPTION_ERU2 =
      InterestRateFutureOptionMarginBlackRateMethodTest.OPTION_ERU2;
  private static final IborIndex INDEX = OPTION_ERU2_DEFINITION.getUnderlyingFuture().getIborIndex();

  private static final MulticurveProviderDiscount MULTICURVE =
      MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final InterpolatedDoublesSurface BLACK_PARAMETERS = 
      BlackDataSets.createBlackSurfaceExpiryStrikePrice();
  private static final BlackSTIRFuturesSmileProviderDiscount BLACK_MULTICURVE = 
      new BlackSTIRFuturesSmileProviderDiscount(MULTICURVE, BLACK_PARAMETERS, INDEX);
  
  private static final InterestRateFutureSecurityDiscountingMethod METHOD_FUTURES = 
      InterestRateFutureSecurityDiscountingMethod.getInstance();
  private static final InterestRateFutureOptionMarginSecurityBlackPriceMethod METHOD_SECURITY_OPTION_BLACK = 
      InterestRateFutureOptionMarginSecurityBlackPriceMethod.getInstance();
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();


  private static final double TOLERANCE_PRICE = 1.0E-8;
  private static final double TOLERANCE_PRICE_DELTA = 1.0E-4;

  @Test
  public void price() {
    double expiry = OPTION_ERU2.getExpirationTime();
    EuropeanVanillaOption option = new EuropeanVanillaOption(OPTION_ERU2.getStrike(), expiry, OPTION_ERU2.isCall());
    double priceFuture = METHOD_FUTURES.price(OPTION_ERU2.getUnderlyingFuture(), MULTICURVE);
    final double volatility = BLACK_PARAMETERS.getZValue(expiry, OPTION_ERU2.getStrike());
    final BlackFunctionData dataBlack = new BlackFunctionData(priceFuture, 1.0, volatility);
    final double priceExpected = BLACK_FUNCTION.getPriceFunction(option).evaluate(dataBlack);
    final double priceComputed = METHOD_SECURITY_OPTION_BLACK.price(OPTION_ERU2, BLACK_MULTICURVE);
    assertEquals("Future option with Black volatilities: option security price", 
        priceExpected, priceComputed, TOLERANCE_PRICE);
  }

  @Test
  public void implied_volatility() {
    double expiry = OPTION_ERU2.getExpirationTime();
    final double volatility = BLACK_PARAMETERS.getZValue(expiry, OPTION_ERU2.getStrike());
    double ivSsvi = METHOD_SECURITY_OPTION_BLACK.impliedVolatility(OPTION_ERU2, BLACK_MULTICURVE);
    assertEquals("SSVI formula: implied volatility", ivSsvi, volatility, TOLERANCE_PRICE);
  }
  
  @Test
  public void black_sensitivity() {
    double shift = 1.0E-5;
    double price0 = METHOD_SECURITY_OPTION_BLACK.price(OPTION_ERU2, BLACK_MULTICURVE);
    BlackSTIRFuturesSmileProviderDiscount blackMulticurveP = new BlackSTIRFuturesSmileProviderDiscount(
        MULTICURVE, BlackDataSets.createBlackSurfaceExpiryStrikePrice(shift), INDEX);
    double priceP = METHOD_SECURITY_OPTION_BLACK.price(OPTION_ERU2, blackMulticurveP);
    SurfaceValue vega = METHOD_SECURITY_OPTION_BLACK.priceBlackSensitivity(OPTION_ERU2, BLACK_MULTICURVE);
    assertEquals("SSVI formula: Black sensitivity", 
        (priceP - price0)/shift, vega.toSingleValue(), TOLERANCE_PRICE_DELTA);
  }
  
}
