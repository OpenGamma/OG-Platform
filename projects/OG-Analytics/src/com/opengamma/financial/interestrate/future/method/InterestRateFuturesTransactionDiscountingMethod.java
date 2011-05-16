/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.future.InterestRateFutureTransaction;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to compute the present value and its sensitivities for an interest rate future with discounting (like a forward). 
 * No convexity adjustment is done. 
 */
public class InterestRateFuturesTransactionDiscountingMethod {

  /**
   * Compute the present value of a future transaction from a quoted price.
   * @param future The future.
   * @param price The quoted price.
   * @return The present value.
   */
  public double presentValueFromPrice(final InterestRateFutureTransaction future, final double price) {
    double pv = (price - future.getReferencePrice()) * future.getUnderlyingFuture().getPaymentAccrualFactor() * future.getUnderlyingFuture().getNotional() * future.getQuantity();
    return pv;
  }

  /**
   * Computes the present value of future from the curves using an estimation of the future rate without convexity adjustment.
   * @param future The future.
   * @param curves The yield curves. Should contain the forward curve associated. 
   * @return The present value.
   */
  public double presentValueFromCurve(final InterestRateFutureTransaction future, final YieldCurveBundle curves) {
    Validate.notNull(future, "Future");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(future.getUnderlyingFuture().getForwardCurveName());
    double forward = (forwardCurve.getDiscountFactor(future.getUnderlyingFuture().getFixingPeriodStartTime()) 
        / forwardCurve.getDiscountFactor(future.getUnderlyingFuture().getFixingPeriodEndTime()) - 1)
        / future.getUnderlyingFuture().getFixingPeriodAccrualFactor();
    double pv = (1 - forward - future.getReferencePrice()) * future.getUnderlyingFuture().getPaymentAccrualFactor() * future.getUnderlyingFuture().getNotional() * future.getQuantity();
    return pv;
  }

  /**
   * Compute the present value sensitivity to rates of a interest rate future by discounting.
   * @param future The future.
   * @param curves The yield curves. Should contain the forward curve associated. 
   * @return The present value rate sensitivity.
   */
  public PresentValueSensitivity presentValueCurveSensitivity(final InterestRateFutureTransaction future, final YieldCurveBundle curves) {
    Validate.notNull(future, "Future");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(future.getUnderlyingFuture().getForwardCurveName());
    double dfForwardStart = forwardCurve.getDiscountFactor(future.getUnderlyingFuture().getFixingPeriodStartTime());
    double dfForwardEnd = forwardCurve.getDiscountFactor(future.getUnderlyingFuture().getFixingPeriodEndTime());
    // Backward sweep
    double pvBar = 1.0;
    double forwardBar = -future.getUnderlyingFuture().getPaymentAccrualFactor() * future.getUnderlyingFuture().getNotional() * future.getQuantity() * pvBar;
    double dfForwardEndBar = -dfForwardStart / (dfForwardEnd * dfForwardEnd) / future.getUnderlyingFuture().getFixingPeriodAccrualFactor() * forwardBar;
    double dfForwardStartBar = 1.0 / (future.getUnderlyingFuture().getFixingPeriodAccrualFactor() * dfForwardEnd) * forwardBar;
    Map<String, List<DoublesPair>> resultMap = new HashMap<String, List<DoublesPair>>();
    List<DoublesPair> listForward = new ArrayList<DoublesPair>();
    listForward.add(new DoublesPair(future.getUnderlyingFuture().getFixingPeriodStartTime(), -future.getUnderlyingFuture().getFixingPeriodStartTime() * dfForwardStart * dfForwardStartBar));
    listForward.add(new DoublesPair(future.getUnderlyingFuture().getFixingPeriodEndTime(), -future.getUnderlyingFuture().getFixingPeriodEndTime() * dfForwardEnd * dfForwardEndBar));
    resultMap.put(future.getUnderlyingFuture().getForwardCurveName(), listForward);
    PresentValueSensitivity result = new PresentValueSensitivity(resultMap);
    return result;
  }

}
