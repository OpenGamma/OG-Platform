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

import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverageFlatCompoundingSpread;
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
public final class CouponIborAverageFlatCompoundingSpreadDiscountingMethod {

  /**
   * The method unique instance.
   */
  private static final CouponIborAverageFlatCompoundingSpreadDiscountingMethod INSTANCE = new CouponIborAverageFlatCompoundingSpreadDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponIborAverageFlatCompoundingSpreadDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponIborAverageFlatCompoundingSpreadDiscountingMethod() {
  }

  /**
   * Compute the present value of a Ibor average coupon by discounting.
   * @param coupon The coupon.
   * @param multicurves The multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CouponIborAverageFlatCompoundingSpread coupon, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurves, "Multi-curves provider");

    final int nPeriods = coupon.getFixingTime().length;
    double payoff = coupon.getAmountAccrued();
    for (int i = 0; i < nPeriods; ++i) {
      double forward = i == 0 ? coupon.getRateFixed() : 0.0;
      final int nDates = coupon.getFixingTime()[i].length;
      for (int j = 0; j < nDates; ++j) {
        final double forward1 = multicurves.getSimplyCompoundForwardRate(coupon.getIndex(), coupon.getFixingPeriodStartTime()[i][j], coupon.getFixingPeriodEndTime()[i][j],
            coupon.getFixingPeriodAccrualFactor()[i][j]);
        forward += coupon.getWeight()[i][j] * forward1;
      }
      payoff += (coupon.getSpread() + forward + payoff * forward) * coupon.getPaymentAccrualFactors()[i];
    }
    final double df = multicurves.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    final double pv = coupon.getNotional() * payoff * df;
    return MultipleCurrencyAmount.of(coupon.getCurrency(), pv);
  }

  /**
   * Compute the present value sensitivity to yield for discounting curve and forward rate (in index convention) for forward curve.
   * @param coupon The coupon.
   * @param multicurve The multi-curve provider.
   * @return The present value sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final CouponIborAverageFlatCompoundingSpread coupon, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurve, "Curves");

    final int nPeriods = coupon.getFixingTime().length;
    final int[] nDates = new int[nPeriods];
    final double[] forwards = new double[nPeriods];
    final double[] cpaSum = new double[nPeriods];
    double payoff = coupon.getAmountAccrued();
    forwards[0] = coupon.getRateFixed();
    for (int i = 0; i < nPeriods; ++i) {
      nDates[i] = coupon.getFixingTime()[i].length;
      for (int j = 0; j < nDates[i]; ++j) {
        final double forward = multicurve.getSimplyCompoundForwardRate(coupon.getIndex(), coupon.getFixingPeriodStartTime()[i][j], coupon.getFixingPeriodEndTime()[i][j],
            coupon.getFixingPeriodAccrualFactor()[i][j]);
        forwards[i] += coupon.getWeight()[i][j] * forward;
      }
      payoff += (coupon.getSpread() + forwards[i] + payoff * forwards[i]) * coupon.getPaymentAccrualFactors()[i];
      cpaSum[i] = payoff;
    }
    final double df = multicurve.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());

    final double forwardBar = coupon.getNotional() * df;
    final double dfBar = coupon.getNotional() * payoff;
    final double[][][] cpaBar = new double[nPeriods][nPeriods][];
    for (int i = 0; i < nPeriods; ++i) {
      for (int j = 0; j < nPeriods; ++j) {
        cpaBar[i][j] = new double[nDates[j]];
      }
    }
    for (int j = 0; j < nDates[0]; ++j) {
      cpaBar[0][0][j] += coupon.getWeight()[0][j] * (1.0 + coupon.getAmountAccrued()) * coupon.getPaymentAccrualFactors()[0];
    }
    for (int i = 1; i < nPeriods; ++i) {
      for (int k = 0; k < nDates[i]; ++k) {
        cpaBar[i][i][k] += coupon.getWeight()[i][k] * (1.0 + cpaSum[i - 1]) * coupon.getPaymentAccrualFactors()[i];
      }
      for (int l = 0; l < i; ++l) {
        for (int j = 0; j < nPeriods; ++j) {
          for (int k = 0; k < nDates[j]; ++k) {
            cpaBar[i][j][k] += cpaBar[l][j][k] * forwards[i] * coupon.getPaymentAccrualFactors()[i];
          }
        }
      }
    }

    final double[][] forwardBars = new double[nPeriods][];
    for (int j = 0; j < nPeriods; ++j) {
      forwardBars[j] = new double[nDates[j]];
      for (int i = 0; i < nPeriods; ++i) {
        for (int k = 0; k < nDates[j]; ++k) {
          forwardBars[j][k] += cpaBar[i][j][k] * forwardBar;
        }
      }
    }
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(coupon.getPaymentTime(), -coupon.getPaymentTime() * df * dfBar));
    mapDsc.put(multicurve.getName(coupon.getCurrency()), listDiscounting);

    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<>();
    final List<ForwardSensitivity> listForward = new ArrayList<>();
    for (int i = 0; i < nPeriods; ++i) {
      for (int j = 0; j < nDates[i]; ++j) {
        listForward.add(new SimplyCompoundedForwardSensitivity(coupon.getFixingPeriodStartTime()[i][j], coupon.getFixingPeriodEndTime()[i][j], coupon.getFixingPeriodAccrualFactor()[i][j],
            forwardBars[i][j]));
      }
    }
    mapFwd.put(multicurve.getName(coupon.getIndex()), listForward);
    return MultipleCurrencyMulticurveSensitivity.of(coupon.getCurrency(), MulticurveSensitivity.of(mapDsc, mapFwd));
  }
}
