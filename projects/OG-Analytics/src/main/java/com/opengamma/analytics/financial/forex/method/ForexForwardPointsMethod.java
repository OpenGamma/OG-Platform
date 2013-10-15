/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.method;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Pricing method for Forex transactions (spot or forward) by forward points.
 * <p>Documentation: Forex Swaps and Cross-currency Swaps. OpenGamma Documentation n. 21
 * @deprecated {@link YieldCurveBundle} is deprecated. Use {@link com.opengamma.analytics.financial.forex.provider.ForexForwardPointsMethod}
 */
@Deprecated
public final class ForexForwardPointsMethod {

  /**
   * The method unique instance.
   */
  private static final ForexForwardPointsMethod INSTANCE = new ForexForwardPointsMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static ForexForwardPointsMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private ForexForwardPointsMethod() {
  }

  /**
   * Compute the present value by estimating the forward points (on the second currency). The present value is computed in the second currency.
   * @param fx The Forex derivative.
   * @param curves The curve bundle containing the discounting curves.
   * @param forwardPoints The curve with the forward points
   * @return The multi-currency present value (in currency 2).
   */
  public MultipleCurrencyAmount presentValue(final Forex fx, final YieldCurveBundle curves, final DoublesCurve forwardPoints) {
    final double fxRate = curves.getFxRates().getFxRate(fx.getCurrency1(), fx.getCurrency2());
    final double payTime = fx.getPaymentTime();
    final double fwdPts = forwardPoints.getYValue(payTime);
    final double amount1 = fx.getPaymentCurrency1().getAmount();
    final double amount2 = fx.getPaymentCurrency2().getAmount();
    final double df2 = curves.getCurve(fx.getPaymentCurrency2().getFundingCurveName()).getDiscountFactor(payTime);
    final double pv = df2 * (amount2 + amount1 * (fxRate + fwdPts));
    return MultipleCurrencyAmount.of(fx.getCurrency2(), pv);
  }

  /**
   * Compute the present value by estimating the forward points (on the second currency). The present value is computed in the second currency.
   * @param fx The Forex derivative.
   * @param curves The curve bundle containing the discounting curves.
   * @param forwardPoints The curve with the forward points
   * @return The multi-currency present value (in currency 2).
   */
  public MultipleCurrencyAmount currencyExposure(final Forex fx, final YieldCurveBundle curves, final DoublesCurve forwardPoints) {
    final double fxRate = curves.getFxRates().getFxRate(fx.getCurrency1(), fx.getCurrency2());
    final double payTime = fx.getPaymentTime();
    final double fwdPts = forwardPoints.getYValue(payTime);
    final double amount1 = fx.getPaymentCurrency1().getAmount();
    final double amount2 = fx.getPaymentCurrency2().getAmount();
    final double df2 = curves.getCurve(fx.getPaymentCurrency2().getFundingCurveName()).getDiscountFactor(payTime);
    final double ce1 = amount1 * df2 * (1.0d + fwdPts / fxRate);
    final double ce2 = amount2 * df2;
    MultipleCurrencyAmount ce = MultipleCurrencyAmount.of(fx.getCurrency1(), ce1);
    ce = ce.plus(fx.getCurrency2(), ce2);
    return ce;
  }

  /**
   * Computes the present value curve sensitivity for forex by forward point method.
   * The sensitivity is only to the final discounting, not to the forward points.
   * @param fx The Forex derivative.
   * @param curves The curve bundle containing the discounting curves.
   * @param forwardPoints The curve with the forward points
   * @return The sensitivity.
   */
  public MultipleCurrencyInterestRateCurveSensitivity presentValueCurveSensitivity(final Forex fx, final YieldCurveBundle curves, final DoublesCurve forwardPoints) {
    final double fxRate = curves.getFxRates().getFxRate(fx.getCurrency1(), fx.getCurrency2());
    final double payTime = fx.getPaymentTime();
    final double fwdPts = forwardPoints.getYValue(payTime);
    final double amount1 = fx.getPaymentCurrency1().getAmount();
    final double amount2 = fx.getPaymentCurrency2().getAmount();
    final double df2 = curves.getCurve(fx.getPaymentCurrency2().getFundingCurveName()).getDiscountFactor(payTime);
    // Backward sweep
    final double pvBar = 1.0;
    final double df2Bar = (amount2 + amount1 * (fxRate + fwdPts)) * pvBar;
    final DoublesPair s = DoublesPair.of(payTime, -payTime * df2 * df2Bar);
    final List<DoublesPair> list = new ArrayList<>();
    list.add(s);
    return MultipleCurrencyInterestRateCurveSensitivity.of(fx.getCurrency2(), InterestRateCurveSensitivity.of(fx.getPaymentCurrency2().getFundingCurveName(), list));
  }

  /**
   * Computes the sensitivity of the present value to the figures in the forward points curves.
   * @param fx The Forex derivative.
   * @param curves The curve bundle containing the discounting curves.
   * @param forwardPoints The curve with the forward points
   * @return The sensitivity.
   */
  public double[] presentValueForwardPointsSensitivity(final Forex fx, final YieldCurveBundle curves, final DoublesCurve forwardPoints) {
    final double payTime = fx.getPaymentTime();
    final double amount1 = fx.getPaymentCurrency1().getAmount();
    final double df2 = curves.getCurve(fx.getPaymentCurrency2().getFundingCurveName()).getDiscountFactor(payTime);
    // Backward sweep
    final double pvBar = 1.0;
    final double fwdPtsBar = df2 * amount1 * pvBar;
    final Double[] fwdPtsDp = forwardPoints.getYValueParameterSensitivity(payTime);
    final double[] sensitivity = new double[fwdPtsDp.length];
    for (int loops = 0; loops < fwdPtsDp.length; loops++) {
      sensitivity[loops] = fwdPtsDp[loops] * fwdPtsBar;
    }
    return sensitivity;
  }

}
