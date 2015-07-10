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

import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverageFixingDatesCompoundingFlatSpread;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimplyCompoundedForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Pricing method related to coupon with average on a single index. Pricing done by simple forward and discounting.
 * No timing adjustment is done.
 */
public final class CouponIborAverageFixingDatesCompoundingFlatSpreadDiscountingMethod {

  /**
   * The method unique instance.
   */
  private static final CouponIborAverageFixingDatesCompoundingFlatSpreadDiscountingMethod INSTANCE =
      new CouponIborAverageFixingDatesCompoundingFlatSpreadDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponIborAverageFixingDatesCompoundingFlatSpreadDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponIborAverageFixingDatesCompoundingFlatSpreadDiscountingMethod() {
  }

  /**
   * Compute the present value of a Ibor average coupon by discounting.
   * @param coupon The coupon.
   * @param multicurves The multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CouponIborAverageFixingDatesCompoundingFlatSpread coupon, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurves, "Multi-curves provider");
    final int nPeriods = coupon.getFixingTime().length;
    double payoff = coupon.getRateFixed();
    for (int i = 0; i < nPeriods; ++i) {
      double forwardAverage = ((i == 0) ? coupon.getAmountAccrued() : 0.0);
      final int nDates = coupon.getFixingTime()[i].length;
      for (int j = 0; j < nDates; ++j) {
        final double forward1 = multicurves.getSimplyCompoundForwardRate(coupon.getIndex(), coupon.getFixingPeriodStartTime()[i][j], coupon.getFixingPeriodEndTime()[i][j],
            coupon.getFixingPeriodAccrualFactor()[i][j]);
        forwardAverage += coupon.getWeight()[i][j] * forward1;
      }
      payoff += (forwardAverage + coupon.getSpread() + forwardAverage * payoff) * coupon.getPaymentAccrualFactors()[i];
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
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final CouponIborAverageFixingDatesCompoundingFlatSpread coupon, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurve, "Curves");
    final int nPeriods = coupon.getFixingTime().length;
    final int[] nDates = new int[nPeriods];
    final double[] forwardAverage = new double[nPeriods];
    final double[] cpaSum = new double[nPeriods + 1];
    cpaSum[0] = coupon.getRateFixed();
    forwardAverage[0] = coupon.getAmountAccrued();
    for (int i = 0; i < nPeriods; ++i) {
      nDates[i] = coupon.getFixingTime()[i].length;
      for (int j = 0; j < nDates[i]; ++j) {
        final double forward = multicurve.getSimplyCompoundForwardRate(coupon.getIndex(), coupon.getFixingPeriodStartTime()[i][j], coupon.getFixingPeriodEndTime()[i][j],
            coupon.getFixingPeriodAccrualFactor()[i][j]);
        forwardAverage[i] += coupon.getWeight()[i][j] * forward;
      }
      cpaSum[i + 1] = cpaSum[i] + (forwardAverage[i] + coupon.getSpread() + cpaSum[i] * forwardAverage[i]) * coupon.getPaymentAccrualFactors()[i];
    }
    double payoff = cpaSum[nPeriods];
    final double df = multicurve.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    // Backward sweep
    final double payoffBar = coupon.getNotional() * df;
    final double dfBar = coupon.getNotional() * payoff;
    final double[] cpaSumBar = new double[nPeriods + 1];
    final double[] forwardAverageBar = new double[nPeriods];
    cpaSumBar[nPeriods] = payoffBar;
    for (int i = nPeriods - 1; i >= 0; i--) {
      cpaSumBar[i] += (1 + forwardAverage[i] * coupon.getPaymentAccrualFactors()[i]) * cpaSumBar[i + 1];
      forwardAverageBar[i] = (1 + cpaSum[i]) * coupon.getPaymentAccrualFactors()[i] * cpaSumBar[i + 1];
    }
    final double[][] forwardBar = new double[nPeriods][];
    for (int i = 0; i < nPeriods; ++i) {
      forwardBar[i] = new double[nDates[i]];
      for (int j = 0; j < nDates[i]; ++j) {
        forwardBar[i][j] += coupon.getWeight()[i][j] * forwardAverageBar[i];
      }
    }
    // Storing results
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(coupon.getPaymentTime(), -coupon.getPaymentTime() * df * dfBar));
    mapDsc.put(multicurve.getName(coupon.getCurrency()), listDiscounting);

    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<>();
    final List<ForwardSensitivity> listForward = new ArrayList<>();
    for (int i = 0; i < nPeriods; ++i) {
      for (int j = 0; j < nDates[i]; ++j) {
        listForward.add(new SimplyCompoundedForwardSensitivity(coupon.getFixingPeriodStartTime()[i][j], coupon.getFixingPeriodEndTime()[i][j], coupon.getFixingPeriodAccrualFactor()[i][j],
            forwardBar[i][j]));
      }
    }
    mapFwd.put(multicurve.getName(coupon.getIndex()), listForward);
    return MultipleCurrencyMulticurveSensitivity.of(coupon.getCurrency(), MulticurveSensitivity.of(mapDsc, mapFwd));
  }

}
