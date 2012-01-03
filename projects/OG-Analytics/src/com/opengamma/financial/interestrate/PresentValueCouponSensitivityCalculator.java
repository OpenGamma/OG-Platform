/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.financial.interestrate.cash.derivative.Cash;
import com.opengamma.financial.interestrate.fra.ForwardRateAgreement;
import com.opengamma.financial.interestrate.fra.method.ForwardRateAgreementDiscountingMethod;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * Calculates the change in present value (PV) when an instruments fixed payments changes (for bonds this is the coupon rate, for swaps is is then rate on the fixed leg etc) dPV/dC
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

  @Override
  public Double visit(final InstrumentDerivative ird, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(ird);
    return ird.accept(this, curves);
  }

  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity bond, final YieldCurveBundle curves) {
    final GenericAnnuity<CouponFixed> coupons = bond.getCoupon();
    final int n = coupons.getNumberOfPayments();
    final CouponFixed[] unitCoupons = new CouponFixed[n];
    for (int i = 0; i < n; i++) {
      unitCoupons[i] = coupons.getNthPayment(i).withUnitCoupon();
    }
    final GenericAnnuity<CouponFixed> unitCouponAnnuity = new GenericAnnuity<CouponFixed>(unitCoupons);
    return PVC.visit(unitCouponAnnuity, curves);    
  }
  
  @Override
  public Double visitCash(final Cash cash, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve curve = curves.getCurve(cash.getYieldCurveName());
    return curve.getDiscountFactor(cash.getEndTime()) * cash.getAccrualFactor();
  }

  @Override
  public Double visitForwardRateAgreement(final ForwardRateAgreement fra, final YieldCurveBundle curves) {
    final ForwardRateAgreementDiscountingMethod method = ForwardRateAgreementDiscountingMethod.getInstance();
    return method.presentValueCouponSensitivity(fra, curves);
  }

  @Override
  public Double visitInterestRateFuture(final InterestRateFuture future, final YieldCurveBundle curves) {
    return future.getPaymentAccrualFactor();
  }

  @Override
  public Double visitFixedCouponSwap(final FixedCouponSwap<?> swap, final YieldCurveBundle curves) {
    return PVC.visit(REPLACE_RATE.visitFixedCouponAnnuity(swap.getFixedLeg(), 1.0), curves);
  }

  /**
   * The assumption is that spread is received (i.e. the spread, if any, is on the received leg only)
   * If the spread is paid (i.e. on the pay leg), swap the legs around and take the negative of the returned value.
   * @param swap 
   * @param curves 
   * @return  The spread on the receive leg of a basis swap 
   */
  @Override
  public Double visitTenorSwap(final TenorSwap<? extends Payment> swap, final YieldCurveBundle curves) {
    return PVC.visit(((AnnuityCouponIbor) swap.getSecondLeg()).withUnitCoupons(), curves);
  }

  @Override
  public Double visitFixedPayment(final PaymentFixed payment, final YieldCurveBundle data) {
    return 0.0;
  }

  @Override
  public Double visitGenericAnnuity(final GenericAnnuity<? extends Payment> annuity, final YieldCurveBundle data) {
    double sum = 0;
    for (final Payment p : annuity.getPayments()) {
      sum += visit(p, data);
    }
    return sum;
  }

  @Override
  public Double visitFixedFloatSwap(final FixedFloatSwap swap, final YieldCurveBundle data) {
    return visitFixedCouponSwap(swap, data);
  }
}
