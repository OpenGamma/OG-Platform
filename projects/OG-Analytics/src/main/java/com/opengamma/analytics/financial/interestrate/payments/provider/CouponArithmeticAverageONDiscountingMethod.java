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

import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponArithmeticAverageON;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class describing the pricing of Fed Fund swap-like floating coupon (arithmetic average on overnight rates) by estimation and discounting (no convexity adjustment is computed).
 * The estimation is done by estimating each ON forward rate and averaging them.
 * <p>Reference: Overnight Indexes Related Products. OpenGamma Documentation n. 20, Version 1.0, February 2013.
 * FIXME: Class under construction, don't use yet.
 */
public final class CouponArithmeticAverageONDiscountingMethod {

  /**
   * The method unique instance.
   */
  private static final CouponArithmeticAverageONDiscountingMethod INSTANCE = new CouponArithmeticAverageONDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponArithmeticAverageONDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponArithmeticAverageONDiscountingMethod() {
  }

  /**
   * Computes the present value. 
   * @param coupon The coupon.
   * @param multicurve The multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CouponArithmeticAverageON coupon, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurve, "Multi-curve provider");
    double[] delta = coupon.getFixingPeriodAccrualFactors();
    double[] times = coupon.getFixingPeriodTimes();
    int nbFwd = delta.length;
    double[] forwardON = new double[nbFwd];
    double rateAccrued = coupon.getRateAccrued();
    for (int loopfwd = 0; loopfwd < nbFwd; loopfwd++) {
      forwardON[loopfwd] = multicurve.getForwardRate(coupon.getIndex(), times[loopfwd], times[loopfwd + 1], delta[loopfwd]);
      rateAccrued += forwardON[loopfwd] * delta[loopfwd];
    }
    double df = multicurve.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    double pv = df * rateAccrued * coupon.getNotional(); // Does not use the payment accrual factor.
    return MultipleCurrencyAmount.of(coupon.getCurrency(), pv);
  }

  /**
   * Computes the present value.
   * @param coupon The coupon.
   * @param multicurve The multi-curve provider.
   * @return The present value curve sensitivities.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final CouponArithmeticAverageON coupon, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurve, "Multi-curve provider");
    // Forward sweep
    double[] delta = coupon.getFixingPeriodAccrualFactors();
    double[] times = coupon.getFixingPeriodTimes();
    int nbFwd = delta.length;
    double[] forwardON = new double[nbFwd];
    double rateAccrued = coupon.getRateAccrued();
    for (int loopfwd = 0; loopfwd < nbFwd; loopfwd++) {
      forwardON[loopfwd] = multicurve.getForwardRate(coupon.getIndex(), times[loopfwd], times[loopfwd + 1], delta[loopfwd]);
      rateAccrued += forwardON[loopfwd] * delta[loopfwd];
    }
    double df = multicurve.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    // Backward sweep
    double pvBar = 1.0;
    double dfBar = rateAccrued * coupon.getNotional() * pvBar;
    double rateAccruedBar = df * coupon.getNotional() * pvBar;
    double[] forwardONBar = new double[nbFwd];
    for (int loopfwd = 0; loopfwd < nbFwd; loopfwd++) {
      forwardONBar[loopfwd] = delta[loopfwd] * rateAccruedBar;
    }
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<String, List<DoublesPair>>();
    final List<DoublesPair> listDiscounting = new ArrayList<DoublesPair>();
    listDiscounting.add(new DoublesPair(coupon.getPaymentTime(), -coupon.getPaymentTime() * df * dfBar));
    mapDsc.put(multicurve.getName(coupon.getCurrency()), listDiscounting);
    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<String, List<ForwardSensitivity>>();
    final List<ForwardSensitivity> listForward = new ArrayList<ForwardSensitivity>();
    for (int loopfwd = 0; loopfwd < nbFwd; loopfwd++) {
      listForward.add(new ForwardSensitivity(times[loopfwd], times[loopfwd + 1], delta[loopfwd], forwardONBar[loopfwd]));
    }
    mapFwd.put(multicurve.getName(coupon.getIndex()), listForward);
    final MultipleCurrencyMulticurveSensitivity result = MultipleCurrencyMulticurveSensitivity.of(coupon.getCurrency(), MulticurveSensitivity.of(mapDsc, mapFwd));
    return result;
  }

}
