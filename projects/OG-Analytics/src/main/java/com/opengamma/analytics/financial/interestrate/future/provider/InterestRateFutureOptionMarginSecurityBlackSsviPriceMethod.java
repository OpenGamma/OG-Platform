/**
 * Copyright (C) 2016 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackStirFuturesSsviPriceProvider;
import com.opengamma.analytics.math.differentiation.ValueDerivatives;
import com.opengamma.util.ArgumentChecker;

/**
 * Extends the {@link InterestRateFutureOptionMarginSecurityBlackRateMethod} to compute the sensitivity with 
 * respect to SSVI parameters.
 */
public final class InterestRateFutureOptionMarginSecurityBlackSsviPriceMethod 
    extends InterestRateFutureOptionMarginSecurityBlackPriceMethod {

  /**
   * Creates the method unique instance.
   */
  private static final InterestRateFutureOptionMarginSecurityBlackSsviPriceMethod INSTANCE = 
      new InterestRateFutureOptionMarginSecurityBlackSsviPriceMethod();

  /**
   * Constructor.
   */
  private InterestRateFutureOptionMarginSecurityBlackSsviPriceMethod() {
  }

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static InterestRateFutureOptionMarginSecurityBlackSsviPriceMethod getInstance() {
    return INSTANCE;
  }

  /** The Black function used in the pricing. */
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();
  /** The method used to compute the future price. It is a method without convexity adjustment. */
  private static final InterestRateFutureSecurityDiscountingMethod METHOD_FUTURE = 
      InterestRateFutureSecurityDiscountingMethod.getInstance();
  
  /**
   * Computes the option security price SSVI formula parameters sensitivity. 
   * The future price is computed without convexity adjustment.
   * @param security The future option security.
   * @param ssviData The curve and SSVI formula data.
   * @return The security price SSVI parameters sensitivity. The sensitivities are to the ATM Black volatility, 
   * rho parameter and eta parameter.
   */
  public ValueDerivatives priceSsviSensitivity(InterestRateFutureOptionMarginSecurity security, 
      BlackStirFuturesSsviPriceProvider ssviData) {
    ArgumentChecker.notNull(security, "Option security");
    ArgumentChecker.notNull(ssviData, "SSVI data");
    // Forward sweep
    final double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), ssviData.getMulticurveProvider());
    final double strike = security.getStrike();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, security.getExpirationTime(), security.isCall());
    final double delay = security.getUnderlyingFuture().getTradingLastTime() - security.getExpirationTime();
    ValueDerivatives volatilityAd = ssviData
        .volatilityAdjoint(security.getExpirationTime(), delay, security.getStrike(), priceFuture);
    final BlackFunctionData dataBlack = new BlackFunctionData(priceFuture, 1.0, volatilityAd.getValue());
    double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    // Backward sweep
    double priceBar = 1.0;
    double volatilityBar = priceAdjoint[2] * priceBar;
    double[] derivatives = new double[3];
    for (int i = 0; i < 3; i++) {
      derivatives[i] = volatilityAd.getDerivatives()[i + 3] * volatilityBar;
    }
    return ValueDerivatives.of(priceAdjoint[0], derivatives);
  }

}
