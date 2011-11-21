/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.method;

import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

/**
 * Method to compute the price for an interest rate future with discounting (like a forward). 
 * No convexity adjustment is done. 
 */
public final class InterestRateFutureDiscountingMethod extends InterestRateFutureMethod {

  private static final InterestRateFutureDiscountingMethod INSTANCE = new InterestRateFutureDiscountingMethod();

  public static InterestRateFutureDiscountingMethod getInstance() {
    return INSTANCE;
  }

  private InterestRateFutureDiscountingMethod() {
  }

  /**
   * Computes the price of a future from the curves using an estimation of the future rate without convexity adjustment.
   * @param future The future.
   * @param curves The yield curves. Should contain the forward curve associated. 
   * @return The price.
   */
  public double price(final InterestRateFuture future, final YieldCurveBundle curves) {
    Validate.notNull(future, "Future");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(future.getForwardCurveName());
    final double forward = (forwardCurve.getDiscountFactor(future.getFixingPeriodStartTime()) / forwardCurve.getDiscountFactor(future.getFixingPeriodEndTime()) - 1)
        / future.getFixingPeriodAccrualFactor();
    final double price = 1.0 - forward;
    return price;
  }

  public CurrencyAmount presentValue(final InterestRateFuture future, final YieldCurveBundle curves) {
    final double pv = presentValueFromPrice(future, price(future, curves));
    return CurrencyAmount.of(future.getCurrency(), pv);
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof InterestRateFuture, "Interest rate future");
    return presentValue((InterestRateFuture) instrument, curves);
  }

  /**
   * Computes the future rate (1-price) from the curves using an estimation of the future rate without convexity adjustment.
   * @param future The future.
   * @param curves The yield curves. Should contain the forward curve associated. 
   * @return The rate.
   */
  public double parRate(final InterestRateFuture future, final YieldCurveBundle curves) {
    Validate.notNull(future, "Future");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(future.getForwardCurveName());
    final double forward = (forwardCurve.getDiscountFactor(future.getFixingPeriodStartTime()) / forwardCurve.getDiscountFactor(future.getFixingPeriodEndTime()) - 1)
        / future.getFixingPeriodAccrualFactor();
    return forward;
  }

  /**
   * Compute the price sensitivity to rates of a interest rate future by discounting.
   * @param future The future.
   * @param curves The yield curves. Should contain the forward curve associated. 
   * @return The price rate sensitivity.
   */
  @Override
  public InterestRateCurveSensitivity priceCurveSensitivity(final InterestRateFuture future, final YieldCurveBundle curves) {
    Validate.notNull(future, "Future");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(future.getForwardCurveName());
    double dfForwardStart = forwardCurve.getDiscountFactor(future.getFixingPeriodStartTime());
    double dfForwardEnd = forwardCurve.getDiscountFactor(future.getFixingPeriodEndTime());
    // Backward sweep
    double priceBar = 1.0;
    double forwardBar = -priceBar;
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
}
