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

import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborFxReset;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimplyCompoundedForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to compute present value and present value sensitivity for Ibor coupon with FX reset.
 */
public final class CouponIborFxResetDiscountingMethod {

  /**
   * The method unique instance.
   */
  private static final CouponIborFxResetDiscountingMethod INSTANCE = new CouponIborFxResetDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponIborFxResetDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponIborFxResetDiscountingMethod() {
  }


  /**
   * Compute the present value of a Ibor coupon with spread using a specific forward rate provider by discounting.
   * @param coupon The coupon with FX reset.
   * @param multicurves The multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CouponIborFxReset coupon,
      final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(coupon, "coupon");
    ArgumentChecker.notNull(multicurves, "multicurves");
    double forward = multicurves.getSimplyCompoundForwardRate(coupon.getIndex(), coupon.getFixingPeriodStartTime(),
        coupon.getFixingPeriodEndTime(), coupon.getFixingAccrualFactor());
    double tp = coupon.getPaymentTime();
    double t0 = coupon.getFxDeliveryTime();
    Currency ccyPayment = coupon.getCurrency();
    Currency ccyReference = coupon.getReferenceCurrency();
    double dfCcyPaymentAtPayment = multicurves.getDiscountFactor(ccyPayment, tp);
    double dfCcyReferenceAtDelivery = multicurves.getDiscountFactor(ccyReference, t0);
    double dfCcyPaymentAtDelivery = multicurves.getDiscountFactor(ccyPayment, t0);
    double fxRate = multicurves.getFxRate(ccyReference, ccyPayment);
    double notional = fxRate * coupon.getNotional();
    double value = (forward + coupon.getSpread()) * coupon.getPaymentYearFraction() * notional * dfCcyPaymentAtPayment *
        dfCcyReferenceAtDelivery / dfCcyPaymentAtDelivery;
    return MultipleCurrencyAmount.of(ccyPayment, value);
  }

  /**
   * Compute the present value sensitivity to rates of a Ibor coupon by discounting.
   * @param coupon The coupon with FX reset.
   * @param multicurves The multi-curve provider.
   * @return The present value sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final CouponIborFxReset coupon,
      final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurves, "Multi-curves");

    double forward = multicurves.getSimplyCompoundForwardRate(coupon.getIndex(), coupon.getFixingPeriodStartTime(),
        coupon.getFixingPeriodEndTime(), coupon.getFixingAccrualFactor());
    Currency ccyPayment = coupon.getCurrency();
    Currency ccyReference = coupon.getReferenceCurrency();
    double tp = coupon.getPaymentTime();
    double t0 = coupon.getFxDeliveryTime();
    double todayFxRate = multicurves.getFxRate(ccyReference, ccyPayment);
    double notional = todayFxRate * coupon.getNotional();
    double amount = notional * (forward + coupon.getSpread()) * coupon.getPaymentYearFraction();
    double dfCcyPaymentAtPayment = multicurves.getDiscountFactor(ccyPayment, tp);
    double dfCcyReferenceAtDelivery = multicurves.getDiscountFactor(ccyReference, t0);
    double dfCcyPaymentAtDelivery = multicurves.getDiscountFactor(ccyPayment, t0);
    double forwardBar = coupon.getPaymentYearFraction() * notional * dfCcyPaymentAtPayment *
        dfCcyReferenceAtDelivery / dfCcyPaymentAtDelivery;
    // Backward sweep.
    double pvBar = 1.0;
    double dfCcyPaymentAtDeliveryBar = -amount *
        dfCcyPaymentAtPayment * dfCcyReferenceAtDelivery / (dfCcyPaymentAtDelivery * dfCcyPaymentAtDelivery) * pvBar;
    double dfCcyReferenceAtDeliveryBar = amount * dfCcyPaymentAtPayment / dfCcyPaymentAtDelivery * pvBar;
    double dfCcyPaymentAtPaymentBar = amount * dfCcyReferenceAtDelivery / dfCcyPaymentAtDelivery * pvBar;
    MultipleCurrencyMulticurveSensitivity result = new MultipleCurrencyMulticurveSensitivity();
    final Map<String, List<DoublesPair>> mapDscCcyPayment = new HashMap<>();
    final List<DoublesPair> listDscCcyPayment = new ArrayList<>();
    listDscCcyPayment.add(DoublesPair.of(t0, -t0 * dfCcyPaymentAtDelivery * dfCcyPaymentAtDeliveryBar));
    listDscCcyPayment.add(DoublesPair.of(tp, -tp * dfCcyPaymentAtPayment * dfCcyPaymentAtPaymentBar));
    mapDscCcyPayment.put(multicurves.getName(ccyPayment), listDscCcyPayment);
    result = result.plus(ccyPayment, MulticurveSensitivity.ofYieldDiscounting(mapDscCcyPayment));
    final Map<String, List<DoublesPair>> mapDscCcyReference = new HashMap<>();
    final List<DoublesPair> listDscCcyReference = new ArrayList<>();
    listDscCcyReference.add(DoublesPair.of(t0, -t0 * dfCcyReferenceAtDelivery * dfCcyReferenceAtDeliveryBar));
    mapDscCcyReference.put(multicurves.getName(ccyReference), listDscCcyReference);
    result = result.plus(ccyReference, MulticurveSensitivity.ofYieldDiscounting(mapDscCcyReference));
    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<>();
    final List<ForwardSensitivity> listForward = new ArrayList<>();
    listForward.add(new SimplyCompoundedForwardSensitivity(coupon.getFixingPeriodStartTime(), coupon.getFixingPeriodEndTime(), coupon.getFixingAccrualFactor(), forwardBar));
    mapFwd.put(multicurves.getName(coupon.getIndex()), listForward);
    result = result.plus(coupon.getIndex().getCurrency(), MulticurveSensitivity.ofForward(mapFwd));
    return result;
  }

  /**
   * Compute the currency exposure of a Fixed coupon with FX reset notional by discounting. 
   * See documentation for the hypothesis.
   * @param coupon The coupon.
   * @param multicurves The multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount currencyExposure(final CouponIborFxReset coupon,
      final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(coupon, "coupon");
    ArgumentChecker.notNull(multicurves, "multicurves");
    double forward = multicurves.getSimplyCompoundForwardRate(coupon.getIndex(), coupon.getFixingPeriodStartTime(),
        coupon.getFixingPeriodEndTime(), coupon.getFixingAccrualFactor());
    double tp = coupon.getPaymentTime();
    double t0 = coupon.getFxDeliveryTime();
    Currency ccyPayment = coupon.getCurrency();
    Currency ccyReference = coupon.getReferenceCurrency();
    double dfCcyPaymentAtPayment = multicurves.getDiscountFactor(ccyPayment, tp);
    double dfCcyReferenceAtDelivery = multicurves.getDiscountFactor(ccyReference, t0);
    double dfCcyPaymentAtDelivery = multicurves.getDiscountFactor(ccyPayment, t0);
    double notionalRef = coupon.getNotional();
    double pvRef = (forward + coupon.getSpread()) * coupon.getPaymentYearFraction() * notionalRef *
        dfCcyPaymentAtPayment * dfCcyReferenceAtDelivery / dfCcyPaymentAtDelivery;
    return MultipleCurrencyAmount.of(ccyReference, pvRef);
  }
}
