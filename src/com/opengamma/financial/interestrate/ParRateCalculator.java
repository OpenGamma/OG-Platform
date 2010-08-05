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
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * Get the single fixed rate that makes the PV of the instrument zero. For  fixed-float swaps this is the swap rate, for FRAs it is the forward etc. For instruments that 
 * cannot PV to zero, e.g. bonds, a single payment of -1.0 is assumed at zero (i.e. the bond must PV to 1.0)
 */
public final class ParRateCalculator implements InterestRateDerivativeVisitor<Double> {

  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static ParRateCalculator s_instance;

  public static ParRateCalculator getInstance() {
    if (s_instance == null) {
      s_instance = new ParRateCalculator();
    }
    return s_instance;
  }

  private ParRateCalculator() {
  }

  @Override
  public Double getValue(final InterestRateDerivative derivative, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(derivative);
    return derivative.accept(this, curves);
  }

  @Override
  public Double visitCash(final Cash cash, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve curve = curves.getCurve(cash.getYieldCurveName());
    final double ta = cash.getTradeTime();
    final double tb = cash.getPaymentTime();
    final double yearFrac = cash.getYearFraction();
    // TODO need a getForwardRate method on YieldAndDiscountCurve
    if (yearFrac == 0.0) {
      if (ta != tb) {
        throw new IllegalArgumentException("Year fraction is zero, but payment time greater than trade time");
      }
      final double eps = 1e-8;
      final double rate = curve.getInterestRate(ta);
      final double dRate = curve.getInterestRate(ta + eps);
      return rate + ta * (dRate - rate) / eps;
    }
    return (curve.getDiscountFactor(ta) / curve.getDiscountFactor(tb) - 1) / yearFrac;
  }

  @Override
  public Double visitForwardRateAgreement(final ForwardRateAgreement fra, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve curve = curves.getCurve(fra.getLiborCurveName());
    final double ta = fra.getFixingDate();
    final double tb = fra.getMaturity();
    final double yearFrac = fra.getForwardYearFraction();
    Validate.isTrue(yearFrac > 0, "tenor span must be greater than zero");
    final double pa = curve.getDiscountFactor(ta);
    final double pb = curve.getDiscountFactor(tb);
    return (pa / pb - 1) / yearFrac;
  }

  @Override
  public Double visitInterestRateFuture(final InterestRateFuture future, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve curve = curves.getCurve(future.getCurveName());
    final double ta = future.getSettlementDate();
    final double delta = future.getYearFraction();
    final double tb = ta + delta;
    Validate.isTrue(delta > 0, "tenor span must be greater than zero");
    final double pa = curve.getDiscountFactor(ta);
    final double pb = curve.getDiscountFactor(tb);
    return (pa / pb - 1) / delta;
  }

  @Override
  public Double visitSwap(final Swap swap, final YieldCurveBundle curves) {
    return null;
  }

  @Override
  public Double visitFixedFloatSwap(final FixedFloatSwap swap, final YieldCurveBundle curves) {
    final FixedAnnuity tempAnnuity = swap.getFixedLeg().toUnitCouponFixedAnnuity(swap.getFloatingLeg().getNotional());
    final double pvFloat = PVC.getValue(swap.getFloatingLeg(), curves);
    final double pvFixed = PVC.getValue(tempAnnuity, curves);
    return pvFloat / pvFixed;
  }

  @Override
  public Double visitBasisSwap(final BasisSwap swap, final YieldCurveBundle curves) {

    final VariableAnnuity payLeg = swap.getPayLeg().toZeroSpreadVariableAnnuity();
    final VariableAnnuity receiveLeg = swap.getReceiveLeg().toZeroSpreadVariableAnnuity();
    final FixedAnnuity spreadLeg = swap.getPayLeg().toUnitCouponFixedAnnuity();

    final double pvPay = PVC.getValue(payLeg, curves);
    final double pvReceive = PVC.getValue(receiveLeg, curves);
    final double pvSpread = PVC.getValue(spreadLeg, curves);

    return (pvReceive - pvPay) / pvSpread;
  }

  @Override
  public Double visitBond(final Bond bond, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve curve = curves.getCurve(bond.getCurveName());
    final FixedAnnuity ann = bond.getFixedAnnuity().toUnitCouponFixedAnnuity(1.0);
    final double pvann = PVC.getValue(ann, curves);
    final double maturity = bond.getMaturity();
    return (1 - curve.getDiscountFactor(maturity)) / pvann;
  }

  @Override
  public Double visitVariableAnnuity(final VariableAnnuity annuity, final YieldCurveBundle curves) {
    final FixedAnnuity tempAnnuity = annuity.toUnitCouponFixedAnnuity();
    final double pvFloat = PVC.getValue(annuity, curves);
    final double pvFixed = PVC.getValue(tempAnnuity, curves);
    return pvFloat / pvFixed;
  }

  @Override
  public Double visitFixedAnnuity(final FixedAnnuity annuity, final YieldCurveBundle curves) {
    final FixedAnnuity ann = annuity.toUnitCouponFixedAnnuity(1.0);
    return 1.0 / PVC.getValue(ann, curves);
  }

  @Override
  public Double visitConstantCouponAnnuity(ConstantCouponAnnuity annuity, YieldCurveBundle curves) {
    return visitFixedAnnuity(annuity, curves);
  }

}
