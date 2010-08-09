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

/**
 * 
**/
public final class ParRateDifferenceCalculator implements InterestRateDerivativeVisitor<Double> {

  private static final ParRateCalculator RATE_CAL = ParRateCalculator.getInstance();
  private static ParRateDifferenceCalculator s_instance;

  public static ParRateDifferenceCalculator getInstance() {
    if (s_instance == null) {
      s_instance = new ParRateDifferenceCalculator();
    }
    return s_instance;
  }

  private ParRateDifferenceCalculator() {
  }

  @Override
  public Double getValue(final InterestRateDerivative derivative, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(derivative);
    return derivative.accept(this, curves);
  }

  @Override
  public Double visitBasisSwap(BasisSwap swap, YieldCurveBundle curves) {
    double spread = swap.getPayLeg().getSpreads()[0];
    return RATE_CAL.getValue(swap, curves) - spread;

  }

  @Override
  public Double visitBond(Bond bond, YieldCurveBundle curves) {
    return null;
  }

  @Override
  public Double visitCash(Cash cash, YieldCurveBundle curves) {
    return RATE_CAL.getValue(cash, curves) - cash.getRate();
  }

  @Override
  public Double visitFixedFloatSwap(FixedFloatSwap swap, YieldCurveBundle curves) {
    return RATE_CAL.getValue(swap, curves) - swap.getFixedLeg().getCouponRate();
  }

  @Override
  public Double visitForwardRateAgreement(ForwardRateAgreement fra, YieldCurveBundle curves) {
    return RATE_CAL.getValue(fra, curves) - fra.getStrike();
  }

  @Override
  public Double visitInterestRateFuture(InterestRateFuture future, YieldCurveBundle curves) {
    return RATE_CAL.getValue(future, curves) - (1 - future.getPrice() / 100);
  }

  @Override
  public Double visitSwap(Swap swap, YieldCurveBundle curves) {
    return null;
  }

  @Override
  public Double visitFixedAnnuity(FixedAnnuity annuity, YieldCurveBundle curves) {
    return null;
  }

  @Override
  public Double visitConstantCouponAnnuity(ConstantCouponAnnuity annuity, YieldCurveBundle curves) {
    return null;
  }

  @Override
  public Double visitVariableAnnuity(VariableAnnuity annuity, YieldCurveBundle curves) {
    return null;
  }

}
