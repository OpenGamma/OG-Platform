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
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to compute the price for an interest rate future with discounting (like a forward).
 * No convexity adjustment is done.
 * @deprecated Use {@link com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureSecurityDiscountingMethodDeprecated}
 */
@Deprecated
public final class InterestRateFutureSecurityDiscountingMethod extends InterestRateFutureSecurityMethod {

  /**
   * The unique instance of the calculator.
   */
  private static final InterestRateFutureSecurityDiscountingMethod INSTANCE = new InterestRateFutureSecurityDiscountingMethod();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static InterestRateFutureSecurityDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private InterestRateFutureSecurityDiscountingMethod() {
  }

  /**
   * Computes the price of a future from the curves using an estimation of the future rate without convexity adjustment.
   * @param future The future.
   * @param curves The yield curves. Should contain the forward curve associated.
   * @return The price.
   */
  public double price(final InterestRateFutureSecurity future, final YieldCurveBundle curves) {
    Validate.notNull(future, "Future");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(future.getForwardCurveName());
    final double forward = (forwardCurve.getDiscountFactor(future.getFixingPeriodStartTime()) / forwardCurve.getDiscountFactor(future.getFixingPeriodEndTime()) - 1)
        / future.getFixingPeriodAccrualFactor();
    final double price = 1.0 - forward;
    return price;
  }

  /**
   * Compute the price sensitivity to rates of an interest rate future by discounting.
   * @param future The future.
   * @param curves The yield curves. Should contain the forward curve associated.
   * @return The price rate sensitivity.
   */
  @Override
  public InterestRateCurveSensitivity priceCurveSensitivity(final InterestRateFutureSecurity future, final YieldCurveBundle curves) {
    Validate.notNull(future, "Future");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(future.getForwardCurveName());
    final double dfForwardStart = forwardCurve.getDiscountFactor(future.getFixingPeriodStartTime());
    final double dfForwardEnd = forwardCurve.getDiscountFactor(future.getFixingPeriodEndTime());
    // Partials - XBar => d(price)/dX
    final double priceBar = 1.0;
    final double forwardBar = -priceBar;
    final double dfForwardEndBar = -dfForwardStart / (dfForwardEnd * dfForwardEnd) / future.getFixingPeriodAccrualFactor() * forwardBar;
    final double dfForwardStartBar = 1.0 / (future.getFixingPeriodAccrualFactor() * dfForwardEnd) * forwardBar;
    final Map<String, List<DoublesPair>> resultMap = new HashMap<>();
    final List<DoublesPair> listForward = new ArrayList<>();
    final List<DoublesPair> listFunding = new ArrayList<>();
    listForward.add(DoublesPair.of(future.getFixingPeriodStartTime(), -future.getFixingPeriodStartTime() * dfForwardStart * dfForwardStartBar));
    listForward.add(DoublesPair.of(future.getFixingPeriodEndTime(), -future.getFixingPeriodEndTime() * dfForwardEnd * dfForwardEndBar));
    resultMap.put(future.getDiscountingCurveName(), listFunding);
    resultMap.put(future.getForwardCurveName(), listForward);
    final InterestRateCurveSensitivity result = new InterestRateCurveSensitivity(resultMap);
    return result;
  }

  /**
   * Computes the future rate (1-price) from the curves using an estimation of the future rate without convexity adjustment.
   * @param future The future.
   * @param curves The yield curves. Should contain the forward curve associated.
   * @return The rate.
   */
  public double parRate(final InterestRateFutureSecurity future, final YieldCurveBundle curves) {
    Validate.notNull(future, "Future");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(future.getForwardCurveName());
    final double forward = (forwardCurve.getDiscountFactor(future.getFixingPeriodStartTime()) / forwardCurve.getDiscountFactor(future.getFixingPeriodEndTime()) - 1)
        / future.getFixingPeriodAccrualFactor();
    return forward;
  }

  /**
   * Computes the future rate (1-price) curve sensitivity from the curves using an estimation of the future rate without convexity adjustment.
   * @param future The future.
   * @param curves The yield curves. Should contain the forward curve associated.
   * @return The rate curve sensitivity.
   */
  public InterestRateCurveSensitivity parRateCurveSensitivity(final InterestRateFutureSecurity future, final YieldCurveBundle curves) {
    Validate.notNull(future, "Future");
    Validate.notNull(curves, "Curves");
    final String curveName = future.getForwardCurveName();
    final YieldAndDiscountCurve curve = curves.getCurve(curveName);
    final double ta = future.getFixingPeriodStartTime();
    final double tb = future.getFixingPeriodEndTime();
    final double ratio = curve.getDiscountFactor(ta) / curve.getDiscountFactor(tb) / future.getFixingPeriodAccrualFactor();
    final DoublesPair s1 = DoublesPair.of(ta, -ta * ratio);
    final DoublesPair s2 = DoublesPair.of(tb, tb * ratio);
    final List<DoublesPair> temp = new ArrayList<>();
    temp.add(s1);
    temp.add(s2);
    final Map<String, List<DoublesPair>> result = new HashMap<>();
    result.put(curveName, temp);
    return new InterestRateCurveSensitivity(result);
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    throw new UnsupportedOperationException("Present value not supported for STIR futures securities");
  }

  @Override
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final InterestRateFutureSecurity future, final YieldCurveBundle curves) {
    return presentValueCurveSensitivity(future, priceCurveSensitivity(future, curves));
  }
}
