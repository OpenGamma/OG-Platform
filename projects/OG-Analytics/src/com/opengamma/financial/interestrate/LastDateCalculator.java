/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.payments.ContinuouslyMonitoredAverageRatePayment;
import com.opengamma.financial.interestrate.payments.FixedPayment;
import com.opengamma.financial.interestrate.payments.ForwardLiborPayment;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swap.definition.FloatingRateNote;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;

/**
 * 
 */
public class LastDateCalculator implements InterestRateDerivativeVisitor<Object, Double> {

  public Double getValue(InterestRateDerivative ird) {
    return getValue(ird, null);
  }

  @Override
  public Double getValue(InterestRateDerivative ird, Object data) {
    Validate.notNull(ird, "ird");
    return ird.accept(this, null);
  }

  @Override
  public Double visitTenorSwap(TenorSwap swap, Object data) {
    return visitSwap(swap, data);
  }

  @Override
  public Double visitBond(Bond bond, Object data) {
    return bond.getPrinciplePayment().getPaymentTime();
  }

  @Override
  public Double visitCash(Cash cash, Object data) {
    return cash.getMaturity();
  }

  @Override
  public Double visitFixedCouponSwap(FixedCouponSwap<?> swap, Object data) {
    return visitSwap(swap, data);
  }

  @Override
  public Double visitFloatingRateNote(FloatingRateNote frn, Object data) {
    return visitSwap(frn, data);
  }

  @Override
  public Double visitForwardRateAgreement(ForwardRateAgreement fra, Object data) {
    return fra.getMaturity();
  }

  @Override
  public Double visitInterestRateFuture(InterestRateFuture future, Object data) {
    return future.getMaturity();
  }

  @Override
  public Double visitFixedPayment(FixedPayment payment, Object data) {
    return payment.getPaymentTime();
  }

  @Override
  public Double visitForwardLiborPayment(ForwardLiborPayment payment, Object data) {
    return Math.max(payment.getLiborMaturityTime(), payment.getPaymentTime());
  }

  @Override
  public Double visitGenericAnnuity(GenericAnnuity<? extends Payment> annuity, Object data) {
    return getValue(annuity.getNthPayment(annuity.getNumberOfPayments() - 1), data);
  }

  @Override
  public Double visitSwap(Swap<?, ?> swap, Object data) {
    double a = getValue(swap.getPayLeg(), data);
    double b = getValue(swap.getReceiveLeg(), data);
    return Math.max(a, b);
  }

  @Override
  public Double visitContinuouslyMonitoredAverageRatePayment(ContinuouslyMonitoredAverageRatePayment payment, Object data) {
    return payment.getPaymentTime();
  }

  // @Override
  // public Double visitConstantCouponAnnuity(FixedCouponAnnuity annuity, Object data) {
  // return annuity.getPaymentTimes()[annuity.getNumberOfPayments() - 1];
  // }
  //
  // @Override
  // public Double visitFixedAnnuity(FixedAnnuity annuity, Object data) {
  // return annuity.getPaymentTimes()[annuity.getNumberOfPayments() - 1];
  // }
  //
  // @Override
  // public Double visitVariableAnnuity(ForwardLiborAnnuity annuity, Object data) {
  // int nPay = annuity.getNumberOfPayments();
  // return Math.max(annuity.getPaymentTimes()[nPay - 1], annuity.getIndexMaturityTimes()[nPay - 1]);
  // }
}
