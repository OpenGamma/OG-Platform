/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.method;

import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.provider.BondFutureOptionPremiumSecurityBlackSmileMethod;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeAndForwardBundle;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method for the pricing of bond future options. The pricing is done with a Black approach on the bond future.
 * The Black parameters are represented by (expiration-strike) surfaces.
 * @deprecated Use {@link BondFutureOptionPremiumSecurityBlackSmileMethod}
 */
@Deprecated
public final class BondFutureOptionPremiumSecurityBlackSurfaceMethod {

  /**
   * Creates the method unique instance.
   */
  private static final BondFutureOptionPremiumSecurityBlackSurfaceMethod INSTANCE = new BondFutureOptionPremiumSecurityBlackSurfaceMethod();

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static BondFutureOptionPremiumSecurityBlackSurfaceMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private BondFutureOptionPremiumSecurityBlackSurfaceMethod() {
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
   * @param blackData The curve, Black volatility data and future price, not null
   * @return The security price.
   */
  public double optionPrice(final BondFutureOptionPremiumSecurity security, final YieldCurveWithBlackCubeAndForwardBundle blackData) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(blackData, "Black data");
    final double strike = security.getStrike();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, security.getExpirationTime(), security.isCall());
    final double volatility = blackData.getVolatility(security.getExpirationTime(), security.getStrike());
    final BlackFunctionData dataBlack = new BlackFunctionData(blackData.getForward(), 1.0, volatility);
    final double priceSecurity = BLACK_FUNCTION.getPriceFunction(option).evaluate(dataBlack);
    return priceSecurity;
  }

  /**
   * Computes the option security price. The future price is computed from the curves.
   * @param security The bond future option security, not null
   * @param blackData The curve and Black volatility data, not null
   * @return The security price.
   */
  public double optionPrice(final BondFutureOptionPremiumSecurity security, final YieldCurveWithBlackCubeBundle blackData) {
    ArgumentChecker.notNull(security, "security");
    final double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), blackData);
    return optionPrice(security, YieldCurveWithBlackCubeAndForwardBundle.from(blackData, priceFuture));
  }

  /**
   * Computes the option security price. If the future price is present in the bundle ()
   * @param security The bond future option security, not null
   * @param curves The curve, Black volatility data and potentially future price.
   * @return The security price.
   */
  public double optionPrice(final BondFutureOptionPremiumSecurity security, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "Curves");
    if (curves instanceof YieldCurveWithBlackCubeBundle) {
      return optionPrice(security, (YieldCurveWithBlackCubeBundle) curves);
    } else if (curves instanceof YieldCurveWithBlackCubeAndForwardBundle) {
      return optionPrice(security, (YieldCurveWithBlackCubeAndForwardBundle) curves);
    }
    throw new UnsupportedOperationException(
        "The BondFutureOptionPremiumSecurityBlackSurfaceMethod method requires a YieldCurveWithBlackCubeBundle or YieldCurveWithBlackCubeAndForwardBundle as data.");
  }

  /**
   * Computes the option security price curve sensitivity.
   * It is supposed that for a given strike the volatility does not change with the curves.
   * @param security The future option security, not null
   * @param blackData The curve and Black volatility data, not null
   * @return The security price curve sensitivity.
   */
  public InterestRateCurveSensitivity priceCurveSensitivity(final BondFutureOptionPremiumSecurity security, final YieldCurveWithBlackCubeAndForwardBundle blackData) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(blackData, "Black data");
    // Forward sweep
    final double priceFuture = blackData.getForward();
    final double strike = security.getStrike();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, security.getExpirationTime(), security.isCall());
    final double volatility = blackData.getVolatility(security.getExpirationTime(), security.getStrike());
    final BlackFunctionData dataBlack = new BlackFunctionData(priceFuture, 1.0, volatility);
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    // Backward sweep
    final double priceBar = 1.0;
    final double priceFutureBar = priceAdjoint[1] * priceBar;
    final InterestRateCurveSensitivity priceFutureDerivative = METHOD_FUTURE.priceCurveSensitivity(security.getUnderlyingFuture(), blackData);
    return priceFutureDerivative.multipliedBy(priceFutureBar);
  }

  /**
   * Computes the option security price curve sensitivity.
   * It is supposed that for a given strike the volatility does not change with the curves.
   * @param security The future option security, not null
   * @param blackData The curve and Black volatility data, not null
   * @return The security price curve sensitivity.
   */
  public InterestRateCurveSensitivity priceCurveSensitivity(final BondFutureOptionPremiumSecurity security, final YieldCurveWithBlackCubeBundle blackData) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(blackData, "Black data");
    final double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), blackData);
    return priceCurveSensitivity(security, YieldCurveWithBlackCubeAndForwardBundle.from(blackData, priceFuture));
  }

  public InterestRateCurveSensitivity priceCurveSensitivity(final BondFutureOptionPremiumSecurity security, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "Curves");
    if (curves instanceof YieldCurveWithBlackCubeBundle) {
      return priceCurveSensitivity(security, (YieldCurveWithBlackCubeBundle) curves);
    } else if (curves instanceof YieldCurveWithBlackCubeAndForwardBundle) {
      return priceCurveSensitivity(security, (YieldCurveWithBlackCubeAndForwardBundle) curves);
    }
    throw new UnsupportedOperationException(
        "The BondFutureOptionPremiumSecurityBlackSurfaceMethod method requires a YieldCurveWithBlackCubeBundle or YieldCurveWithBlackCubeAndForwardBundle as data.");
  }

  /**
   * Computes the option security price volatility sensitivity.
   * @param security The future option security, not null
   * @param blackData The curve and Black volatility data, not null
   * @return The security price Black volatility sensitivity.
   */
  public SurfaceValue priceBlackSensitivity(final BondFutureOptionPremiumSecurity security, final YieldCurveWithBlackCubeAndForwardBundle blackData) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(blackData, "YieldCurveWithBlackCubeBundle was unexpectedly null");
    // Forward sweep
    final double strike = security.getStrike();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, security.getExpirationTime(), security.isCall());
    final double volatility = blackData.getVolatility(security.getExpirationTime(), security.getStrike());
    final BlackFunctionData dataBlack = new BlackFunctionData(blackData.getForward(), 1.0, volatility);
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    // Backward sweep
    final double priceBar = 1.0;
    final double volatilityBar = priceAdjoint[2] * priceBar;
    final DoublesPair expiryStrikeDelay = DoublesPair.of(security.getExpirationTime(), strike);
    final SurfaceValue sensitivity = SurfaceValue.from(expiryStrikeDelay, volatilityBar);
    return sensitivity;
  }

  public SurfaceValue priceBlackSensitivity(final BondFutureOptionPremiumSecurity security, final YieldCurveBundle curves) {
    if (curves instanceof YieldCurveWithBlackCubeBundle) {
      return priceBlackSensitivity(security, (YieldCurveWithBlackCubeBundle) curves);
    } else if (curves instanceof YieldCurveWithBlackCubeAndForwardBundle) {
      return priceBlackSensitivity(security, (YieldCurveWithBlackCubeAndForwardBundle) curves);
    }
    throw new IllegalArgumentException("Yield curve bundle should contain Black cube");
  }

  public SurfaceValue priceBlackSensitivity(final BondFutureOptionPremiumSecurity security, final YieldCurveWithBlackCubeBundle blackData) {
    ArgumentChecker.notNull(blackData, "black data");
    ArgumentChecker.notNull(security, "security");
    final double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), blackData);
    return priceBlackSensitivity(security, YieldCurveWithBlackCubeAndForwardBundle.from(blackData, priceFuture));
  }

  /**
   * Computes the option's value delta, the first derivative of the security price wrt underlying futures rate. TODO: REVIEW is it futures rate or price?
   * @param security The future option security, not null
   * @param blackData The curve and Black volatility data, not null
   * @return The security value delta.
   */
  public double optionPriceDelta(final BondFutureOptionPremiumSecurity security, final YieldCurveWithBlackCubeAndForwardBundle blackData) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(blackData, "YieldCurveWithBlackCubeAndForwardBundle was unexpectedly null");
    // Forward sweep
    final double strike = security.getStrike();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, security.getExpirationTime(), security.isCall());

    final double volatility = blackData.getVolatility(security.getExpirationTime(), security.getStrike());
    final BlackFunctionData dataBlack = new BlackFunctionData(blackData.getForward(), 1.0, volatility);

    final double[] firstDerivs = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    return firstDerivs[1];
  }

  /**
   * Computes the option's value delta, the first derivative of the security price wrt underlying futures rate.
   * @param security The future option security, not null
   * @param curves The curve and Black volatility data, not null
   * @return The security value delta.
   */
  public double optionPriceDelta(final BondFutureOptionPremiumSecurity security, final YieldCurveBundle curves) {
    if (curves instanceof YieldCurveWithBlackCubeBundle) {
      return optionPriceDelta(security, (YieldCurveWithBlackCubeBundle) curves);
    } else if (curves instanceof YieldCurveWithBlackCubeAndForwardBundle) {
      return optionPriceDelta(security, (YieldCurveWithBlackCubeAndForwardBundle) curves);
    }
    throw new IllegalArgumentException("Yield curve bundle should contain Black cube");
  }

  public double optionPriceDelta(final BondFutureOptionPremiumSecurity security, final YieldCurveWithBlackCubeBundle blackData) {
    ArgumentChecker.notNull(blackData, "black data");
    ArgumentChecker.notNull(security, "security");
    final double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), blackData);
    return optionPriceDelta(security, YieldCurveWithBlackCubeAndForwardBundle.from(blackData, priceFuture));
  }

  /**
   * Computes the option's value gamma, the second derivative of the security price wrt underlying futures rate.
   * @param security The future option security, not null
   * @param blackData The curve and Black volatility data, not null
   * @return The security price gamma.
   */
  public double optionPriceGamma(final BondFutureOptionPremiumSecurity security, final YieldCurveWithBlackCubeAndForwardBundle blackData) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(blackData, "YieldCurveWithBlackCubeBundle was unexpectedly null");
    // Forward sweep
    final double strike = security.getStrike();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, security.getExpirationTime(), security.isCall());

    final double volatility = blackData.getVolatility(security.getExpirationTime(), security.getStrike());
    final BlackFunctionData dataBlack = new BlackFunctionData(blackData.getForward(), 1.0, volatility);

    // TODO This is overkill. We only need one value, but it provides extra calculations while doing testing
    final double[] firstDerivs = new double[3];
    final double[][] secondDerivs = new double[3][3];
    BLACK_FUNCTION.getPriceAdjoint2(option, dataBlack, firstDerivs, secondDerivs);
    return secondDerivs[0][0];
  }

  /**
   * Computes the option's value gamma, the second derivative of the security price wrt underlying futures rate.
   * @param security The future option security, not null
   * @param curves The curve and Black volatility data, not null
   * @return The security price gamma.
   */
  public double optionPriceGamma(final BondFutureOptionPremiumSecurity security, final YieldCurveBundle curves) {
    if (curves instanceof YieldCurveWithBlackCubeBundle) {
      return optionPriceGamma(security, (YieldCurveWithBlackCubeBundle) curves);
    } else if (curves instanceof YieldCurveWithBlackCubeAndForwardBundle) {
      return optionPriceGamma(security, (YieldCurveWithBlackCubeAndForwardBundle) curves);
    }
    throw new IllegalArgumentException("Yield curve bundle should contain Black cube");
  }

  public double optionPriceGamma(final BondFutureOptionPremiumSecurity security, final YieldCurveWithBlackCubeBundle blackData) {
    ArgumentChecker.notNull(blackData, "black data");
    ArgumentChecker.notNull(security, "security");
    final double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), blackData);
    return optionPriceGamma(security, YieldCurveWithBlackCubeAndForwardBundle.from(blackData, priceFuture));
  }

  /**
   * Computes the option's value vega, the first derivative of the security price wrt implied vol.
   * @param security The future option security, not null
   * @param blackData The curve and Black volatility data, not null
   * @return The security value delta.
   */
  public double optionPriceVega(final BondFutureOptionPremiumSecurity security, final YieldCurveWithBlackCubeAndForwardBundle blackData) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(blackData, "YieldCurveWithBlackCubeBundle was unexpectedly null");
    // Forward sweep
    final double strike = security.getStrike();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, security.getExpirationTime(), security.isCall());

    final double volatility = blackData.getVolatility(security.getExpirationTime(), security.getStrike());
    final BlackFunctionData dataBlack = new BlackFunctionData(blackData.getForward(), 1.0, volatility);

    final double[] firstDerivs = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    return firstDerivs[2];
  }

  /**
   * Computes the option's value delta, the first derivative of the security price wrt underlying futures rate.
   * @param security The future option security, not null
   * @param curves The curve and Black volatility data, not null
   * @return The security value delta.
   */
  public double optionPriceVega(final BondFutureOptionPremiumSecurity security, final YieldCurveBundle curves) {
    if (curves instanceof YieldCurveWithBlackCubeBundle) {
      return optionPriceVega(security, (YieldCurveWithBlackCubeBundle) curves);
    } else if (curves instanceof YieldCurveWithBlackCubeAndForwardBundle) {
      return optionPriceVega(security, (YieldCurveWithBlackCubeAndForwardBundle) curves);
    }
    throw new IllegalArgumentException("Yield curve bundle should contain Black cube");
  }

  public double optionPriceVega(final BondFutureOptionPremiumSecurity security, final YieldCurveWithBlackCubeBundle blackData) {
    ArgumentChecker.notNull(blackData, "black data");
    ArgumentChecker.notNull(security, "security");
    final double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), blackData);
    return optionPriceVega(security, YieldCurveWithBlackCubeAndForwardBundle.from(blackData, priceFuture));
  }

  /**
   * Interpolates and returns the option's implied volatility
   * @param security The future option security, not null
   * @param curves The curve and Black volatility data, not null
   * @return Lognormal Implied Volatility
   */
  public double impliedVolatility(final BondFutureOptionPremiumSecurity security, final YieldCurveBundle curves) {
    if (curves instanceof YieldCurveWithBlackCubeBundle) {
      return optionPriceVega(security, (YieldCurveWithBlackCubeBundle) curves);
    } else if (curves instanceof YieldCurveWithBlackCubeAndForwardBundle) {
      return optionPriceVega(security, (YieldCurveWithBlackCubeAndForwardBundle) curves);
    }
    throw new IllegalArgumentException("Yield curve bundle should contain Black cube");
  }

  /**
   * Interpolates and returns the option's implied volatility
   * @param security The future option security, not null
   * @param blackData The curve and Black volatility data, not null
   * @return Lognormal Implied Volatility.
   */
  public double impliedVolatility(final BondFutureOptionPremiumSecurity security, final YieldCurveWithBlackCubeAndForwardBundle blackData) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(blackData, "Black data");
    return blackData.getVolatility(security.getExpirationTime(), security.getStrike());
  }

  public double impliedVolatility(final BondFutureOptionPremiumSecurity security, final YieldCurveWithBlackCubeBundle blackData) {
    ArgumentChecker.notNull(blackData, "black data");
    ArgumentChecker.notNull(security, "security");
    final double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), blackData);
    return impliedVolatility(security, YieldCurveWithBlackCubeAndForwardBundle.from(blackData, priceFuture));
  }

  public double underlyingFuturePrice(final BondFutureOptionPremiumSecurity security, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackCubeBundle, "Yield curve bundle should contain Black cube");
    return underlyingFuturePrice(security, (YieldCurveWithBlackCubeBundle) curves);
  }

  /**
   * Computes the underlying future security price.
   * @param security The future option security, not null
   * @param blackData The curve and Black volatility data, not null
   * @return The security price.
   */
  public double underlyingFuturePrice(final BondFutureOptionPremiumSecurity security, final YieldCurveWithBlackCubeBundle blackData) {
    ArgumentChecker.notNull(security, "security");
    return METHOD_FUTURE.price(security.getUnderlyingFuture(), blackData);
  }

}
