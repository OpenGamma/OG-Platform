/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.util.money.Currency;

//TODO: rewrite the FRN from scratch.
/**
 * 
 */
public class FloatingRateNote extends Swap<PaymentFixed, CouponIbor> {

  public FloatingRateNote(final GenericAnnuity<CouponIbor> forwardLiborAnnuity, final PaymentFixed initalPayment, final PaymentFixed finalPayment) {
    super(setUpFixedLeg(forwardLiborAnnuity, initalPayment, finalPayment), forwardLiborAnnuity);
  }

  private static GenericAnnuity<PaymentFixed> setUpFixedLeg(final GenericAnnuity<CouponIbor> annuity, final PaymentFixed initalPayment, final PaymentFixed finalPayment) {

    final String curveName = annuity.getDiscountCurve();
    //consistency checks on the inputs
    Validate.isTrue(initalPayment.getCurrency() == finalPayment.getCurrency(), "initial and final payments in different currencies");
    Validate.isTrue(initalPayment.getCurrency() == annuity.getCurrency(), "flaoting and fixed payments in different currencies");
    Validate.isTrue(initalPayment.getPaymentTime() < finalPayment.getPaymentTime(), "initial payment after final payment");
    Validate.isTrue(initalPayment.getPaymentTime() <= annuity.getNthPayment(0).getPaymentTime(), "initial payment after first floating payments");
    Validate.isTrue(curveName == initalPayment.getFundingCurveName(), "inital payment discounted off different curve to floating payments");
    Validate.isTrue(curveName == finalPayment.getFundingCurveName(), "final payment discounted off different curve to floating payments");
    Validate.isTrue(initalPayment.getAmount() * finalPayment.getAmount() < 0, "inital payment should be oposite sign to final");
    Validate.isTrue((annuity.isPayer() && initalPayment.getAmount() > 0.0) || (!annuity.isPayer() && initalPayment.getAmount() < 0.0), "initial payment should be oposite sign to Ibor coupons");

    final PaymentFixed[] fixedPayments = new PaymentFixed[2];

    fixedPayments[0] = initalPayment;
    fixedPayments[1] = finalPayment;

    return new GenericAnnuity<PaymentFixed>(fixedPayments);
  }

  public AnnuityCouponIbor getFloatingLeg() {
    return (AnnuityCouponIbor) getSecondLeg();
  }

  /**
   * Return the currency of the annuity. 
   * @return The currency
   */
  public Currency getCurrency() {
    return getFirstLeg().getCurrency();
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitFloatingRateNote(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitFloatingRateNote(this);
  }

}
