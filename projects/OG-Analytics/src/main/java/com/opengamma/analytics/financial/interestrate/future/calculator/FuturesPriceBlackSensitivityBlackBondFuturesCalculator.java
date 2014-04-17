/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.calculator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.provider.BondFuturesSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.sensitivity.PresentValueBlackBondFuturesCubeSensitivity;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesProviderInterface;
import com.opengamma.analytics.util.amount.CubeValue;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Triple;

/**
 * Computes the price for different types of futures. Calculator using a multi-curve and issuer provider.
 */
public final class FuturesPriceBlackSensitivityBlackBondFuturesCalculator extends InstrumentDerivativeVisitorAdapter<BlackBondFuturesProviderInterface, PresentValueBlackBondFuturesCubeSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final FuturesPriceBlackSensitivityBlackBondFuturesCalculator INSTANCE = new FuturesPriceBlackSensitivityBlackBondFuturesCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static FuturesPriceBlackSensitivityBlackBondFuturesCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private FuturesPriceBlackSensitivityBlackBondFuturesCalculator() {
  }

  /** The Black function used in the pricing. */
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();
  /** The method used to compute the future price. */
  private static final BondFuturesSecurityDiscountingMethod METHOD_FUTURE = BondFuturesSecurityDiscountingMethod.getInstance();

  //     -----     Futures options    -----

  @Override
  public PresentValueBlackBondFuturesCubeSensitivity visitBondFuturesOptionMarginSecurity(final BondFuturesOptionMarginSecurity security, 
      final BlackBondFuturesProviderInterface black) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(black, "Black  data");
    final double priceFutures = METHOD_FUTURE.price(security.getUnderlyingFuture(), black.getIssuerProvider());
    // Forward sweep
    final double strike = security.getStrike();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, security.getExpirationTime(), security.isCall());
    final double delay = security.getUnderlyingFuture().getNoticeLastTime() - security.getExpirationTime();
    final double volatility = black.getVolatility(security.getExpirationTime(), delay, strike, priceFutures);
    final BlackFunctionData dataBlack = new BlackFunctionData(priceFutures, 1.0, volatility);
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    // Backward sweep
    final double priceBar = 1.0;
    final double volBar = priceAdjoint[2] * priceBar;
    final CubeValue blackSensi = new CubeValue();
    blackSensi.add(Triple.of(security.getExpirationTime(), delay, strike), volBar);
    return new PresentValueBlackBondFuturesCubeSensitivity(blackSensi, security.getCurrency(), black.getLegalEntity());
  }

}
