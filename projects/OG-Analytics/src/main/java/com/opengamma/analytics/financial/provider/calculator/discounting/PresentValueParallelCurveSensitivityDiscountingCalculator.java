/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.discounting;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.payments.provider.PaymentFixedDiscountingMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.util.amount.StringAmount;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculates the present value sensitivity to parallel curve movements.
 */
public final class PresentValueParallelCurveSensitivityDiscountingCalculator extends InstrumentDerivativeVisitorAdapter<MulticurveProviderInterface, StringAmount> {
  // TODO: This calculator is similar (equivalent?) to the PV01Calculator. Should they be merged?

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueParallelCurveSensitivityDiscountingCalculator INSTANCE = new PresentValueParallelCurveSensitivityDiscountingCalculator();

  /**
   * Returns the instance of the calculator.
   * @return The instance.
   */
  public static PresentValueParallelCurveSensitivityDiscountingCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private PresentValueParallelCurveSensitivityDiscountingCalculator() {
  }

  /**
   * Methods.
   */
  private static final PaymentFixedDiscountingMethod METHOD_PAYMENTFIXED = PaymentFixedDiscountingMethod.getInstance();

  // -----     Payment/Coupon     ------

  @Override
  public StringAmount visitFixedPayment(final PaymentFixed payment, final MulticurveProviderInterface multicurves) {
    return METHOD_PAYMENTFIXED.presentValueParallelCurveSensitivity(payment, multicurves);
  }

  @Override
  public StringAmount visitCouponFixed(final CouponFixed payment, final MulticurveProviderInterface multicurves) {
    return visitFixedPayment(payment.toPaymentFixed(), multicurves);
  }

  // -----     Annuity     ------

  @Override
  public StringAmount visitGenericAnnuity(final Annuity<? extends Payment> annuity, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(annuity, "Annuity");
    ArgumentChecker.notNull(multicurves, "multicurve");
    StringAmount pvpcs = new StringAmount();
    for (final Payment p : annuity.getPayments()) {
      pvpcs = StringAmount.plus(pvpcs, p.accept(this, multicurves));
    }
    return pvpcs;
  }

  @Override
  public StringAmount visitFixedCouponAnnuity(final AnnuityCouponFixed annuity, final MulticurveProviderInterface multicurves) {
    return visitGenericAnnuity(annuity, multicurves);
  }

}
