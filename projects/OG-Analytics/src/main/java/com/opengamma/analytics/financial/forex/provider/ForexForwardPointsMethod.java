/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveForwardPointsProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Pricing method for Forex transactions (spot or forward) by forward points.
 * <p>Documentation: Forex Swaps and Cross-currency Swaps. OpenGamma Documentation n. 21
 */
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
   * No coherence check is done between the interest curves and the forward points curves.
   * @param fx The Forex derivative.
   * @param multicurves The multi-curves provider.
   * @param forwardPoints The curve with the forward points
   * @return The multi-currency present value (in currency 2).
   */
  public MultipleCurrencyAmount presentValue(final Forex fx, final MulticurveProviderInterface multicurves, final DoublesCurve forwardPoints) {
    final double fxRate = multicurves.getFxRate(fx.getCurrency1(), fx.getCurrency2());
    final double payTime = fx.getPaymentTime();
    final double fwdPts = forwardPoints.getYValue(payTime);
    final double amount1 = fx.getPaymentCurrency1().getAmount();
    final double amount2 = fx.getPaymentCurrency2().getAmount();
    final double df2 = multicurves.getDiscountFactor(fx.getCurrency2(), payTime);
    final double pv = df2 * (amount2 + amount1 * (fxRate + fwdPts));
    return MultipleCurrencyAmount.of(fx.getCurrency2(), pv);
  }

  public MultipleCurrencyAmount presentValue(final Forex fx, final MulticurveForwardPointsProviderInterface multicurvesForward) {
    ArgumentChecker.notNull(multicurvesForward, "multi-curve provider");
    ArgumentChecker.isTrue(multicurvesForward.getCurrencyPair().getFirst().equals(fx.getCurrency1()), "Currency 1");
    ArgumentChecker.isTrue(multicurvesForward.getCurrencyPair().getSecond().equals(fx.getCurrency2()), "Currency 2");
    return presentValue(fx, multicurvesForward.getMulticurveProvider(), multicurvesForward.getForwardPointsCurve());
  }

  /**
   * Compute the currency exposure by estimating the forward points (on the second currency). The present value is computed in the second currency.
   * @param fx The Forex derivative.
   * @param multicurves The multi-curves provider.
   * @param forwardPoints The curve with the forward points
   * @return The currency exposure.
   */
  public MultipleCurrencyAmount currencyExposure(final Forex fx, final MulticurveProviderInterface multicurves, final DoublesCurve forwardPoints) {
    final double fxRate = multicurves.getFxRate(fx.getCurrency1(), fx.getCurrency2());
    final double payTime = fx.getPaymentTime();
    final double fwdPts = forwardPoints.getYValue(payTime);
    final double amount1 = fx.getPaymentCurrency1().getAmount();
    final double amount2 = fx.getPaymentCurrency2().getAmount();
    final double df2 = multicurves.getDiscountFactor(fx.getCurrency2(), payTime);
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
   * @param multicurves The multi-curve provider.
   * @param forwardPoints The curve with the forward points
   * @return The sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final Forex fx, final MulticurveProviderInterface multicurves, final DoublesCurve forwardPoints) {
    final double fxRate = multicurves.getFxRate(fx.getCurrency1(), fx.getCurrency2());
    final double payTime = fx.getPaymentTime();
    final double fwdPts = forwardPoints.getYValue(payTime);
    final double amount1 = fx.getPaymentCurrency1().getAmount();
    final double amount2 = fx.getPaymentCurrency2().getAmount();
    final double df2 = multicurves.getDiscountFactor(fx.getCurrency2(), payTime);
    // Backward sweep
    final double pvBar = 1.0;
    final double df2Bar = (amount2 + amount1 * (fxRate + fwdPts)) * pvBar;
    final DoublesPair s = new DoublesPair(payTime, -payTime * df2 * df2Bar);
    final List<DoublesPair> list = new ArrayList<>();
    list.add(s);
    final Map<String, List<DoublesPair>> result = new HashMap<>();
    result.put(multicurves.getName(fx.getCurrency2()), list);
    return MultipleCurrencyMulticurveSensitivity.of(fx.getCurrency2(), MulticurveSensitivity.ofYieldDiscounting(result));
  }

  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final Forex fx, final MulticurveForwardPointsProviderInterface multicurvesForward) {
    ArgumentChecker.notNull(multicurvesForward, "multi-curve provider");
    ArgumentChecker.isTrue(multicurvesForward.getCurrencyPair().getFirst().equals(fx.getCurrency1()), "Currency 1");
    ArgumentChecker.isTrue(multicurvesForward.getCurrencyPair().getSecond().equals(fx.getCurrency2()), "Currency 2");
    return presentValueCurveSensitivity(fx, multicurvesForward.getMulticurveProvider(), multicurvesForward.getForwardPointsCurve());
  }

  /**
   * Computes the sensitivity of the present value to the figures in the forward points curves.
   * @param fx The Forex derivative.
   * @param multicurves The multi-curve provider.
   * @param forwardPoints The curve with the forward points
   * @return The sensitivity.
   */
  public double[] presentValueForwardPointsSensitivity(final Forex fx, final MulticurveProviderInterface multicurves, final DoublesCurve forwardPoints) {
    final double payTime = fx.getPaymentTime();
    final double amount1 = fx.getPaymentCurrency1().getAmount();
    final double df2 = multicurves.getDiscountFactor(fx.getCurrency2(), payTime);
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
