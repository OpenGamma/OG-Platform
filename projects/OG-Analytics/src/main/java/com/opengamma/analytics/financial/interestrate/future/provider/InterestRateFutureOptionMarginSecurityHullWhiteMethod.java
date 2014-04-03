/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimplyCompoundedForwardSensitivity;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.ArgumentChecker;

/**
 * Method for the pricing of interest rate future options with daily margining.
 * The model is the Hull-White one factor model {@link HullWhiteOneFactorPiecewiseConstantInterestRateModel}.
 * <p>Reference: Interest Rate Futures and their options: Some Pricing Approaches. OpenGamma Documentation n.6. Version 1.5 - December 2012.
 */
public final class InterestRateFutureOptionMarginSecurityHullWhiteMethod extends InterestRateFutureOptionMarginSecurityGenericMethod<HullWhiteOneFactorProviderInterface> {

  /**
   * The unique instance of the calculator.
   */
  private static final InterestRateFutureOptionMarginSecurityHullWhiteMethod INSTANCE = new InterestRateFutureOptionMarginSecurityHullWhiteMethod();

  /**
   * Constructor.
   */
  private InterestRateFutureOptionMarginSecurityHullWhiteMethod() {
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static InterestRateFutureOptionMarginSecurityHullWhiteMethod getInstance() {
    return INSTANCE;
  }

  /**
   * The Hull-White model.
   */
  private static final HullWhiteOneFactorPiecewiseConstantInterestRateModel MODEL = new HullWhiteOneFactorPiecewiseConstantInterestRateModel();
  /**
   * The normal distribution implementation.
   */
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  /**
   * Computes the price in the Hull-White one factor model.
   * @param security The option security.
   * @param hwMulticurves The multi-curves provider with Hull-White one factor parameters.
   * @return The price.
   */
  @Override
  public double price(final InterestRateFutureOptionMarginSecurity security, final HullWhiteOneFactorProviderInterface hwMulticurves) {
    ArgumentChecker.notNull(security, "Option security");
    ArgumentChecker.notNull(hwMulticurves, "Hull-White and multi-curves data");
    ArgumentChecker.isTrue(security.getCurrency().equals(hwMulticurves.getHullWhiteCurrency()), "Model currency incompatible with security currency");
    final MulticurveProviderInterface multicurves = hwMulticurves.getMulticurveProvider();
    final HullWhiteOneFactorPiecewiseConstantParameters parameters = hwMulticurves.getHullWhiteParameters();
    final double k = security.getStrike();
    final double ktilde = 1.0 - k;
    final double theta = security.getExpirationTime();
    final double delta = security.getUnderlyingFuture().getFixingPeriodAccrualFactor();
    final double t0 = security.getUnderlyingFuture().getTradingLastTime();
    final double t1 = security.getUnderlyingFuture().getFixingPeriodStartTime();
    final double t2 = security.getUnderlyingFuture().getFixingPeriodEndTime();
    final double alpha = MODEL.alpha(parameters, 0.0, theta, t1, t2);
    final double gamma = MODEL.futuresConvexityFactor(parameters, t0, t1, t2);
    final double forward = multicurves.getSimplyCompoundForwardRate(security.getUnderlyingFuture().getIborIndex(), t1, t2, delta);
    final double kappa = -Math.log((1 + delta * ktilde) / (1 + delta * forward) / gamma) / alpha - 0.5 * alpha;
    if (security.isCall()) {
      final double normalKappa = NORMAL.getCDF(-kappa);
      final double normalAlphaKappa = NORMAL.getCDF(-kappa - alpha);
      return (1 - k + 1.0 / delta) * normalKappa - (1.0 / delta + forward) * gamma * normalAlphaKappa;
    }
    final double normalKappa = NORMAL.getCDF(kappa);
    final double normalAlphaKappa = NORMAL.getCDF(kappa + alpha);
    return (1.0 / delta + forward) * gamma * normalAlphaKappa - (1 - k + 1.0 / delta) * normalKappa;
  }

  /**
   * Computes the price curve sensitivity in the Hull-White one factor model.
   * @param security The option security.
   * @param hwMulticurves The multi-curves provider with Hull-White one factor parameters.
   * @return The curve sensitivity.
   */
  @Override
  public MulticurveSensitivity priceCurveSensitivity(final InterestRateFutureOptionMarginSecurity security, final HullWhiteOneFactorProviderInterface hwMulticurves) {
    ArgumentChecker.notNull(security, "Option security");
    ArgumentChecker.notNull(hwMulticurves, "Hull-White and multi-curves data");
    ArgumentChecker.isTrue(security.getCurrency().equals(hwMulticurves.getHullWhiteCurrency()), "Model currency incompatible with security currency");
    final MulticurveProviderInterface multicurves = hwMulticurves.getMulticurveProvider();
    final HullWhiteOneFactorPiecewiseConstantParameters parameters = hwMulticurves.getHullWhiteParameters();
    final double k = security.getStrike();
    final double ktilde = 1.0 - k;
    final double theta = security.getExpirationTime();
    final double delta = security.getUnderlyingFuture().getFixingPeriodAccrualFactor();
    final double t0 = security.getUnderlyingFuture().getTradingLastTime();
    final double t1 = security.getUnderlyingFuture().getFixingPeriodStartTime();
    final double t2 = security.getUnderlyingFuture().getFixingPeriodEndTime();
    // forward sweep
    final double alpha = MODEL.alpha(parameters, 0.0, theta, t1, t2);
    final double gamma = MODEL.futuresConvexityFactor(parameters, t0, t1, t2);
    final double forward = multicurves.getSimplyCompoundForwardRate(security.getUnderlyingFuture().getIborIndex(), t1, t2, delta);
    final double kappa = -Math.log((1 + delta * ktilde) / (1 + delta * forward) / gamma) / alpha - 0.5 * alpha;
    // Bakcward sweep
    final double priceBar = 1.0;
    double forwardBar;
    if (security.isCall()) {
      final double normalAlphaKappa = NORMAL.getCDF(-kappa - alpha);
      forwardBar = -gamma * normalAlphaKappa * priceBar;
    } else {
      final double normalAlphaKappa = NORMAL.getCDF(kappa + alpha);
      forwardBar = gamma * normalAlphaKappa * priceBar;
    }
    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<>();
    final List<ForwardSensitivity> listForward = new ArrayList<>();
    listForward.add(new SimplyCompoundedForwardSensitivity(t1, t2, delta, forwardBar));
    mapFwd.put(hwMulticurves.getMulticurveProvider().getName(security.getUnderlyingFuture().getIborIndex()), listForward);
    return MulticurveSensitivity.ofForward(mapFwd);
  }

}
