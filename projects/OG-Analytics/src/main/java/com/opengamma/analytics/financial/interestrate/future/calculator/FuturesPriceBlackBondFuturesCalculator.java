/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.calculator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.provider.BondFuturesSecurityDiscountingMethod;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Computes the price for different types of futures. Calculator using a multi-curve and issuer provider.
 */
public final class FuturesPriceBlackBondFuturesCalculator extends InstrumentDerivativeVisitorAdapter<BlackBondFuturesProviderInterface, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final FuturesPriceBlackBondFuturesCalculator INSTANCE = new FuturesPriceBlackBondFuturesCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static FuturesPriceBlackBondFuturesCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private FuturesPriceBlackBondFuturesCalculator() {
  }

  /** The Black function used in the pricing. */
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();
  /** The method used to compute the future price. */
  private static final BondFuturesSecurityDiscountingMethod METHOD_FUTURE = BondFuturesSecurityDiscountingMethod.getInstance();

  //     -----     Futures options    -----

  @Override
  public Double visitBondFuturesOptionMarginSecurity(final BondFuturesOptionMarginSecurity security, final BlackBondFuturesProviderInterface black) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(black, "Black  data");
    final double priceFutures = METHOD_FUTURE.price(security.getUnderlyingFuture(), black.getIssuerProvider());
    final double strike = security.getStrike();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, security.getExpirationTime(), security.isCall());
    final double delay = security.getUnderlyingFuture().getNoticeLastTime() - security.getExpirationTime();
    final double volatility = black.getVolatility(security.getExpirationTime(), delay, strike, priceFutures);
    final BlackFunctionData dataBlack = new BlackFunctionData(priceFutures, 1.0, volatility);
    final double priceSecurity = BLACK_FUNCTION.getPriceFunction(option).evaluate(dataBlack);
    return priceSecurity;
  }

}
