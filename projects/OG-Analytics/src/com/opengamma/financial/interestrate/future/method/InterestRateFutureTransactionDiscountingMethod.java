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

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.future.InterestRateFutureSecurity;
import com.opengamma.financial.interestrate.future.InterestRateFutureTransaction;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to compute the present value and its sensitivities for an interest rate future with discounting (like a forward). 
 * No convexity adjustment is done. 
 */
public final class InterestRateFutureTransactionDiscountingMethod extends InterestRateFutureTransactionMethod {
  private static final InterestRateFutureTransactionDiscountingMethod INSTANCE = new InterestRateFutureTransactionDiscountingMethod();

  public static InterestRateFutureTransactionDiscountingMethod getInstance() {
    return INSTANCE;
  }

  private InterestRateFutureTransactionDiscountingMethod() {
  }

  /**
   * Computes the present value of future from the curves using an estimation of the future rate without convexity adjustment.
   * @param future The future.
   * @param curves The yield curves. Should contain the discounting and forward curves associated to the instrument.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final InterestRateFutureTransaction future, final YieldCurveBundle curves) {
    Validate.notNull(future, "Future");
    Validate.notNull(curves, "Curves");
    InterestRateFutureSecurity underlyingFuture = future.getUnderlyingFuture();
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(underlyingFuture.getForwardCurveName());
    final double forward = (forwardCurve.getDiscountFactor(underlyingFuture.getFixingPeriodStartTime())
        / forwardCurve.getDiscountFactor(underlyingFuture.getFixingPeriodEndTime()) - 1)
        / underlyingFuture.getFixingPeriodAccrualFactor();
    final double futurePrice = 1 - forward;
    final double pv = presentValueFromPrice(future, futurePrice);
    return CurrencyAmount.of(underlyingFuture.getCurrency(), pv);
  }

  @Override
  public CurrencyAmount presentValue(final InterestRateDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof InterestRateFutureTransaction, "Interest rate future transaction");
    return presentValue((InterestRateFutureTransaction) instrument, curves);
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
    InterestRateFutureSecurity underlyingFuture = future.getUnderlyingFuture();
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(underlyingFuture.getForwardCurveName());
    final double dfForwardStart = forwardCurve.getDiscountFactor(underlyingFuture.getFixingPeriodStartTime());
    final double dfForwardEnd = forwardCurve.getDiscountFactor(underlyingFuture.getFixingPeriodEndTime());
    // Backward sweep
    final double pvBar = 1.0;
    final double forwardBar = -underlyingFuture.getPaymentAccrualFactor() * underlyingFuture.getNotional() * future.getQuantity() * pvBar;
    final double dfForwardEndBar = -dfForwardStart / (dfForwardEnd * dfForwardEnd) / underlyingFuture.getFixingPeriodAccrualFactor() * forwardBar;
    final double dfForwardStartBar = 1.0 / (underlyingFuture.getFixingPeriodAccrualFactor() * dfForwardEnd) * forwardBar;
    final Map<String, List<DoublesPair>> resultMap = new HashMap<String, List<DoublesPair>>();
    final List<DoublesPair> listForward = new ArrayList<DoublesPair>();
    listForward.add(new DoublesPair(underlyingFuture.getFixingPeriodStartTime(), -underlyingFuture.getFixingPeriodStartTime() * dfForwardStart * dfForwardStartBar));
    listForward.add(new DoublesPair(underlyingFuture.getFixingPeriodEndTime(), -underlyingFuture.getFixingPeriodEndTime() * dfForwardEnd * dfForwardEndBar));
    resultMap.put(underlyingFuture.getForwardCurveName(), listForward);
    final PresentValueSensitivity result = new PresentValueSensitivity(resultMap);
    return result;
  }

}
