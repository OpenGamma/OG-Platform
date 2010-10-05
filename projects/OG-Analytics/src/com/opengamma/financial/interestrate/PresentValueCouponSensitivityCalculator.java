/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import org.apache.commons.lang.Validate;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.opengamma.financial.interestrate.annuity.definition.ForwardLiborAnnuity;
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
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * 
 */
public final class PresentValueCouponSensitivityCalculator implements InterestRateDerivativeVisitor<YieldCurveBundle, Double> {

  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final PresentValueCouponSensitivityCalculator s_instance = new PresentValueCouponSensitivityCalculator();

  public static PresentValueCouponSensitivityCalculator getInstance() {
    return s_instance;
  }

  private PresentValueCouponSensitivityCalculator() {
  }

  @Override
  public Double getValue(final InterestRateDerivative ird, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(ird);
    return ird.accept(this, curves);
  }

  @Override
  public Double visitBond(final Bond bond, final YieldCurveBundle curves) {
    final double pvann = PVC.getValue(bond.getUnitCouponAnnuity(), curves);
    return pvann;
  }

  @Override
  public Double visitCash(final Cash cash, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve curve = curves.getCurve(cash.getYieldCurveName());
    return curve.getDiscountFactor(cash.getMaturity()) * cash.getYearFraction();
  }

  @Override
  public Double visitForwardRateAgreement(final ForwardRateAgreement fra, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve fundingCurve = curves.getCurve(fra.getFundingCurveName());
    final YieldAndDiscountCurve liborCurve = curves.getCurve(fra.getLiborCurveName());
    final double fwdAlpha = fra.getForwardYearFraction();
    final double discountAlpha = fra.getDiscountingYearFraction();
    final double forward = (liborCurve.getDiscountFactor(fra.getFixingDate()) / liborCurve.getDiscountFactor(fra.getMaturity()) - 1.0) / fwdAlpha;
    final double res = -fundingCurve.getDiscountFactor(fra.getSettlementDate()) * fwdAlpha / (1 + forward * discountAlpha);
    return res;
  }

  @Override
  public Double visitInterestRateFuture(final InterestRateFuture future, final YieldCurveBundle curves) {
    return future.getValueYearFraction();
  }

  @Override
  public Double visitFixedCouponSwap(final FixedCouponSwap<?> swap, final YieldCurveBundle curves) {
    return -PVC.getValue(swap.getFixedLeg().withRate(1.0), curves);
  }

  @Override
  public Double visitSwap(Swap<?, ?> swap, YieldCurveBundle data) {
    throw new NotImplementedException();

  }

  @Override
  public Double visitFloatingRateNote(final FloatingRateNote frn, final YieldCurveBundle curves) {
    return visitSwap(frn, curves);
  }

  /**
   * The assumption is that spread is received (i.e. the spread, if any, is on the received leg only)
   * If the spread is paid (i.e. on the pay leg), swap the legs around and take the negative of the returned value.
   *@param swap 
   * @param curves 
   *@return  The spread on the receive leg of a basis swap 
   */
  @Override
  public Double visitTenorSwap(final TenorSwap swap, final YieldCurveBundle curves) {
    return PVC.getValue(((ForwardLiborAnnuity) swap.getReceiveLeg()).withUnitCoupons(), curves);
  }

  @Override
  public Double visitFixedPayment(FixedPayment payment, YieldCurveBundle data) {
    return 0.0;
  }

  @Override
  public Double visitForwardLiborPayment(ForwardLiborPayment payment, YieldCurveBundle data) {
    throw new NotImplementedException();
  }

  @Override
  public Double visitGenericAnnuity(GenericAnnuity<? extends Payment> annuity, YieldCurveBundle data) {
    double sum = 0;
    for (Payment p : annuity.getPayments()) {
      sum += getValue(p, data);
    }
    return sum;
  }

  @Override
  public Double visitContinuouslyMonitoredAverageRatePayment(ContinuouslyMonitoredAverageRatePayment payment, YieldCurveBundle data) {
    throw new NotImplementedException();
  }

  // @Override
  // public Double visitConstantCouponAnnuity(final FixedCouponAnnuity annuity, final YieldCurveBundle curves) {
  // return visitFixedAnnuity(annuity, curves);
  // }
  //
  // @Override
  // public Double visitFixedAnnuity(final FixedAnnuity annuity, final YieldCurveBundle curves) {
  // final double res = PVC.getValue(annuity.withUnitCoupons(), curves);
  // return res;
  // }
  //
  // @Override
  // public Double visitVariableAnnuity(final ForwardLiborAnnuity annuity, final YieldCurveBundle curves) {
  // final double res = PVC.getValue(annuity.withUnitCoupons(), curves);
  // return res;
  // }
  //  

}
