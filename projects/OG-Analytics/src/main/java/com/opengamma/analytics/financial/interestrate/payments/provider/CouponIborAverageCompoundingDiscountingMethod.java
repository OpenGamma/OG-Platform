/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverageCompounding;
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
public final class CouponIborAverageCompoundingDiscountingMethod {

  /**
   * The method unique instance.
   */
  private static final CouponIborAverageCompoundingDiscountingMethod INSTANCE = new CouponIborAverageCompoundingDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponIborAverageCompoundingDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponIborAverageCompoundingDiscountingMethod() {
  }

  /**
   * Compute the present value of a Ibor average coupon by discounting.
   * @param coupon The coupon.
   * @param multicurves The multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CouponIborAverageCompounding coupon, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurves, "Multi-curves provider");

    final int nPeriods = coupon.getFixingTime().length;
    final int nDates = coupon.getFixingTime()[0].length;
    double amountAccrued = 1.0;
    for (int i = 0; i < nPeriods; ++i) {
      double forward = 0.;
      for (int j = 0; j < nDates; ++j) {
        final double forward1 = multicurves.getSimplyCompoundForwardRate(coupon.getIndex(), coupon.getFixingPeriodStartTime()[i][j], coupon.getFixingPeriodEndTime()[i][j],
            coupon.getFixingPeriodAccrualFactor()[i][j]);
        forward += coupon.getWeight()[i][j] * forward1;
      }
      amountAccrued *= (1.0 + forward * coupon.getPaymentAccrualFactors()[i]);
    }
    amountAccrued *= coupon.getAmountAccrued();
    final double df = multicurves.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    final double pv = coupon.getNotional() * (amountAccrued - 1.0) * df;
    return MultipleCurrencyAmount.of(coupon.getCurrency(), pv);
  }

  /**
   * Compute the present value sensitivity to yield for discounting curve and forward rate (in index convention) for forward curve.
   * @param coupon The coupon.
   * @param multicurve The multi-curve provider.
   * @return The present value sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final CouponIborAverageCompounding coupon, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurve, "Curves");
    final int nPeriods = coupon.getFixingTime().length;
    final int nDates = coupon.getFixingTime()[0].length;
    final double[] fctr = new double[nPeriods];
    Arrays.fill(fctr, 0.0);
    double amountAccrued = 1.0;
    for (int i = 0; i < nPeriods; ++i) {
      double forward = 0.;
      for (int j = 0; j < nDates; ++j) {
        final double forward1 = multicurve.getSimplyCompoundForwardRate(coupon.getIndex(), coupon.getFixingPeriodStartTime()[i][j], coupon.getFixingPeriodEndTime()[i][j],
            coupon.getFixingPeriodAccrualFactor()[i][j]);
        forward += coupon.getWeight()[i][j] * forward1;
      }
      fctr[i] = (1.0 + forward * coupon.getPaymentAccrualFactors()[i]);
      amountAccrued *= fctr[i];
    }
    amountAccrued *= coupon.getAmountAccrued();
    final double df = multicurve.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());

    final double forwardBar = coupon.getNotional() * df;
    final double dfBar = coupon.getNotional() * (amountAccrued - 1.0);
    final double[][] forwardBars = new double[nPeriods][nDates];
    for (int i = 0; i < nPeriods; ++i) {
      for (int j = 0; j < nDates; ++j) {
        forwardBars[i][j] = coupon.getWeight()[i][j] * forwardBar * amountAccrued * coupon.getPaymentAccrualFactors()[i] / fctr[i];
      }
    }
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(coupon.getPaymentTime(), -coupon.getPaymentTime() * df * dfBar));
    mapDsc.put(multicurve.getName(coupon.getCurrency()), listDiscounting);

    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<>();
    final List<ForwardSensitivity> listForward = new ArrayList<>();
    for (int i = 0; i < nPeriods; ++i) {
      for (int j = 0; j < nDates; ++j) {
        listForward.add(new SimplyCompoundedForwardSensitivity(coupon.getFixingPeriodStartTime()[i][j], coupon.getFixingPeriodEndTime()[i][j], coupon.getFixingPeriodAccrualFactor()[i][j],
            forwardBars[i][j]));
      }
    }
    mapFwd.put(multicurve.getName(coupon.getIndex()), listForward);
    return MultipleCurrencyMulticurveSensitivity.of(coupon.getCurrency(), MulticurveSensitivity.of(mapDsc, mapFwd));
  }
}
