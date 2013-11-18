/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesSmilePriceProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesSmilePriceProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesSmileProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method for the pricing of bond future options. The pricing is done with a Black approach on the bond future.
 * The Black parameters are represented by (expiration-strike) surfaces.  
 */
public final class BondFutureOptionPremiumSecurityBlackSmileMethod {

  /**
   * Creates the method unique instance.
   */
  private static final BondFutureOptionPremiumSecurityBlackSmileMethod INSTANCE = new BondFutureOptionPremiumSecurityBlackSmileMethod();

  /**
   * Constructor.
   */
  private BondFutureOptionPremiumSecurityBlackSmileMethod() {
  }

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static BondFutureOptionPremiumSecurityBlackSmileMethod getInstance() {
    return INSTANCE;
  }

  /**
   * The Black function used in the pricing.
   */
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  /**
   * The method used to compute the future price. 
   */
  private static final BondFutureDiscountingMethod METHOD_FUTURE = BondFutureDiscountingMethod.getInstance();

  /**
   * Computes the option security price from future price.
   * @param security The future option security, not null
   * @param blackPrice The curve, Black volatility data and future price, not null
   * @return The security price.
   */
  public double price(final BondFutureOptionPremiumSecurity security, final BlackBondFuturesSmilePriceProviderInterface blackPrice) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(blackPrice, "Black and price data");
    final double strike = security.getStrike();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, security.getExpirationTime(), security.isCall());
    final double volatility = blackPrice.getBlackProvider().getVolatility(security.getExpirationTime(), security.getStrike());
    final BlackFunctionData dataBlack = new BlackFunctionData(blackPrice.getFuturesPrice(), 1.0, volatility);
    final double priceSecurity = BLACK_FUNCTION.getPriceFunction(option).evaluate(dataBlack);
    return priceSecurity;
  }

  /**
   * Computes the option security price. The future price is computed from the curves. 
   * @param security The bond future option security, not null
   * @param black The curve and Black volatility data, not null
   * @return The security price.
   */
  public double price(final BondFutureOptionPremiumSecurity security, final BlackBondFuturesSmileProviderInterface black) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(black, "Black  data");
    final double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), black.getIssuerProvider());
    return price(security, new BlackBondFuturesSmilePriceProvider(black, priceFuture));
  }

  /**
   * Computes the option security price curve sensitivity. 
   * It is supposed that for a given strike the volatility does not change with the curves.
   * The option price and its derivative wrt the futures price is computed using the futures price. 
   * The derivatives of the futures price with respect to the curves are computed using the curves.
   * @param security The future option security, not null
   * @param blackPrice The curve, Black volatility data and future price, not null
   * @return The security price curve sensitivity.
   */
  public MulticurveSensitivity priceCurveSensitivity(final BondFutureOptionPremiumSecurity security, final BlackBondFuturesSmilePriceProviderInterface blackPrice) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(blackPrice, "Black data");
    // Forward sweep
    final double priceFuture = blackPrice.getFuturesPrice();
    final double strike = security.getStrike();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, security.getExpirationTime(), security.isCall());
    final double volatility = blackPrice.getBlackProvider().getVolatility(security.getExpirationTime(), security.getStrike());
    final BlackFunctionData dataBlack = new BlackFunctionData(priceFuture, 1.0, volatility);
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    // Backward sweep
    final double priceBar = 1.0;
    final double priceFutureBar = priceAdjoint[1] * priceBar;
    final MulticurveSensitivity priceFutureDerivative = METHOD_FUTURE.priceCurveSensitivity(security.getUnderlyingFuture(), blackPrice.getIssuerProvider());
    return priceFutureDerivative.multipliedBy(priceFutureBar);
  }

  /**
   * Computes the option security price curve sensitivity. 
   * It is supposed that for a given strike the volatility does not change with the curves.
   * The futures price and its derivatives with respect to the curves are computed using the curves.
   * @param security The future option security, not null
   * @param black The curve and Black volatility data, not null
   * @return The security price curve sensitivity.
   */
  public MulticurveSensitivity priceCurveSensitivity(final BondFutureOptionPremiumSecurity security, final BlackBondFuturesSmileProviderInterface black) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(black, "Black  data");
    final double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), black.getIssuerProvider());
    return priceCurveSensitivity(security, new BlackBondFuturesSmilePriceProvider(black, priceFuture));
  }

  /**
   * Computes the option security price volatility sensitivity. 
   * The option price and its derivative wrt the futures price is computed using the futures price. 
   * The derivatives of the futures price with respect to the curves are computed using the curves.
   * @param security The future option security, not null
   * @param blackPrice The curve, Black volatility data and future price, not null
   * @return The security price Black sensitivity.
   */
  public SurfaceValue priceBlackSensitivity(final BondFutureOptionPremiumSecurity security, final BlackBondFuturesSmilePriceProviderInterface blackPrice) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(blackPrice, "YieldCurveWithBlackCubeBundle was unexpectedly null");
    // Forward sweep
    final double strike = security.getStrike();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, security.getExpirationTime(), security.isCall());
    final double volatility = blackPrice.getBlackProvider().getVolatility(security.getExpirationTime(), security.getStrike());
    final BlackFunctionData dataBlack = new BlackFunctionData(blackPrice.getFuturesPrice(), 1.0, volatility);
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    // Backward sweep
    final double priceBar = 1.0;
    final double volatilityBar = priceAdjoint[2] * priceBar;
    final DoublesPair expiryStrikeDelay = DoublesPair.of(security.getExpirationTime(), strike);
    final SurfaceValue sensitivity = SurfaceValue.from(expiryStrikeDelay, volatilityBar);
    return sensitivity;
  }

  /**
   * Computes the option security price Black sensitivity. 
   * All the results are computed using the curves, including the futures price. The futures price is computed by discounting like a forward on the CTD.
   * @param security The future option security, not null
   * @param black The curve, Black volatility data.
   * @return The security price Black sensitivity.
   */
  public SurfaceValue priceBlackSensitivity(final BondFutureOptionPremiumSecurity security, final BlackBondFuturesSmileProviderInterface black) {
    ArgumentChecker.notNull(black, "black data");
    ArgumentChecker.notNull(security, "security");
    final double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), black.getIssuerProvider());
    return priceBlackSensitivity(security, new BlackBondFuturesSmilePriceProvider(black, priceFuture));
  }

  /**
   * Computes the option's value delta, the first derivative of the security price wrt underlying futures rate.
   * @param security The future option security, not null
   * @param blackPrice The curve, Black volatility data and future price, not null
   * @return The security value delta.
   */
  public double priceDelta(final BondFutureOptionPremiumSecurity security, final BlackBondFuturesSmilePriceProviderInterface blackPrice) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(blackPrice, "YieldCurveWithBlackCubeAndForwardBundle was unexpectedly null");
    // Forward sweep
    final double strike = security.getStrike();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, security.getExpirationTime(), security.isCall());
    final double volatility = blackPrice.getBlackProvider().getVolatility(security.getExpirationTime(), security.getStrike());
    final BlackFunctionData dataBlack = new BlackFunctionData(blackPrice.getFuturesPrice(), 1.0, volatility);
    final double[] firstDerivs = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    return firstDerivs[1];
  }

  /**
   * The delta (first order derivative) of the option price wrt the futures price.
   * @param security The future option security, not null
   * @param black The curve, Black volatility data.
   * @return The option price delta wrt the underlying futures price.
   */
  public double priceDelta(final BondFutureOptionPremiumSecurity security, final BlackBondFuturesSmileProviderInterface black) {
    ArgumentChecker.notNull(black, "black data");
    ArgumentChecker.notNull(security, "security");
    final double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), black.getIssuerProvider());
    return priceDelta(security, new BlackBondFuturesSmilePriceProvider(black, priceFuture));
  }

  /**
   * Computes the option's value gamma, the second derivative of the security price wrt underlying futures rate. 
   * @param security The future option security, not null
   * @param blackPrice The curve, Black volatility data and future price, not null
   * @return The security price gamma.
   */
  public double priceGamma(final BondFutureOptionPremiumSecurity security, final BlackBondFuturesSmilePriceProviderInterface blackPrice) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(blackPrice, "YieldCurveWithBlackCubeBundle was unexpectedly null");
    // Forward sweep
    final double strike = security.getStrike();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, security.getExpirationTime(), security.isCall());
    final double volatility = blackPrice.getBlackProvider().getVolatility(security.getExpirationTime(), security.getStrike());
    final BlackFunctionData dataBlack = new BlackFunctionData(blackPrice.getFuturesPrice(), 1.0, volatility);
    final double[] firstDerivs = new double[3];
    final double[][] secondDerivs = new double[3][3];
    BLACK_FUNCTION.getPriceAdjoint2(option, dataBlack, firstDerivs, secondDerivs);
    return secondDerivs[0][0];
  }

  /**
   * The gamma (second order derivative) of the option price wrt the futures price.
   * @param security The future option security, not null
   * @param black The curve, Black volatility data.
   * @return The option price gamma.
   */
  public double priceGamma(final BondFutureOptionPremiumSecurity security, final BlackBondFuturesSmileProviderInterface black) {
    ArgumentChecker.notNull(black, "black data");
    ArgumentChecker.notNull(security, "security");
    final double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), black.getIssuerProvider());
    return priceGamma(security, new BlackBondFuturesSmilePriceProvider(black, priceFuture));
  }

  /**
   * Computes the option's value vega, the first derivative of the security price wrt implied vol. 
   * @param security The future option security, not null
   * @param blackPrice The curve, Black volatility data and future price, not null
   * @return The security value delta.
   */
  public double priceVega(final BondFutureOptionPremiumSecurity security, final BlackBondFuturesSmilePriceProviderInterface blackPrice) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(blackPrice, "YieldCurveWithBlackCubeBundle was unexpectedly null");
    // Forward sweep
    final double strike = security.getStrike();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, security.getExpirationTime(), security.isCall());
    final double volatility = blackPrice.getBlackProvider().getVolatility(security.getExpirationTime(), security.getStrike());
    final BlackFunctionData dataBlack = new BlackFunctionData(blackPrice.getFuturesPrice(), 1.0, volatility);
    final double[] firstDerivs = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    return firstDerivs[2];
  }

  /**
   * The vega (first order derivative) of the option price wrt the volatility.
   * @param security The future option security, not null
   * @param black The curve, Black volatility data.
   * @return The option price gamma.
   */
  public double priceVega(final BondFutureOptionPremiumSecurity security, final BlackBondFuturesSmileProviderInterface black) {
    ArgumentChecker.notNull(black, "black data");
    ArgumentChecker.notNull(security, "security");
    final double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), black.getIssuerProvider());
    return priceVega(security, new BlackBondFuturesSmilePriceProvider(black, priceFuture));
  }

  /**
   * Interpolates and returns the option's implied volatility 
   * @param security The future option security, not null
   * @param blackPrice The curve, Black volatility data and future price, not null
   * @return Lognormal Implied Volatility.
   */
  public double impliedVolatility(final BondFutureOptionPremiumSecurity security, final BlackBondFuturesSmilePriceProviderInterface blackPrice) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(blackPrice, "Black data");
    return blackPrice.getBlackProvider().getVolatility(security.getExpirationTime(), security.getStrike());
  }

  /**
   * The (Black) implied volatility associated to the expiration and strike.
   * @param security The future option security, not null
   * @param black The curve, Black volatility data.
   * @return The option price vega.
   */
  public double impliedVolatility(final BondFutureOptionPremiumSecurity security, final BlackBondFuturesSmileProviderInterface black) {
    ArgumentChecker.notNull(black, "black data");
    ArgumentChecker.notNull(security, "security");
    final double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), black.getIssuerProvider());
    return impliedVolatility(security, new BlackBondFuturesSmilePriceProvider(black, priceFuture));
  }

  /**
   * Computes the underlying future security price. 
   * @param security The future option security, not null
   * @param issuerMulticurves Issuer and multi-curves provider.
   * @return The security price.
   */
  public double underlyingFuturePrice(final BondFutureOptionPremiumSecurity security, final IssuerProviderInterface issuerMulticurves) {
    ArgumentChecker.notNull(security, "security");
    return METHOD_FUTURE.price(security.getUnderlyingFuture(), issuerMulticurves);
  }

}
