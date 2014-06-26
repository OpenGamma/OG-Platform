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

import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborFlatCompoundingSpread;
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
public final class CouponIborFlatCompoundingSpreadDiscountingMethod {

  /**
   * The method unique instance.
   */
  private static final CouponIborFlatCompoundingSpreadDiscountingMethod INSTANCE = new CouponIborFlatCompoundingSpreadDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponIborFlatCompoundingSpreadDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponIborFlatCompoundingSpreadDiscountingMethod() {
  }

  /**
   * Compute the present value of a Ibor average coupon by discounting.
   * @param coupon The coupon.
   * @param multicurves The multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CouponIborFlatCompoundingSpread coupon, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurves, "Multi-curves provider");

    final int nPeriods = coupon.getFixingTime().length;
    final int nDatesIni = coupon.getFixingTime()[0].length;
    final int nDates = coupon.getFixingTime()[1].length;
    double payoff = coupon.getAmountAccrued();
    double fwdIni = coupon.getRateFixed();
    for (int j = 0; j < nDatesIni; ++j) {
      fwdIni += coupon.getWeight()[0][j] *
          multicurves.getSimplyCompoundForwardRate(coupon.getIndex(), coupon.getFixingPeriodStartTime()[0][j], coupon.getFixingPeriodEndTime()[0][j], coupon.getFixingPeriodAccrualFactor()[0][j]);
    }
    payoff += (coupon.getSpread() + fwdIni + payoff * fwdIni) * coupon.getPaymentAccrualFactors()[0];
    for (int i = 1; i < nPeriods; ++i) {
      double forward = 0.;
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
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final CouponIborFlatCompoundingSpread coupon, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurve, "Curves");
    final int nPeriods = coupon.getFixingTime().length;
    final int nDatesIni = coupon.getFixingTime()[0].length;
    final int nDates = coupon.getFixingTime()[1].length;

    final double[] forwards = new double[nPeriods];
    final double[] cpaSum = new double[nPeriods];
    double payoff = coupon.getAmountAccrued();
    forwards[0] = coupon.getRateFixed();
    for (int j = 0; j < nDatesIni; ++j) {
      forwards[0] += coupon.getWeight()[0][j] *
          multicurve.getSimplyCompoundForwardRate(coupon.getIndex(), coupon.getFixingPeriodStartTime()[0][j], coupon.getFixingPeriodEndTime()[0][j], coupon.getFixingPeriodAccrualFactor()[0][j]);
    }
    payoff += (coupon.getSpread() + forwards[0] + payoff * forwards[0]) * coupon.getPaymentAccrualFactors()[0];
    cpaSum[0] = payoff;
    for (int i = 1; i < nPeriods; ++i) {
      for (int j = 0; j < nDates; ++j) {
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
    final double[][][] cpaBar = new double[nPeriods][nPeriods][nDates];
    for (int j = 0; j < nDatesIni; ++j) {
      cpaBar[0][0][j + nDates - nDatesIni] += coupon.getWeight()[0][j] * (1.0 + coupon.getAmountAccrued()) * coupon.getPaymentAccrualFactors()[0];
    }
    for (int i = 1; i < nPeriods; ++i) {
      for (int k = 0; k < nDates; ++k) {
        cpaBar[i][i][k] += coupon.getWeight()[i][k] * (1.0 + cpaSum[i - 1]) * coupon.getPaymentAccrualFactors()[i];
      }
      for (int l = 0; l < i; ++l) {
        for (int j = 0; j < nPeriods; ++j) {
          for (int k = 0; k < nDates; ++k) {
            cpaBar[i][j][k] += cpaBar[l][j][k] * forwards[i] * coupon.getPaymentAccrualFactors()[i];
          }
        }
      }
    }

    final double[][] forwardBars = new double[nPeriods][nDates];
    for (int i = 0; i < nPeriods; ++i) {
      for (int j = 0; j < nPeriods; ++j) {
        for (int k = 0; k < nDates; ++k) {
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
    for (int j = 0; j < nDatesIni; ++j) {
      listForward.add(new SimplyCompoundedForwardSensitivity(coupon.getFixingPeriodStartTime()[0][j], coupon.getFixingPeriodEndTime()[0][j], coupon.getFixingPeriodAccrualFactor()[0][j],
          forwardBars[0][j + nDates - nDatesIni]));
    }
    for (int i = 1; i < nPeriods; ++i) {
      for (int j = 0; j < nDates; ++j) {
        listForward.add(new SimplyCompoundedForwardSensitivity(coupon.getFixingPeriodStartTime()[i][j], coupon.getFixingPeriodEndTime()[i][j], coupon.getFixingPeriodAccrualFactor()[i][j],
            forwardBars[i][j]));
      }
    }
    mapFwd.put(multicurve.getName(coupon.getIndex()), listForward);
    return MultipleCurrencyMulticurveSensitivity.of(coupon.getCurrency(), MulticurveSensitivity.of(mapDsc, mapFwd));
  }
}
