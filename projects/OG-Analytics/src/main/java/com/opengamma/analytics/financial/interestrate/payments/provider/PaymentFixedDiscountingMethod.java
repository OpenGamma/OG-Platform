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

import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.util.amount.StringAmount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Methods related to fixed payments.
 */
public final class PaymentFixedDiscountingMethod {

  /**
   * The method unique instance.
   */
  private static final PaymentFixedDiscountingMethod INSTANCE = new PaymentFixedDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static PaymentFixedDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private PaymentFixedDiscountingMethod() {
  }

  /**
   * Compute the the present value of a fixed payment by discounting to a parallel curve movement.
   * @param payment The payment.
   * @param multicurves The multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final PaymentFixed payment, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(payment, "Payment");
    ArgumentChecker.notNull(multicurves, "Multi-curves");
    final double pv = payment.getAmount() * multicurves.getDiscountFactor(payment.getCurrency(), payment.getPaymentTime());
    return MultipleCurrencyAmount.of(payment.getCurrency(), pv);
  }

  /**
   * Computes the present value curve sensitivity of a fixed payment by discounting.
   * @param payment The fixed payment.
   * @param multicurves The multi-curve provider.
   * @return The sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final PaymentFixed payment, final MulticurveProviderInterface multicurves) {
    final double time = payment.getPaymentTime();
    final DoublesPair s = DoublesPair.of(time, -time * payment.getAmount() * multicurves.getDiscountFactor(payment.getCurrency(), time));
    final List<DoublesPair> list = new ArrayList<>();
    list.add(s);
    final Map<String, List<DoublesPair>> result = new HashMap<>();
    result.put(multicurves.getName(payment.getCurrency()), list);
    return MultipleCurrencyMulticurveSensitivity.of(payment.getCurrency(), MulticurveSensitivity.ofYieldDiscounting(result));
  }

  /**
   * Compute the the present value curve sensitivity of a fixed payment by discounting to a parallel curve movement.
   * @param payment The payment.
   * @param multicurves The multi-curves provider.
   * @return The sensitivity.
   * TODO: Should this be multiple-currency?
   */
  public StringAmount presentValueParallelCurveSensitivity(final PaymentFixed payment, final MulticurveProviderInterface multicurves) {
    final double time = payment.getPaymentTime();
    final double sensitivity = -time * payment.getAmount() * multicurves.getDiscountFactor(payment.getCurrency(), time);
    return StringAmount.from(multicurves.getName(payment.getCurrency()), sensitivity);
  }

}
