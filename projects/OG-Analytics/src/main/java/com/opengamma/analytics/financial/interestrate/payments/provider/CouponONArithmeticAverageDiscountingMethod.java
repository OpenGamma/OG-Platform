/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverage;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimplyCompoundedForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class describing the pricing of Fed Fund swap-like floating coupon (arithmetic average on overnight rates) by estimation and discounting (no convexity adjustment is computed).
 * The estimation is done by estimating each ON forward rate and averaging them.
 * <p>Reference: Overnight Indexes Related Products. OpenGamma Documentation n. 20, Version 1.0, February 2013.
 */
public final class CouponONArithmeticAverageDiscountingMethod {
  // FIXME: Class under construction, don't use yet.

  /**
   * The method unique instance.
   */
  private static final CouponONArithmeticAverageDiscountingMethod INSTANCE = new CouponONArithmeticAverageDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponONArithmeticAverageDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponONArithmeticAverageDiscountingMethod() {
  }

  /**
   * Computes the present value.
   * @param coupon The coupon.
   * @param multicurve The multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CouponONArithmeticAverage coupon, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurve, "Multi-curve provider");
    final double[] delta = coupon.getFixingPeriodAccrualFactors();
    final double[] startTimes = coupon.getFixingPeriodStartTimes();
    final double[] endTimes = coupon.getFixingPeriodEndTimes();
    final int nbFwd = delta.length;
    final double[] forwardON = new double[nbFwd];
    double rateAccrued = coupon.getRateAccrued();
    for (int loopfwd = 0; loopfwd < nbFwd; loopfwd++) {
      forwardON[loopfwd] = multicurve.getSimplyCompoundForwardRate(coupon.getIndex(), startTimes[loopfwd], endTimes[loopfwd], delta[loopfwd]);
      rateAccrued += forwardON[loopfwd] * delta[loopfwd];
    }
    final double df = multicurve.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    final double pv = df * rateAccrued * coupon.getNotional(); // Does not use the payment accrual factor.
    return MultipleCurrencyAmount.of(coupon.getCurrency(), pv);
  }

  /**
   * Computes the present value.
   * @param coupon The coupon.
   * @param multicurve The multi-curve provider.
   * @return The present value curve sensitivities.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final CouponONArithmeticAverage coupon, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurve, "Multi-curve provider");
    // Forward sweep
    final double[] delta = coupon.getFixingPeriodAccrualFactors();
    final double[] startTimes = coupon.getFixingPeriodStartTimes();
    final double[] endTimes = coupon.getFixingPeriodEndTimes();
    ;
    final int nbFwd = delta.length;
    final double[] forwardON = new double[nbFwd];
    double rateAccrued = coupon.getRateAccrued();
    for (int loopfwd = 0; loopfwd < nbFwd; loopfwd++) {
      forwardON[loopfwd] = multicurve.getSimplyCompoundForwardRate(coupon.getIndex(), startTimes[loopfwd], endTimes[loopfwd], delta[loopfwd]);
      rateAccrued += forwardON[loopfwd] * delta[loopfwd];
    }
    final double df = multicurve.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    // Backward sweep
    final double pvBar = 1.0;
    final double dfBar = rateAccrued * coupon.getNotional() * pvBar;
    final double rateAccruedBar = df * coupon.getNotional() * pvBar;
    final double[] forwardONBar = new double[nbFwd];
    for (int loopfwd = 0; loopfwd < nbFwd; loopfwd++) {
      forwardONBar[loopfwd] = delta[loopfwd] * rateAccruedBar;
    }
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(coupon.getPaymentTime(), -coupon.getPaymentTime() * df * dfBar));
    mapDsc.put(multicurve.getName(coupon.getCurrency()), listDiscounting);
    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<>();
    final List<ForwardSensitivity> listForward = new ArrayList<>();
    for (int loopfwd = 0; loopfwd < nbFwd; loopfwd++) {
      listForward.add(new SimplyCompoundedForwardSensitivity(startTimes[loopfwd], endTimes[loopfwd], delta[loopfwd], forwardONBar[loopfwd]));
    }
    mapFwd.put(multicurve.getName(coupon.getIndex()), listForward);
    final MultipleCurrencyMulticurveSensitivity result = MultipleCurrencyMulticurveSensitivity.of(coupon.getCurrency(), MulticurveSensitivity.of(mapDsc, mapFwd));
    return result;
  }

}
