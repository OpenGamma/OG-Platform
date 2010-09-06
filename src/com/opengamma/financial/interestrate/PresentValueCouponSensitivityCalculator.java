/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.annuity.definition.ConstantCouponAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.FixedAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.VariableAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.swap.definition.BasisSwap;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.FloatingRateNote;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * 
 */
public class PresentValueCouponSensitivityCalculator implements InterestRateDerivativeVisitor<Double> {

  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final PresentValueCouponSensitivityCalculator s_instance = new PresentValueCouponSensitivityCalculator();

  public static PresentValueCouponSensitivityCalculator getInstance() {
    return s_instance;
  }

  private PresentValueCouponSensitivityCalculator() {
  }

  @Override
  public Double getValue(InterestRateDerivative ird, YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(ird);
    return ird.accept(this, curves);
  }

  @Override
  public Double visitBond(Bond bond, YieldCurveBundle curves) {
    final double pvann = PVC.getValue(bond.getFixedAnnuity().withUnitCoupons(), curves);
    return pvann;
  }

  @Override
  public Double visitCash(Cash cash, YieldCurveBundle curves) {
    final YieldAndDiscountCurve curve = curves.getCurve(cash.getYieldCurveName());
    return curve.getDiscountFactor(cash.getPaymentTime()) * cash.getYearFraction();
  }

  @Override
  public Double visitFixedFloatSwap(FixedFloatSwap swap, YieldCurveBundle curves) {
    final double pvFixed = PVC.getValue(swap.getFixedLeg().withUnitCoupons(), curves);
    return -pvFixed;
  }

  @Override
  public Double visitSwap(Swap swap, YieldCurveBundle curves) {
    final double pvSpread = PVC.getValue(swap.getReceiveLeg().withUnitCoupons(), curves);
    return pvSpread;
  }

  @Override
  public Double visitFloatingRateNote(FloatingRateNote frn, YieldCurveBundle curves) {
    return visitSwap(frn, curves);
  }

  @Override
  public Double visitBasisSwap(BasisSwap swap, YieldCurveBundle curves) {
    return visitSwap(swap, curves);
  }

  @Override
  public Double visitForwardRateAgreement(ForwardRateAgreement fra, YieldCurveBundle curves) {
    final YieldAndDiscountCurve fundingCurve = curves.getCurve(fra.getFundingCurveName());
    final YieldAndDiscountCurve liborCurve = curves.getCurve(fra.getLiborCurveName());
    final double fwdAlpha = fra.getForwardYearFraction();
    final double discountAlpha = fra.getDiscountingYearFraction();
    final double forward = (liborCurve.getDiscountFactor(fra.getFixingDate())
        / liborCurve.getDiscountFactor(fra.getMaturity()) - 1.0)
        / fwdAlpha;
    final double res = -fundingCurve.getDiscountFactor(fra.getSettlementDate()) * fwdAlpha
        / (1 + forward * discountAlpha);
    return res;
  }

  @Override
  public Double visitInterestRateFuture(InterestRateFuture future, YieldCurveBundle curves) {
    return future.getValueYearFraction();
  }

  @Override
  public Double visitConstantCouponAnnuity(ConstantCouponAnnuity annuity, YieldCurveBundle curves) {
    return visitFixedAnnuity(annuity, curves);
  }

  @Override
  public Double visitFixedAnnuity(FixedAnnuity annuity, YieldCurveBundle curves) {
    final double res = PVC.getValue(annuity.withUnitCoupons(), curves);
    return res;
  }

  @Override
  public Double visitVariableAnnuity(VariableAnnuity annuity, YieldCurveBundle curves) {
    double res = PVC.getValue(annuity.withUnitCoupons(), curves);
    return res;
  }

}
