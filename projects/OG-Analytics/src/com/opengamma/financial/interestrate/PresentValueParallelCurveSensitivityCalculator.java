/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.financial.interestrate.payments.method.PaymentFixedDiscountingMethod;
import com.opengamma.util.surface.StringValue;

/**
 * Calculates the present value sensitivity to parallel curve movements.
 */
public final class PresentValueParallelCurveSensitivityCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, StringValue> {
  // TODO: This calculator is similar (equivalent?) to the PV01Calculator. Should they be merged?

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
  public StringValue visit(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    return instrument.accept(this, curves);
  }

  @Override
  public StringValue visitFixedPayment(final PaymentFixed payment, final YieldCurveBundle curves) {
    return METHOD_PAYMENTFIXED.presentValueParallelCurveSensitivity(payment, curves);
  }

  @Override
  public StringValue visitFixedCouponPayment(final CouponFixed payment, final YieldCurveBundle data) {
    return visitFixedPayment(payment.toPaymentFixed(), data);
  }

  @Override
  public StringValue visitGenericAnnuity(final GenericAnnuity<? extends Payment> annuity, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(annuity);
    StringValue pvpcs = new StringValue();
    for (final Payment p : annuity.getPayments()) {
      pvpcs = StringValue.plus(pvpcs, visit(p, curves));
    }
    return pvpcs;
  }

  @Override
  public StringValue visitFixedCouponAnnuity(final AnnuityCouponFixed annuity, final YieldCurveBundle data) {
    return visitGenericAnnuity(annuity, data);
  }

}
