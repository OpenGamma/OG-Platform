/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.market.description.CurveSensitivityMarket;
import com.opengamma.analytics.financial.interestrate.market.description.MultipleCurrencyCurveSensitivityMarket;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.method.PricingProviderMethod;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to compute present value and present value sensitivity for fixed coupon.
 */
public final class CouponFixedDiscountingProviderMethod implements PricingProviderMethod {

  /**
   * The method unique instance.
   */
  private static final CouponFixedDiscountingProviderMethod INSTANCE = new CouponFixedDiscountingProviderMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponFixedDiscountingProviderMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponFixedDiscountingProviderMethod() {
  }

  /**
   * Compute the present value of a Fixed coupon by discounting.
   * @param coupon The coupon.
   * @param multicurves The multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CouponFixed coupon, final MulticurveProviderInterface multicurves) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(multicurves, "multicurve");
    final double df = multicurves.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    final double value = coupon.getAmount() * df;
    return MultipleCurrencyAmount.of(coupon.getCurrency(), value);
  }

  @Override
  public MultipleCurrencyAmount presentValue(final InstrumentDerivative instrument, final MulticurveProviderInterface multicurve) {
    Validate.isTrue(instrument instanceof CouponFixed, "Coupon Fixed");
    return presentValue((CouponFixed) instrument, multicurve);
  }

  /**
   * Computes the present value curve sensitivity of a fixed coupon by discounting.
   * @param cpn The coupon.
   * @param multicurve The multi-curve provider.
   * @return The sensitivity.
   */
  public MultipleCurrencyCurveSensitivityMarket presentValueCurveSensitivity(CouponFixed cpn, final MulticurveProviderInterface multicurve) {
    final double time = cpn.getPaymentTime();
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<String, List<DoublesPair>>();
    final DoublesPair s = new DoublesPair(time, -time * cpn.getAmount() * multicurve.getDiscountFactor(cpn.getCurrency(), time));
    final List<DoublesPair> list = new ArrayList<DoublesPair>();
    list.add(s);
    mapDsc.put(multicurve.getName(cpn.getCurrency()), list);
    MultipleCurrencyCurveSensitivityMarket result = new MultipleCurrencyCurveSensitivityMarket();
    result = result.plus(cpn.getCurrency(), CurveSensitivityMarket.ofYieldDiscounting(mapDsc));
    return result;
  }

}
