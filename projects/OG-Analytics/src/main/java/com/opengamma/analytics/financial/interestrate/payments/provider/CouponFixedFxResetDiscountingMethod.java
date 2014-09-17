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

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedFxReset;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to compute results for fixed coupon with FX reset notional.
 * See documentation for the hypothesis used to obtain the explicit formula.
 * <P>
 * Reference: Coupons with FX reset. OpenGamma Documentation, xx. September 2014.
 */
public final class CouponFixedFxResetDiscountingMethod {

  /**
   * The method unique instance.
   */
  private static final CouponFixedFxResetDiscountingMethod INSTANCE = new CouponFixedFxResetDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponFixedFxResetDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponFixedFxResetDiscountingMethod() {
  }

  /**
   * Compute the present value of a Fixed coupon with FX reset notional by discounting. 
   * See documentation for the hypothesis.
   * @param coupon The coupon.
   * @param multicurve The multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CouponFixedFxReset coupon, 
      final MulticurveProviderInterface multicurve) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(multicurve, "multicurve");
    Currency ccyPayment = coupon.getCurrency();
    Currency ccyReference = coupon.getReferenceCurrency();
    double tp = coupon.getPaymentTime();
    double t0 = coupon.getFxDeliveryTime();
    double todayFxRate = multicurve.getFxRate(ccyReference, ccyPayment);
    double amount = coupon.paymentAmount(todayFxRate);
    double dfCcyPaymentAtPayment = multicurve.getDiscountFactor(ccyPayment, tp);
    double dfCcyReferenceAtDelivery = multicurve.getDiscountFactor(ccyReference, t0);
    double dfCcyPaymentAtDelivery = multicurve.getDiscountFactor(ccyPayment, t0);
    double pv = amount * dfCcyPaymentAtPayment * dfCcyReferenceAtDelivery / dfCcyPaymentAtDelivery;
    return MultipleCurrencyAmount.of(ccyPayment, pv);
  }
  
  /**
   * Compute the present value curve sensitivity of a Fixed coupon with FX reset notional by discounting. 
   * See documentation for the hypothesis.
   * @param coupon The coupon.
   * @param multicurve The multi-curve provider.
   * @return The present value curve sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final CouponFixedFxReset coupon, 
      final MulticurveProviderInterface multicurve) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(multicurve, "multicurve");
    Currency ccyPayment = coupon.getCurrency();
    Currency ccyReference = coupon.getReferenceCurrency();
    double tp = coupon.getPaymentTime();
    double t0 = coupon.getFxDeliveryTime();
    double todayFxRate = multicurve.getFxRate(ccyReference, ccyPayment);
    double amount = coupon.paymentAmount(todayFxRate);
    double dfCcyPaymentAtPayment = multicurve.getDiscountFactor(ccyPayment, tp);
    double dfCcyReferenceAtDelivery = multicurve.getDiscountFactor(ccyReference, t0);
    double dfCcyPaymentAtDelivery = multicurve.getDiscountFactor(ccyPayment, t0);
//    double pv = amount * dfCcyPaymentAtPayment * dfCcyReferenceAtDelivery / dfCcyPaymentAtDelivery;
    // Backward sweep.
    double pvBar = 1.0;
    double dfCcyPaymentAtDeliveryBar = -amount * 
        dfCcyPaymentAtPayment * dfCcyReferenceAtDelivery / (dfCcyPaymentAtDelivery * dfCcyPaymentAtDelivery) * pvBar;
    double dfCcyReferenceAtDeliveryBar = amount * dfCcyPaymentAtPayment  / dfCcyPaymentAtDelivery * pvBar;
    double dfCcyPaymentAtPaymentBar = amount * dfCcyReferenceAtDelivery / dfCcyPaymentAtDelivery * pvBar;
    MultipleCurrencyMulticurveSensitivity result = new MultipleCurrencyMulticurveSensitivity();
    final Map<String, List<DoublesPair>> mapDscCcyPayment = new HashMap<>();
    final List<DoublesPair> listDscCcyPayment = new ArrayList<>();
    listDscCcyPayment.add(DoublesPair.of(t0, -t0 * dfCcyPaymentAtDelivery * dfCcyPaymentAtDeliveryBar));
    listDscCcyPayment.add(DoublesPair.of(tp, -tp * dfCcyPaymentAtPayment * dfCcyPaymentAtPaymentBar));
    mapDscCcyPayment.put(multicurve.getName(ccyPayment), listDscCcyPayment);
    result = result.plus(ccyPayment, MulticurveSensitivity.ofYieldDiscounting(mapDscCcyPayment));
    final Map<String, List<DoublesPair>> mapDscCcyReference = new HashMap<>();
    final List<DoublesPair> listDscCcyReference = new ArrayList<>();
    listDscCcyReference.add(DoublesPair.of(t0, -t0 * dfCcyReferenceAtDelivery * dfCcyReferenceAtDeliveryBar));
    mapDscCcyReference.put(multicurve.getName(ccyReference), listDscCcyReference);
    result = result.plus(ccyPayment, MulticurveSensitivity.ofYieldDiscounting(mapDscCcyReference));
    return result;
  }


  /**
   * Compute the currency exposure of a Fixed coupon with FX reset notional by discounting. 
   * See documentation for the hypothesis.
   * @param coupon The coupon.
   * @param multicurves The multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount currencyExposure(final CouponFixedFxReset coupon, 
      final MulticurveProviderInterface multicurves) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(multicurves, "multicurve");
    Currency ccyPayment = coupon.getCurrency();
    Currency ccyReference = coupon.getReferenceCurrency();
    double dfCcyPaymentAtPayment = multicurves.getDiscountFactor(ccyPayment, coupon.getPaymentTime());
    double dfCcyReferenceAtDelivery = multicurves.getDiscountFactor(ccyReference, coupon.getFxDeliveryTime());
    double dfCcyPaymentAtDelivery = multicurves.getDiscountFactor(ccyPayment, coupon.getFxDeliveryTime());
    double pv = coupon.getNotional() * coupon.getPaymentYearFraction() * coupon.getRate() * 
        dfCcyPaymentAtPayment * dfCcyReferenceAtDelivery / dfCcyPaymentAtDelivery;
    return MultipleCurrencyAmount.of(ccyReference, pv);
  }

}
