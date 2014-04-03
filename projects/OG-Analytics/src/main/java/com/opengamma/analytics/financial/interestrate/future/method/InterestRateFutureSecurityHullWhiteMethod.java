/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to compute the price for an interest rate future with convexity adjustment from a Hull-White one factor model.
 * <p> Reference: Henrard M., Eurodollar Futures and Options: Convexity Adjustment in HJM One-Factor Model. March 2005.
 * Available at <a href="http://ssrn.com/abstract=682343">http://ssrn.com/abstract=682343</a>
 * @deprecated Use {@link com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureSecurityHullWhiteMethod}
 */
@Deprecated
public final class InterestRateFutureSecurityHullWhiteMethod extends InterestRateFutureSecurityMethod {

  /**
   * The unique instance of the calculator.
   */
  private static final InterestRateFutureSecurityHullWhiteMethod INSTANCE = new InterestRateFutureSecurityHullWhiteMethod();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static InterestRateFutureSecurityHullWhiteMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private InterestRateFutureSecurityHullWhiteMethod() {
  }

  /**
   * The Hull-White model.
   */
  private static final HullWhiteOneFactorPiecewiseConstantInterestRateModel MODEL = new HullWhiteOneFactorPiecewiseConstantInterestRateModel();

  /**
   * Computes the price of a future from the curves using an estimation of the future rate without convexity adjustment.
   * @param future The future.
   * @param curves The Hull-White parameters and the curves.
   * @return The price.
   */
  public double price(final InterestRateFutureSecurity future, final HullWhiteOneFactorPiecewiseConstantDataBundle curves) {
    Validate.notNull(future, "Future");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(future.getForwardCurveName());
    final double dfForwardStart = forwardCurve.getDiscountFactor(future.getFixingPeriodStartTime());
    final double dfForwardEnd = forwardCurve.getDiscountFactor(future.getFixingPeriodEndTime());
    final double forward = (dfForwardStart / dfForwardEnd - 1) / future.getFixingPeriodAccrualFactor();
    final double futureConvexityFactor = MODEL.futuresConvexityFactor(curves.getHullWhiteParameter(), future.getTradingLastTime(), future.getFixingPeriodStartTime(), future.getFixingPeriodEndTime());
    final double price = 1.0 - futureConvexityFactor * forward + (1 - futureConvexityFactor) / future.getFixingPeriodAccrualFactor();
    return price;
  }

  /**
   * Compute the price sensitivity to rates of a interest rate future by discounting.
   * @param future The future.
   * @param curves The Hull-White parameters and the curves.
   * @return The price rate sensitivity.
   */
  public InterestRateCurveSensitivity priceCurveSensitivity(final InterestRateFutureSecurity future, final HullWhiteOneFactorPiecewiseConstantDataBundle curves) {
    ArgumentChecker.notNull(future, "Future");
    ArgumentChecker.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(future.getForwardCurveName());
    final double dfForwardStart = forwardCurve.getDiscountFactor(future.getFixingPeriodStartTime());
    final double dfForwardEnd = forwardCurve.getDiscountFactor(future.getFixingPeriodEndTime());
    final double futureConvexityFactor = MODEL.futuresConvexityFactor(curves.getHullWhiteParameter(), future.getTradingLastTime(), future.getFixingPeriodStartTime(), future.getFixingPeriodEndTime());
    // Backward sweep
    final double priceBar = 1.0;
    final double forwardBar = -futureConvexityFactor * priceBar;
    final double dfForwardEndBar = -dfForwardStart / (dfForwardEnd * dfForwardEnd) / future.getFixingPeriodAccrualFactor() * forwardBar;
    final double dfForwardStartBar = 1.0 / (future.getFixingPeriodAccrualFactor() * dfForwardEnd) * forwardBar;
    final Map<String, List<DoublesPair>> resultMap = new HashMap<>();
    final List<DoublesPair> listForward = new ArrayList<>();
    listForward.add(DoublesPair.of(future.getFixingPeriodStartTime(), -future.getFixingPeriodStartTime() * dfForwardStart * dfForwardStartBar));
    listForward.add(DoublesPair.of(future.getFixingPeriodEndTime(), -future.getFixingPeriodEndTime() * dfForwardEnd * dfForwardEndBar));
    resultMap.put(future.getForwardCurveName(), listForward);
    final InterestRateCurveSensitivity result = new InterestRateCurveSensitivity(resultMap);
    return result;
  }

  @Override
  public InterestRateCurveSensitivity priceCurveSensitivity(final InterestRateFutureSecurity future, final YieldCurveBundle curves) {
    ArgumentChecker.isTrue(curves instanceof HullWhiteOneFactorPiecewiseConstantDataBundle, "Bundle with Hull-White data");
    return priceCurveSensitivity(future, (HullWhiteOneFactorPiecewiseConstantDataBundle) curves);
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    return null;
  }

  @Override
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final InterestRateFutureSecurity future, final YieldCurveBundle curves) {
    return null;
  }

}
