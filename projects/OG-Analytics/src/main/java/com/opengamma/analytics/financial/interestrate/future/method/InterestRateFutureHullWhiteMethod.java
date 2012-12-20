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
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFuture;
import com.opengamma.analytics.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.lambdava.tuple.DoublesPair;

/**
 * Method to compute the price for an interest rate future with convexity adjustment from a Hull-White one factor model.
 * <p> Reference: Henrard M., Eurodollar Futures and Options: Convexity Adjustment in HJM One-Factor Model. March 2005. 
 * Available at <a href="http://ssrn.com/abstract=682343">http://ssrn.com/abstract=682343</a>
 */
public final class InterestRateFutureHullWhiteMethod extends InterestRateFutureMethod {

  /**
   * The unique instance of the calculator.
   */
  private static final InterestRateFutureHullWhiteMethod INSTANCE = new InterestRateFutureHullWhiteMethod();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static InterestRateFutureHullWhiteMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private InterestRateFutureHullWhiteMethod() {
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
  public double price(final InterestRateFuture future, final HullWhiteOneFactorPiecewiseConstantDataBundle curves) {
    Validate.notNull(future, "Future");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(future.getForwardCurveName());
    double dfForwardStart = forwardCurve.getDiscountFactor(future.getFixingPeriodStartTime());
    double dfForwardEnd = forwardCurve.getDiscountFactor(future.getFixingPeriodEndTime());
    double forward = (dfForwardStart / dfForwardEnd - 1) / future.getFixingPeriodAccrualFactor();
    double futureConvexityFactor = MODEL.futureConvexityFactor(curves.getHullWhiteParameter(), future.getLastTradingTime(), future.getFixingPeriodStartTime(), future.getFixingPeriodEndTime());
    double price = 1.0 - futureConvexityFactor * forward + (1 - futureConvexityFactor) / future.getFixingPeriodAccrualFactor();
    return price;
  }

  public CurrencyAmount presentValue(final InterestRateFuture future, final HullWhiteOneFactorPiecewiseConstantDataBundle curves) {
    final double pv = presentValueFromPrice(future, price(future, curves));
    return CurrencyAmount.of(future.getCurrency(), pv);
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    ArgumentChecker.isTrue(instrument instanceof InterestRateFuture, "Interest rate future");
    ArgumentChecker.isTrue(curves instanceof HullWhiteOneFactorPiecewiseConstantDataBundle, "Bundle with Hull-White data");
    return presentValue((InterestRateFuture) instrument, (HullWhiteOneFactorPiecewiseConstantDataBundle) curves);
  }

  /**
   * Compute the price sensitivity to rates of a interest rate future by discounting.
   * @param future The future.
   * @param curves The Hull-White parameters and the curves.
   * @return The price rate sensitivity.
   */
  public InterestRateCurveSensitivity priceCurveSensitivity(final InterestRateFuture future, final HullWhiteOneFactorPiecewiseConstantDataBundle curves) {
    ArgumentChecker.notNull(future, "Future");
    ArgumentChecker.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(future.getForwardCurveName());
    double dfForwardStart = forwardCurve.getDiscountFactor(future.getFixingPeriodStartTime());
    double dfForwardEnd = forwardCurve.getDiscountFactor(future.getFixingPeriodEndTime());
    double futureConvexityFactor = MODEL.futureConvexityFactor(curves.getHullWhiteParameter(), future.getLastTradingTime(), future.getFixingPeriodStartTime(), future.getFixingPeriodEndTime());
    // Backward sweep
    double priceBar = 1.0;
    double forwardBar = -futureConvexityFactor * priceBar;
    double dfForwardEndBar = -dfForwardStart / (dfForwardEnd * dfForwardEnd) / future.getFixingPeriodAccrualFactor() * forwardBar;
    double dfForwardStartBar = 1.0 / (future.getFixingPeriodAccrualFactor() * dfForwardEnd) * forwardBar;
    Map<String, List<DoublesPair>> resultMap = new HashMap<String, List<DoublesPair>>();
    List<DoublesPair> listForward = new ArrayList<DoublesPair>();
    listForward.add(new DoublesPair(future.getFixingPeriodStartTime(), -future.getFixingPeriodStartTime() * dfForwardStart * dfForwardStartBar));
    listForward.add(new DoublesPair(future.getFixingPeriodEndTime(), -future.getFixingPeriodEndTime() * dfForwardEnd * dfForwardEndBar));
    resultMap.put(future.getForwardCurveName(), listForward);
    InterestRateCurveSensitivity result = new InterestRateCurveSensitivity(resultMap);
    return result;
  }

  @Override
  public InterestRateCurveSensitivity priceCurveSensitivity(InterestRateFuture future, YieldCurveBundle curves) {
    ArgumentChecker.isTrue(curves instanceof HullWhiteOneFactorPiecewiseConstantDataBundle, "Bundle with Hull-White data");
    return priceCurveSensitivity(future, (HullWhiteOneFactorPiecewiseConstantDataBundle) curves);
  }

}
