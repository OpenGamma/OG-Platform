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
 * The estimation is done through an approximation.
 * <p>Reference: Overnight Indexes Related Products. OpenGamma Documentation n. 20, Version 1.0, February 2013.
 */
public final class CouponArithmeticAverageONDiscountingApproxMethod {
 // FIXME: Class under construction, don't use yet.

  /**
   * The method unique instance.
   */
  private static final CouponArithmeticAverageONDiscountingApproxMethod INSTANCE = new CouponArithmeticAverageONDiscountingApproxMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponArithmeticAverageONDiscountingApproxMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponArithmeticAverageONDiscountingApproxMethod() {
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
    final double[] times = coupon.getFixingPeriodTimes();
    final int nbFwd = times.length - 1;
    final double delta = coupon.getFixingPeriodRemainingAccrualFactor();
    final double rateAccruedCompounded = multicurve.getForwardRate(coupon.getIndex(), times[0], times[nbFwd], delta) * delta;
    final double rateAccrued = coupon.getRateAccrued() + Math.log(1.0 + rateAccruedCompounded);
    final double df = multicurve.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    final double pv = df * rateAccrued * coupon.getNotional();
    return MultipleCurrencyAmount.of(coupon.getCurrency(), pv);
  }

  /**
   * Computes the present value curve sensitivity.
   * @param coupon The coupon.
   * @param multicurve The multi-curve provider.
   * @return The present value curve sensitivities.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final CouponArithmeticAverageON coupon, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurve, "Multi-curve provider");
    // Forward sweep
    final double[] times = coupon.getFixingPeriodTimes();
    final int nbFwd = times.length - 1;
    final double delta = coupon.getFixingPeriodRemainingAccrualFactor();
    final double forward = multicurve.getForwardRate(coupon.getIndex(), times[0], times[nbFwd], delta);
    final double rateAccruedCompounded = forward * delta;
    final double rateAccrued = coupon.getRateAccrued() + Math.log(1.0 + rateAccruedCompounded);
    final double df = multicurve.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    // Backward sweep
    final double pvBar = 1.0;
    final double dfBar = rateAccrued * coupon.getNotional() * pvBar;
    final double rateAccruedBar = df * coupon.getNotional() * pvBar;
    final double rateAccruedCompoundedBar = rateAccruedBar / (1.0 + rateAccruedCompounded);
    final double forwardBar = delta * rateAccruedCompoundedBar;
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(new DoublesPair(coupon.getPaymentTime(), -coupon.getPaymentTime() * df * dfBar));
    mapDsc.put(multicurve.getName(coupon.getCurrency()), listDiscounting);
    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<>();
    final List<ForwardSensitivity> listForward = new ArrayList<>();
    listForward.add(new ForwardSensitivity(times[0], times[nbFwd], delta, forwardBar));
    mapFwd.put(multicurve.getName(coupon.getIndex()), listForward);
    final MultipleCurrencyMulticurveSensitivity result = MultipleCurrencyMulticurveSensitivity.of(coupon.getCurrency(), MulticurveSensitivity.of(mapDsc, mapFwd));
    return result;
  }

}
