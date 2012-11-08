/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.fra.method.ForwardRateAgreementDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFuture;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * Calculates the change in present value (PV) when an instrument's fixed payments change (for bonds this is the coupon rate, for swaps it is the rate on the fixed leg etc) dPV/dC
 * This can be used to convert between sensitivities of PV to the yield curve and sensitivities of Par rate to the yield curve
 */
public final class PresentValueCouponSensitivityCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, Double> {
  private static final RateReplacingInterestRateDerivativeVisitor REPLACE_RATE = RateReplacingInterestRateDerivativeVisitor.getInstance();
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final PresentValueCouponSensitivityCalculator s_instance = new PresentValueCouponSensitivityCalculator();

  public static PresentValueCouponSensitivityCalculator getInstance() {
    return s_instance;
  }

  private PresentValueCouponSensitivityCalculator() {
  }

  /**
   * Methods used in the calculator.
   */
  private static final ForwardRateAgreementDiscountingMethod METHOD_FRA = ForwardRateAgreementDiscountingMethod.getInstance();

  @Override
  public Double visit(final InstrumentDerivative ird, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(ird);
    return ird.accept(this, curves);
  }

  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity bond, final YieldCurveBundle curves) {
    final Annuity<CouponFixed> coupons = bond.getCoupon();
    final int n = coupons.getNumberOfPayments();
    final CouponFixed[] unitCoupons = new CouponFixed[n];
    for (int i = 0; i < n; i++) {
      unitCoupons[i] = coupons.getNthPayment(i).withUnitCoupon();
    }
    final Annuity<CouponFixed> unitCouponAnnuity = new Annuity<CouponFixed>(unitCoupons);
    return PVC.visit(unitCouponAnnuity, curves);
  }

  @Override
  public Double visitCash(final Cash cash, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve curve = curves.getCurve(cash.getYieldCurveName());
    return curve.getDiscountFactor(cash.getEndTime()) * cash.getAccrualFactor();
  }

  // TODO: Add DepositZero

  @Override
  public Double visitForwardRateAgreement(final ForwardRateAgreement fra, final YieldCurveBundle curves) {
    return METHOD_FRA.presentValueCouponSensitivity(fra, curves);
  }

  @Override
  public Double visitInterestRateFuture(final InterestRateFuture future, final YieldCurveBundle curves) {
    return future.getPaymentAccrualFactor() * future.getQuantity() * future.getNotional();
  }

  @Override
  public Double visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final YieldCurveBundle curves) {
    return PVC.visit(REPLACE_RATE.visitFixedCouponAnnuity(swap.getFixedLeg(), 1.0), curves);
  }

  @Override
  public Double visitFixedPayment(final PaymentFixed payment, final YieldCurveBundle data) {
    return 0.0;
  }

  @Override
  public Double visitGenericAnnuity(final Annuity<? extends Payment> annuity, final YieldCurveBundle data) {
    double sum = 0;
    for (final Payment p : annuity.getPayments()) {
      sum += visit(p, data);
    }
    return sum;
  }

}
