/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverageFixingDates;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimplyCompoundedForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public final class CouponIborAverageFixingDatesDiscountingMethod {

  /**
   * The method unique instance.
   */
  private static final CouponIborAverageFixingDatesDiscountingMethod INSTANCE = new CouponIborAverageFixingDatesDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponIborAverageFixingDatesDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponIborAverageFixingDatesDiscountingMethod() {
  }

  /**
   * Compute the present value of a Ibor average coupon by discounting.
   * @param coupon The coupon.
   * @param multicurves The multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CouponIborAverageFixingDates coupon, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurves, "Multi-curves provider");

    final int nDates = coupon.getFixingPeriodEndTime().length;
    double forward = 0.;
    for (int i = 0; i < nDates; ++i) {
      final double forward1 = multicurves.getSimplyCompoundForwardRate(coupon.getIndex(), coupon.getFixingPeriodStartTime()[i], coupon.getFixingPeriodEndTime()[i],
          coupon.getFixingPeriodAccrualFactor()[i]);
      forward += coupon.getWeight()[i] * forward1;
    }
    forward += coupon.getAmountAccrued();
    final double df = multicurves.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    final double pv = coupon.getNotional() * coupon.getPaymentYearFraction() * forward * df;
    return MultipleCurrencyAmount.of(coupon.getCurrency(), pv);
  }

  /**
   * Compute the present value sensitivity to yield for discounting curve and forward rate (in index convention) for forward curve.
   * @param coupon The coupon.
   * @param multicurve The multi-curve provider.
   * @return The present value sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final CouponIborAverageFixingDates coupon, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurve, "Curves");
    final int nDates = coupon.getFixingPeriodEndTime().length;
    double forward = 0.;
    for (int i = 0; i < nDates; ++i) {
      final double forward1 = multicurve.getSimplyCompoundForwardRate(coupon.getIndex(), coupon.getFixingPeriodStartTime()[i], coupon.getFixingPeriodEndTime()[i],
          coupon.getFixingPeriodAccrualFactor()[i]);
      forward += coupon.getWeight()[i] * forward1;
    }
    forward += coupon.getAmountAccrued();
    final double df = multicurve.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());

    final double forwardBar = coupon.getNotional() * coupon.getPaymentYearFraction() * df;
    final double dfBar = coupon.getNotional() * coupon.getPaymentYearFraction() * forward;
    final double[] forwardBars = new double[nDates];
    for (int i = 0; i < nDates; ++i) {
      forwardBars[i] = coupon.getWeight()[i] * forwardBar;
    }
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(coupon.getPaymentTime(), -coupon.getPaymentTime() * df * dfBar));
    mapDsc.put(multicurve.getName(coupon.getCurrency()), listDiscounting);

    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<>();
    final List<ForwardSensitivity> listForward = new ArrayList<>();
    for (int i = 0; i < nDates; ++i) {
      listForward.add(new SimplyCompoundedForwardSensitivity(coupon.getFixingPeriodStartTime()[i], coupon.getFixingPeriodEndTime()[i], coupon.getFixingPeriodAccrualFactor()[i], forwardBars[i]));
    }
    mapFwd.put(multicurve.getName(coupon.getIndex()), listForward);
    return MultipleCurrencyMulticurveSensitivity.of(coupon.getCurrency(), MulticurveSensitivity.of(mapDsc, mapFwd));
  }

}
