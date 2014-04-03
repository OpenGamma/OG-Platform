/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSTIRFuturesProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method for the pricing of interest rate future options with margin process. The pricing is done with a SABR approach on the future rate (1.0-price).
 * The SABR parameters are represented by (expiration-delay) surfaces. The "delay" is the time between option expiration and future last trading date,
 * i.e. 0 for quarterly options and x for x-year mid-curve options. The future prices are computed without convexity adjustments.
 */
public final class InterestRateFutureOptionMarginSecuritySABRMethod extends InterestRateFutureOptionMarginSecurityGenericMethod<SABRSTIRFuturesProviderInterface> {

  /**
   * Creates the method unique instance.
   */
  private static final InterestRateFutureOptionMarginSecuritySABRMethod INSTANCE = new InterestRateFutureOptionMarginSecuritySABRMethod();

  /**
   * Constructor.
   */
  private InterestRateFutureOptionMarginSecuritySABRMethod() {
  }

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static InterestRateFutureOptionMarginSecuritySABRMethod getInstance() {
    return INSTANCE;
  }

  /**
   * The Black function used in the pricing.
   */
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  /**
   * The method used to compute the future price. It is a method without convexity adjustment.
   */
  private static final InterestRateFutureSecurityDiscountingMethod METHOD_FUTURE = InterestRateFutureSecurityDiscountingMethod.getInstance();

  /**
   * Computes the option security price from future price.
   * @param security The future option security.
   * @param sabrData The SABR and multi-curves provider.
   * @param priceFuture The price of the underlying future.
   * @return The security price.
   */
  public double priceFromFuturePrice(final InterestRateFutureOptionMarginSecurity security, final SABRSTIRFuturesProviderInterface sabrData, final double priceFuture) {
    ArgumentChecker.notNull(security, "Option security");
    ArgumentChecker.notNull(sabrData, "SABR data");
    final double rateStrike = 1.0 - security.getStrike();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(rateStrike, security.getExpirationTime(), !security.isCall());
    final double forward = 1 - priceFuture;
    final double delay = security.getUnderlyingFuture().getTradingLastTime() - security.getExpirationTime();
    final double volatility = sabrData.getSABRParameters().getVolatility(new double[] {security.getExpirationTime(), delay, rateStrike, forward });
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatility);
    final double priceSecurity = BLACK_FUNCTION.getPriceFunction(option).evaluate(dataBlack);
    return priceSecurity;
  }

  /**
   * Computes the option security price. The future price is computed without convexity adjustment.
   * @param security The future option security.
   * @param sabrData The SABR data bundle.
   * @return The security price.
   */
  @Override
  public double price(final InterestRateFutureOptionMarginSecurity security, final SABRSTIRFuturesProviderInterface sabrData) {
    final double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), sabrData.getMulticurveProvider());
    return priceFromFuturePrice(security, sabrData, priceFuture);
  }

  /**
   * Computes the option security price curve sensitivity. The future price is computed without convexity adjustment.
   * @param security The future option security.
   * @param sabrData The SABR data bundle.
   * @return The security price curve sensitivity.
   */
  @Override
  public MulticurveSensitivity priceCurveSensitivity(final InterestRateFutureOptionMarginSecurity security, final SABRSTIRFuturesProviderInterface sabrData) {
    // Forward sweep
    final double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), sabrData.getMulticurveProvider());
    final double rateStrike = 1.0 - security.getStrike();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(rateStrike, security.getExpirationTime(), !security.isCall());
    final double forward = 1 - priceFuture;
    final double delay = security.getUnderlyingFuture().getTradingLastTime() - security.getExpirationTime();
    final double[] volatilityAdjoint = sabrData.getSABRParameters().getVolatilityAdjoint(security.getExpirationTime(), delay, rateStrike, forward);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatilityAdjoint[0]);
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    // Backward sweep
    final double priceBar = 1.0;
    final double volatilityBar = priceAdjoint[2] * priceBar;
    final double forwardBar = priceAdjoint[1] * priceBar + volatilityAdjoint[1] * volatilityBar;
    final double priceFutureBar = -forwardBar;
    final MulticurveSensitivity priceFutureDerivative = METHOD_FUTURE.priceCurveSensitivity(security.getUnderlyingFuture(), sabrData.getMulticurveProvider());
    return priceFutureDerivative.multipliedBy(priceFutureBar);
  }

  /**
   * Computes the option security price curve sensitivity. The future price is computed without convexity adjustment.
   * @param security The future option security.
   * @param sabrData The SABR data bundle.
   * @return The security price curve sensitivity.
   */
  public PresentValueSABRSensitivityDataBundle priceSABRSensitivity(final InterestRateFutureOptionMarginSecurity security, final SABRSTIRFuturesProviderInterface sabrData) {
    final PresentValueSABRSensitivityDataBundle sensi = new PresentValueSABRSensitivityDataBundle();
    // Forward sweep
    final double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), sabrData.getMulticurveProvider());
    final double rateStrike = 1.0 - security.getStrike();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(rateStrike, security.getExpirationTime(), !security.isCall());
    final double forward = 1 - priceFuture;
    final double delay = security.getUnderlyingFuture().getTradingLastTime() - security.getExpirationTime();
    final double[] volatilityAdjoint = sabrData.getSABRParameters().getVolatilityAdjoint(security.getExpirationTime(), delay, rateStrike, forward);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatilityAdjoint[0]);
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    // Backward sweep
    final double priceBar = 1.0;
    final double volatilityBar = priceAdjoint[2] * priceBar;
    final DoublesPair expiryDelay = DoublesPair.of(security.getExpirationTime(), delay);
    sensi.addAlpha(expiryDelay, volatilityAdjoint[3] * volatilityBar);
    sensi.addBeta(expiryDelay, volatilityAdjoint[4] * volatilityBar);
    sensi.addRho(expiryDelay, volatilityAdjoint[5] * volatilityBar);
    sensi.addNu(expiryDelay, volatilityAdjoint[6] * volatilityBar);
    return sensi;
  }

}
