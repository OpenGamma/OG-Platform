/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONCompounded;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * @deprecated Use {@link com.opengamma.analytics.financial.interestrate.payments.provider.CouponONCompoundedDiscountingMethod}
 */
@Deprecated
public final class CouponONCompoundedDiscountingMethod implements PricingMethod {

  /**
   * The method unique instance.
   */
  private static final CouponONCompoundedDiscountingMethod INSTANCE = new CouponONCompoundedDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponONCompoundedDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponONCompoundedDiscountingMethod() {
  }

  /**
   * Computes the present value.
   * @param coupon The coupon.
   * @param curves The curves.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final CouponONCompounded coupon, final YieldCurveBundle curves) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(coupon.getForwardCurveName());
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(coupon.getFundingCurveName());
    double ratio = 1.0;
    double forwardRatei;
    final List<Double> tmpRateArr = new ArrayList<>();
    for (int i = 0; i < coupon.getFixingPeriodAccrualFactors().length; i++) {
      forwardRatei = (forwardCurve.getDiscountFactor(coupon.getFixingPeriodStartTimes()[i]) / forwardCurve.getDiscountFactor(coupon.getFixingPeriodEndTimes()[i]) - 1.0d) /
//    coupon.getFixingPeriodAccrualFactors()[i];
          coupon.getFixingPeriodAccrualFactorsActAct()[i];
//      forwardRatei = Math.pow(((Math.exp(coupon.getFixingPeriodAccrualFactorsActAct()[i] * forwardRatei) - 1.0) * 1.45 + 1.0), 1.0 / coupon.getFixingPeriodAccrualFactors()[i]) - 1.0;
      forwardRatei = Math.pow(((Math.exp(1.0 / 365.0 * forwardRatei) - 1.0) * 365.0 / 252.0 + 1.0), 252.0) - 1.0;
      tmpRateArr.add(forwardRatei);
      ratio *= Math.pow(1 + forwardRatei, coupon.getFixingPeriodAccrualFactors()[i]);
    }
    final double df = discountingCurve.getDiscountFactor(coupon.getPaymentTime());
    final double pv = df * coupon.getNotionalAccrued() * ratio;
    return CurrencyAmount.of(coupon.getCurrency(), pv);
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof CouponONCompounded, "Coupon ON compounded");
    return presentValue(instrument, curves);
  }

  /**
   * Compute the present value sensitivity to rates of a OIS coupon by discounting.
   * @param coupon The coupon.
   * @param curves The yield curves. Should contain the discounting and forward curves associated.
   * @return The present value curve sensitivities.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final CouponONCompounded coupon, final YieldCurveBundle curves) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(coupon.getForwardCurveName());
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(coupon.getFundingCurveName());
    final double df = discountingCurve.getDiscountFactor(coupon.getPaymentTime());
    double ratio = 1.0;
    final double[] discountFactorStart = new double[coupon.getFixingPeriodAccrualFactors().length];
    final double[] discountFactorEnd = new double[coupon.getFixingPeriodAccrualFactors().length];
    final double[] forwardRate = new double[coupon.getFixingPeriodAccrualFactors().length];
    for (int i = 0; i < coupon.getFixingPeriodAccrualFactors().length; i++) {
      discountFactorStart[i] = forwardCurve.getDiscountFactor(coupon.getFixingPeriodStartTimes()[i]);
      discountFactorEnd[i] = forwardCurve.getDiscountFactor(coupon.getFixingPeriodEndTimes()[i]);

      //      forwardRate[i] = (discountFactorEnd[i] / discountFactorStart[i] - 1) / coupon.getFixingPeriodAccrualFactorsActAct()[i];
      forwardRate[i] = (discountFactorStart[i] / discountFactorEnd[i] - 1) / coupon.getFixingPeriodAccrualFactorsActAct()[i];
      ratio *= Math.pow(1 + forwardRate[i], coupon.getFixingPeriodAccrualFactors()[i]);
    }
    // Backward sweep
    final double pvBar = 1.0;
    final double ratioBar = coupon.getNotionalAccrued() * df * pvBar;
    final double[] discountFactorStartBar = new double[coupon.getFixingPeriodAccrualFactors().length];
    final double[] discountFactorEndBar = new double[coupon.getFixingPeriodAccrualFactors().length];
    final double[] forwardBar = new double[coupon.getFixingPeriodAccrualFactors().length];
    for (int i = 0; i < coupon.getFixingPeriodAccrualFactors().length; i++) {
      //      forwardBar[i] = ratioBar * ratioBar * coupon.getFixingPeriodAccrualFactors()[i] /
      //          (1 + forwardRate[i]);
      forwardBar[i] = ratio * ratioBar * coupon.getFixingPeriodAccrualFactors()[i] / (1 + forwardRate[i]);
      //      discountFactorStartBar[i] = forwardBar[i] * discountFactorEnd[i] / (discountFactorStart[i] * discountFactorStart[i]) / coupon.getFixingPeriodAccrualFactorsActAct()[i];
      //      discountFactorEndBar[i] = -forwardBar[i] / (discountFactorStart[i] * coupon.getFixingPeriodAccrualFactorsActAct()[i]);      
      discountFactorStartBar[i] = forwardBar[i] / (discountFactorEnd[i] * coupon.getFixingPeriodAccrualFactorsActAct()[i]);
      discountFactorEndBar[i] = -forwardBar[i] * discountFactorStart[i] / (discountFactorEnd[i] * discountFactorEnd[i]) / coupon.getFixingPeriodAccrualFactorsActAct()[i];
    }
    final double dfBar = coupon.getNotionalAccrued() * ratio * pvBar;
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(new DoublesPair(coupon.getPaymentTime(), -coupon.getPaymentTime() * df * dfBar));
    mapDsc.put(coupon.getFundingCurveName(), listDiscounting);
    InterestRateCurveSensitivity result = new InterestRateCurveSensitivity(mapDsc);
    final Map<String, List<DoublesPair>> mapFwd = new HashMap<>();
    final List<DoublesPair> listForward = new ArrayList<>();
    for (int i = 0; i < coupon.getFixingPeriodAccrualFactors().length; i++) {
      listForward.add(new DoublesPair(coupon.getFixingPeriodStartTimes()[i], -coupon.getFixingPeriodStartTimes()[i] * discountFactorStart[i] * discountFactorStartBar[i]));
      listForward.add(new DoublesPair(coupon.getFixingPeriodEndTimes()[i], -coupon.getFixingPeriodEndTimes()[i] * discountFactorEnd[i] * discountFactorEndBar[i]));
    }
    mapFwd.put(coupon.getForwardCurveName(), listForward);
    result = result.plus(new InterestRateCurveSensitivity(mapFwd));
    return result;
  }

}
