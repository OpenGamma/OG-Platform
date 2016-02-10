/**
 * Copyright (C) 2016 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginSecurity;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesSsviPriceProvider;
import com.opengamma.analytics.math.differentiation.ValueDerivatives;
import com.opengamma.util.ArgumentChecker;

/**
 * Method for the pricing of bond future options with margin process. The pricing is done with a Black approach and
 * a smile described by a SSVI formula.
 */
public class BondFutureOptionMarginSecurityBlackSsviPriceMethod 
    extends BondFutureOptionMarginSecurityBlackPriceMethod {

  /** The method default instance. */
  public static final BondFutureOptionMarginSecurityBlackSsviPriceMethod DEFAULT =
      new BondFutureOptionMarginSecurityBlackSsviPriceMethod();

  /** The Black function used in the pricing. */
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  /**
   * Default constructor.
   */
  private BondFutureOptionMarginSecurityBlackSsviPriceMethod() {
  }

  /**
   * Constructor from a particular bond futures method. The method is used to compute the price and price curve
   * sensitivity of the underlying futures.
   * @param methodFutures The bond futures method.
   */
  public BondFutureOptionMarginSecurityBlackSsviPriceMethod(FuturesSecurityIssuerMethod methodFutures) {
    super(methodFutures);
  }
  
  /**
   * Computes the option security price SSVI formula parameters sensitivity. 
   * The future price is computed without convexity adjustment.
   * @param security The future option security.
   * @param ssviData The curve and SSVI formula data.
   * @return The security price SSVI parameters sensitivity. The sensitivities are to the ATM Black volatility, 
   * rho parameter and eta parameter.
   */
  public ValueDerivatives priceSsviSensitivity(
      BondFuturesOptionMarginSecurity security, 
      BlackBondFuturesSsviPriceProvider ssviData) {
    ArgumentChecker.notNull(security, "Option security");
    ArgumentChecker.notNull(ssviData, "SSVI data");
    // Forward sweep
    double priceFutures = getMethodFutures().price(security.getUnderlyingFuture(), ssviData);
    final double strike = security.getStrike();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, security.getExpirationTime(), security.isCall());
    final double delay = security.getUnderlyingFuture().getTradingLastTime() - security.getExpirationTime();
    ValueDerivatives volatilityAd = ssviData
        .volatilityAdjoint(security.getExpirationTime(), delay, security.getStrike(), priceFutures);
    final BlackFunctionData dataBlack = new BlackFunctionData(priceFutures, 1.0, volatilityAd.getValue());
    double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    // Backward sweep
    double priceBar = 1.0;
    double volatilityBar = priceAdjoint[2] * priceBar;
    double[] derivatives = new double[3];
    for (int i = 0; i < 3; i++) {
      derivatives[i] = volatilityAd.getDerivatives(i + 3) * volatilityBar;
    }
    return ValueDerivatives.of(priceAdjoint[0], derivatives);
  }

}
