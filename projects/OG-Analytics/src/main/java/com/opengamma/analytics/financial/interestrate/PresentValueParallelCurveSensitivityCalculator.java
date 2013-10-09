/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.payments.method.PaymentFixedDiscountingMethod;
import com.opengamma.analytics.util.amount.StringAmount;

/**
 * Calculates the present value sensitivity to parallel curve movements.
 * @deprecated {@link YieldCurveBundle} is deprecated.
 */
@Deprecated
public final class PresentValueParallelCurveSensitivityCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, StringAmount> {

  /**
   * The unique instance of the SABR sensitivity calculator.
   */
  private static final PresentValueParallelCurveSensitivityCalculator INSTANCE = new PresentValueParallelCurveSensitivityCalculator();

  /**
   * Returns the instance of the calculator.
   * @return The instance.
   */
  public static PresentValueParallelCurveSensitivityCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private PresentValueParallelCurveSensitivityCalculator() {
  }

  /**
   * Methods.
   */
  private static final PaymentFixedDiscountingMethod METHOD_PAYMENTFIXED = PaymentFixedDiscountingMethod.getInstance();

  @Override
  public StringAmount visitFixedPayment(final PaymentFixed payment, final YieldCurveBundle curves) {
    return METHOD_PAYMENTFIXED.presentValueParallelCurveSensitivity(payment, curves);
  }

  @Override
  public StringAmount visitCouponFixed(final CouponFixed payment, final YieldCurveBundle data) {
    return visitFixedPayment(payment.toPaymentFixed(), data);
  }

  @Override
  public StringAmount visitGenericAnnuity(final Annuity<? extends Payment> annuity, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(annuity);
    StringAmount pvpcs = new StringAmount();
    for (final Payment p : annuity.getPayments()) {
      pvpcs = StringAmount.plus(pvpcs, p.accept(this, curves));
    }
    return pvpcs;
  }

  @Override
  public StringAmount visitFixedCouponAnnuity(final AnnuityCouponFixed annuity, final YieldCurveBundle data) {
    return visitGenericAnnuity(annuity, data);
  }

}
