/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.libor.Libor;
import com.opengamma.financial.interestrate.swap.definition.BasisSwap;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * 
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
    return curve.getInterestRate(cash.getPaymentTime());
  }

  @Override
  public Double visitForwardRateAgreement(ForwardRateAgreement fra, YieldCurveBundle curves) {
    YieldAndDiscountCurve curve = curves.getCurve(fra.getLiborCurveName());
    final double ta = fra.getStartTime();
    final double tb = fra.getEndTime();
    final double delta = tb - ta;
    Validate.isTrue(delta > 0, "tenor span must be greater than zero");
    final double pa = curve.getDiscountFactor(ta);
    final double pb = curve.getDiscountFactor(tb);
    return (pa / pb - 1) / delta;
  }

  @Override
  public Double visitInterestRateFuture(InterestRateFuture future, YieldCurveBundle curves) {
    YieldAndDiscountCurve curve = curves.getCurve(future.getCurveName());
    final double ta = future.getStartTime();
    final double tb = future.getEndTime();
    final double delta = tb - ta;
    Validate.isTrue(delta > 0, "tenor span must be greater than zero");
    final double pa = curve.getDiscountFactor(ta);
    final double pb = curve.getDiscountFactor(tb);
    return (pa / pb - 1) / delta;
  }

  @Override
  public Double visitLibor(Libor libor, YieldCurveBundle curves) {
    YieldAndDiscountCurve curve = curves.getCurve(libor.getLiborCurveName());
    double t = libor.getPaymentTime();
    // avoid divide by zero when t very small
    if (t < 0.01) {
      double r = curve.getInterestRate(t);
      return r * (1 + r * t / 2);
    }
    return (1. / curve.getDiscountFactor(t) - 1) / t;
  }

  @Override
  public Double visitSwap(FixedFloatSwap swap, YieldCurveBundle curves) {
    double pvFloat = _pvCalculator.getLiborAnnuity(swap.getFloatingLeg(), curves);
    double pvFixed = _pvCalculator.getFixedAnnuity(swap.getFixedLeg(), curves);
    return pvFloat / pvFixed;
  }

  @Override
  public Double visitBasisSwap(BasisSwap swap, YieldCurveBundle curves) {

    double pvPay = _pvCalculator.getLiborAnnuity(swap.getPayLeg(), curves);
    double pvRecieve = _pvCalculator.getLiborAnnuity(swap.getRecieveLeg(), curves);
    double pvSpread = _pvCalculator.getFixedAnnuity(swap.getSpreadLeg(), curves);

    return (pvRecieve - pvPay) / pvSpread;
  }

}
