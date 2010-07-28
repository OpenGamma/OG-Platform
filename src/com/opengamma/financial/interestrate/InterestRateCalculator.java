/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import org.apache.commons.lang.Validate;

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
 * Get the single fixed rate that makes the PV of the instrument zero. For  fixed-float swaps this is the swap rate, for FRAs it is the forward etc. For instruments that cannot PV to zero, e.g. bonds, a single payment of -1.0 
 * is assumed at zero (i.e. the bond must PV to 1.0)
 */
public class InterestRateCalculator implements InterestRateDerivativeVisitor<Double> {

  private final PresentValueCalculator _pvCalculator = new PresentValueCalculator();

  public double getRate(final InterestRateDerivative derivative, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(derivative);
    return derivative.accept(this, curves);
  }

  @Override
  public Double visitCash(Cash cash, YieldCurveBundle curves) {
    YieldAndDiscountCurve curve = curves.getCurve(cash.getYieldCurveName());
    double ta = cash.getTradeTime();
    double tb = cash.getPaymentTime();
    double yearFrac = cash.getYearFraction();
    // TODO need a getForwardRate method on YieldAndDiscountCurve
    if (yearFrac == 0.0) {
      if (ta != tb) {
        throw new IllegalArgumentException("Year fraction is zero, but payment time greater than trade time");
      }
      double eps = 1e-8;
      double rate = curve.getInterestRate(ta);
      double dRate = curve.getInterestRate(ta + eps);
      return rate + ta * (dRate - rate) / eps;
    }
    return (curve.getDiscountFactor(ta) / curve.getDiscountFactor(tb) - 1) / yearFrac;
  }

  @Override
  public Double visitForwardRateAgreement(ForwardRateAgreement fra, YieldCurveBundle curves) {
    YieldAndDiscountCurve curve = curves.getCurve(fra.getLiborCurveName());
    final double ta = fra.getFixingDate();
    final double tb = fra.getMaturity();
    final double yearFrac = fra.getForwardYearFraction();
    Validate.isTrue(yearFrac > 0, "tenor span must be greater than zero");
    final double pa = curve.getDiscountFactor(ta);
    final double pb = curve.getDiscountFactor(tb);
    return (pa / pb - 1) / yearFrac;
  }

  @Override
  public Double visitInterestRateFuture(InterestRateFuture future, YieldCurveBundle curves) {
    YieldAndDiscountCurve curve = curves.getCurve(future.getCurveName());
    final double ta = future.getSettlementDate();
    final double delta = future.getYearFraction();
    final double tb = ta + delta;
    Validate.isTrue(delta > 0, "tenor span must be greater than zero");
    final double pa = curve.getDiscountFactor(ta);
    final double pb = curve.getDiscountFactor(tb);
    return (pa / pb - 1) / delta;
  }

  @Override
  public Double visitSwap(Swap swap, YieldCurveBundle curves) {
    return null;
  }

  @Override
  public Double visitFixedFloatSwap(FixedFloatSwap swap, YieldCurveBundle curves) {
    FixedAnnuity tempAnnuity = swap.getFixedLeg().makeUnitCouponVersion(swap.getFloatingLeg().getNotional());
    double pvFloat = _pvCalculator.getPresentValue(swap.getFloatingLeg(), curves);
    double pvFixed = _pvCalculator.getPresentValue(tempAnnuity, curves);
    return pvFloat / pvFixed;
  }

  @Override
  public Double visitBasisSwap(BasisSwap swap, YieldCurveBundle curves) {

    VariableAnnuity payLeg = swap.getPayLeg().makeZeroSpreadVersion();
    VariableAnnuity receiveLeg = swap.getReceiveLeg().makeZeroSpreadVersion();
    FixedAnnuity spreadLeg = swap.getPayLeg().makeUnitCouponVersion();

    double pvPay = _pvCalculator.getPresentValue(payLeg, curves);
    double pvRecieve = _pvCalculator.getPresentValue(receiveLeg, curves);
    double pvSpread = _pvCalculator.getPresentValue(spreadLeg, curves);

    return (pvRecieve - pvPay) / pvSpread;
  }

  @Override
  public Double visitBond(Bond bond, YieldCurveBundle curves) {
    YieldAndDiscountCurve curve = curves.getCurve(bond.getCurveName());
    FixedAnnuity ann = bond.getFixedAnnuity().makeUnitCouponVersion(1.0);
    double pvann = _pvCalculator.getPresentValue(ann, curves);
    double maturity = bond.getPaymentTimes()[bond.getPaymentTimes().length - 1];
    return (1 - curve.getDiscountFactor(maturity)) / pvann;
  }

  @Override
  public Double visitVariableAnnuity(VariableAnnuity annuity, YieldCurveBundle curves) {
    FixedAnnuity tempAnnuity = annuity.makeUnitCouponVersion();
    double pvFloat = _pvCalculator.getPresentValue(annuity, curves);
    double pvFixed = _pvCalculator.getPresentValue(tempAnnuity, curves);
    return pvFloat / pvFixed;
  }

  @Override
  public Double visitFixedAnnuity(FixedAnnuity annuity, YieldCurveBundle curves) {
    FixedAnnuity ann = annuity.makeUnitCouponVersion(1.0);
    return 1.0 / _pvCalculator.getPresentValue(ann, curves);
  }

}
