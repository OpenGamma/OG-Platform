/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.calculator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.sensitivity.PresentValueBlackSTIRFuturesCubeSensitivity;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSTIRFuturesProviderInterface;
import com.opengamma.analytics.util.amount.CubeValue;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Triple;

/**
 * Computes the price for different types of futures. Calculator using a multi-curve and issuer provider.
 */
public final class FuturesPriceBlackSensitivityBlackSTIRFuturesCalculator extends InstrumentDerivativeVisitorAdapter<BlackSTIRFuturesProviderInterface, PresentValueBlackSTIRFuturesCubeSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final FuturesPriceBlackSensitivityBlackSTIRFuturesCalculator INSTANCE = new FuturesPriceBlackSensitivityBlackSTIRFuturesCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static FuturesPriceBlackSensitivityBlackSTIRFuturesCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private FuturesPriceBlackSensitivityBlackSTIRFuturesCalculator() {
  }

  /** The Black function used in the pricing. */
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();
  /** The method used to compute the future price. It is a method without convexity adjustment. */
  private static final InterestRateFutureSecurityDiscountingMethod METHOD_FUTURE = InterestRateFutureSecurityDiscountingMethod.getInstance();

  //     -----     Futures options    -----

  @Override
  public PresentValueBlackSTIRFuturesCubeSensitivity visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity security,
      final BlackSTIRFuturesProviderInterface black) {
    ArgumentChecker.notNull(security, "Option security");
    ArgumentChecker.notNull(black, "Black data");
    // Forward sweep
    final double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), black.getMulticurveProvider());
    final double strike = security.getStrike();
    final double rateStrike = 1.0 - strike;
    final EuropeanVanillaOption option = new EuropeanVanillaOption(rateStrike, security.getExpirationTime(), !security.isCall());
    final double forward = 1 - priceFuture;
    final double delay = security.getUnderlyingFuture().getTradingLastTime() - security.getExpirationTime();
    final double volatility = black.getVolatility(security.getExpirationTime(), delay, security.getStrike(), rateStrike);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatility);
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    // Backward sweep
    final double priceBar = 1.0;
    final double volatilityBar = priceAdjoint[2] * priceBar;
    final Triple<Double, Double, Double> expiryDelayStrike = Triple.of(security.getExpirationTime(), delay, strike);
    final CubeValue sensi = CubeValue.from(expiryDelayStrike, volatilityBar);
    return new PresentValueBlackSTIRFuturesCubeSensitivity(sensi, security.getUnderlyingFuture().getIborIndex());
  }

}
