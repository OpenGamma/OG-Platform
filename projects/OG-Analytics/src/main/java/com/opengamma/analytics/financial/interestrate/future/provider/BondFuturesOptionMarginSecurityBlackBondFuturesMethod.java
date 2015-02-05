/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginSecurity;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;

/**
 * Method for the pricing of bond future options (with futures-like margin). The pricing is done with a Black approach on the bond future price.
 * The Black parameters are represented by (expiration-delay) surfaces. The delay is the time difference between the last notice and the option expiration.
 */
public final class BondFuturesOptionMarginSecurityBlackBondFuturesMethod extends FuturesSecurityBlackBondFuturesMethod {

  /**
   * Creates the method unique instance.
   */
  private static final BondFuturesOptionMarginSecurityBlackBondFuturesMethod INSTANCE = new BondFuturesOptionMarginSecurityBlackBondFuturesMethod();

  /**
   * Constructor.
   */
  private BondFuturesOptionMarginSecurityBlackBondFuturesMethod() {
  }

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static BondFuturesOptionMarginSecurityBlackBondFuturesMethod getInstance() {
    return INSTANCE;
  }

  /**
   * The Black function used in the pricing.
   */
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  /**
   * The method used to compute the future price. 
   */
  private static final BondFuturesSecurityDiscountingMethod METHOD_FUTURE = BondFuturesSecurityDiscountingMethod.getInstance();

  /**
   * Computes the option security price from future price.
   * @param security The future option security, not null
   * @param black The curve and Black volatility data, not null
   * @param price The underlying futures price.
   * @return The security price.
   */
  public double price(final BondFuturesOptionMarginSecurity security, final BlackBondFuturesProviderInterface black, final double price) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(black, "Black data");
    final double strike = security.getStrike();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, security.getExpirationTime(), security.isCall());
    final double delay = security.getUnderlyingFuture().getNoticeLastTime() - security.getExpirationTime();
    final double volatility = black.getVolatility(security.getExpirationTime(), delay, strike, price);
    final BlackFunctionData dataBlack = new BlackFunctionData(price, 1.0, volatility);
    final double priceSecurity = BLACK_FUNCTION.getPriceFunction(option).evaluate(dataBlack);
    return priceSecurity;
  }

  /**
   * Computes the option security price curve sensitivity. 
   * It is supposed that for a given strike the volatility does not change with the curves.
   * The option price and its derivative wrt the futures price is computed using the futures price. 
   * The derivatives of the futures price with respect to the curves are computed using the curves.
   * @param security The future option security, not null
   * @param black The curve and Black volatility data, not null
   * @param price The underlying futures price.
   * @return The security price curve sensitivity.
   */
  public MulticurveSensitivity priceCurveSensitivity(final BondFuturesOptionMarginSecurity security, final BlackBondFuturesProviderInterface black, final double price) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(black, "Black data");
    // Forward sweep
    final double strike = security.getStrike();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, security.getExpirationTime(), security.isCall());
    final double delay = security.getUnderlyingFuture().getNoticeLastTime() - security.getExpirationTime();
    final double volatility = black.getVolatility(security.getExpirationTime(), delay, strike, price);
    final BlackFunctionData dataBlack = new BlackFunctionData(price, 1.0, volatility);
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    // Backward sweep
    final double priceBar = 1.0;
    final double priceFutureBar = priceAdjoint[1] * priceBar;
    final MulticurveSensitivity priceFutureDerivative = METHOD_FUTURE.priceCurveSensitivity(security.getUnderlyingFuture(), black.getIssuerProvider());
    return priceFutureDerivative.multipliedBy(priceFutureBar);
  }

  /**
   * Interpolates and returns the option's implied volatility 
   * @param security The future option security, not null
   * @param black The curve and Black volatility data, not null
   * @return Lognormal Implied Volatility.
   */
  public double impliedVolatility(final BondFuturesOptionMarginSecurity security, final BlackBondFuturesProviderInterface black) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(black, "Black data");
    final double priceFutures = METHOD_FUTURE.price(security.getUnderlyingFuture(), black.getIssuerProvider());
    final double strike = security.getStrike();
    final double delay = security.getUnderlyingFuture().getNoticeLastTime() - security.getExpirationTime();
    final double volatility = black.getVolatility(security.getExpirationTime(), delay, strike, priceFutures);
    return volatility;
  }

  /**
   * Computes the underlying future security price. 
   * @param security The future option security, not null
   * @param issuerMulticurves Issuer and multi-curves provider.
   * @return The security price.
   */
  public double underlyingFuturePrice(final BondFuturesOptionMarginSecurity security, final IssuerProviderInterface issuerMulticurves) {
    ArgumentChecker.notNull(security, "security");
    return METHOD_FUTURE.price(security.getUnderlyingFuture(), issuerMulticurves);
  }

  /**
  * The theoretical delta of the option with respect to the underlying futures price.
  * @param security The future option security, not null
  * @param black The curve and Black volatility data, not null
  * @return The delta.
  */
  public double deltaUnderlyingPrice(final BondFuturesOptionMarginSecurity security, final BlackBondFuturesProviderInterface black) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(black, "Black data");
    final double priceFutures = METHOD_FUTURE.price(security.getUnderlyingFuture(), black.getIssuerProvider());
    final double strike = security.getStrike();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, security.getExpirationTime(), security.isCall());
    final double delay = security.getUnderlyingFuture().getNoticeLastTime() - security.getExpirationTime();
    final double volatility = black.getVolatility(security.getExpirationTime(), delay, strike, priceFutures);
    final BlackFunctionData dataBlack = new BlackFunctionData(priceFutures, 1.0, volatility);
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    return priceAdjoint[1];
  }
  
  /**
   * The theoretical gamma of the option with respect to the underlying futures price.
   * @param security The future option security, not null
   * @param black The curve and Black volatility data, not null
   * @return The gamma.
   */
  public double gammaUnderlyingPrice(final BondFuturesOptionMarginSecurity security, final BlackBondFuturesProviderInterface black) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(black, "Black data");
    final double priceFutures = METHOD_FUTURE.price(security.getUnderlyingFuture(), black.getIssuerProvider());
    final double strike = security.getStrike();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, security.getExpirationTime(), security.isCall());
    final double delay = security.getUnderlyingFuture().getNoticeLastTime() - security.getExpirationTime();
    final double volatility = black.getVolatility(security.getExpirationTime(), delay, strike, priceFutures);
    final BlackFunctionData dataBlack = new BlackFunctionData(priceFutures, 1.0, volatility);
    final double[] firstDerivs = new double[3];
    final double[][] secondDerivs = new double[3][3];
    BLACK_FUNCTION.getPriceAdjoint2(option, dataBlack, firstDerivs, secondDerivs);
    return secondDerivs[0][0];
  }
  
  /**
   * The theoretical vega of the option with respect to the underlying futures price.
   * @param security The future option security, not null
   * @param black The curve and Black volatility data, not null
   * @return The vega.
   */
  public double vegaUnderlyingPrice(final BondFuturesOptionMarginSecurity security, final BlackBondFuturesProviderInterface black) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(black, "Black data");
    final double priceFutures = METHOD_FUTURE.price(security.getUnderlyingFuture(), black.getIssuerProvider());
    final double strike = security.getStrike();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, security.getExpirationTime(), security.isCall());
    final double delay = security.getUnderlyingFuture().getNoticeLastTime() - security.getExpirationTime();
    final double volatility = black.getVolatility(security.getExpirationTime(), delay, strike, priceFutures);
    final BlackFunctionData dataBlack = new BlackFunctionData(priceFutures, 1.0, volatility);
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    return priceAdjoint[2];
  }

  /**
  * The theoretical theta of the option with respect to the underlying futures price.
  * @param security The future option security, not null
  * @param black The curve and Black volatility data, not null
  * @return The theta.
  */
  public double theta(final BondFuturesOptionMarginSecurity security, final BlackBondFuturesProviderInterface black) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(black, "Black data");
    final double priceFutures = METHOD_FUTURE.price(security.getUnderlyingFuture(), black.getIssuerProvider());
    final double strike = security.getStrike();
    final double delay = security.getUnderlyingFuture().getNoticeLastTime() - security.getExpirationTime();
    final double volatility = black.getVolatility(security.getExpirationTime(), delay, strike, priceFutures);
    final double rate = -Math.log(black.getMulticurveProvider().getDiscountFactor(security.getCurrency(), security.getExpirationTime())) / security.getExpirationTime();
    return BlackFormulaRepository.theta(priceFutures, strike, security.getExpirationTime(), volatility, security.isCall(), rate);
  }
}
