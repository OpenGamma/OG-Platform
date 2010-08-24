/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import org.apache.commons.lang.NotImplementedException;
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

/**
 * 
**/
public final class ParRateDifferenceCalculator implements InterestRateDerivativeVisitor<Double> {

  private static final ParRateCalculator RATE_CAL = ParRateCalculator.getInstance();
  private static ParRateDifferenceCalculator s_instance = new ParRateDifferenceCalculator();

  public static ParRateDifferenceCalculator getInstance() {
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
  public Double visitBasisSwap(final BasisSwap swap, final YieldCurveBundle curves) {
    final double spread = swap.getReceiveLeg().getSpreads()[0];
    return RATE_CAL.getValue(swap, curves) - spread;

  }

  @Override
  public Double visitBond(final Bond bond, final YieldCurveBundle curves) {
    return null;
  }

  @Override
  public Double visitCash(final Cash cash, final YieldCurveBundle curves) {
    return RATE_CAL.getValue(cash, curves) - cash.getRate();
  }

  @Override
  public Double visitFixedFloatSwap(final FixedFloatSwap swap, final YieldCurveBundle curves) {
    return RATE_CAL.getValue(swap, curves) - swap.getFixedLeg().getCouponRate();
  }

  @Override
  public Double visitForwardRateAgreement(final ForwardRateAgreement fra, final YieldCurveBundle curves) {
    return RATE_CAL.getValue(fra, curves) - fra.getStrike();
  }

  @Override
  public Double visitInterestRateFuture(final InterestRateFuture future, final YieldCurveBundle curves) {
    return RATE_CAL.getValue(future, curves) - (1 - future.getPrice() / 100);
  }

  @Override
  public Double visitSwap(final Swap swap, final YieldCurveBundle curves) {
    throw new NotImplementedException();
  }

  @Override
  public Double visitFloatingRateNote(final FloatingRateNote frn, final YieldCurveBundle curves) {
    final double spread = frn.getReceiveLeg().getSpreads()[0];
    return RATE_CAL.getValue(frn, curves) - spread;
  }

  @Override
  public Double visitFixedAnnuity(final FixedAnnuity annuity, final YieldCurveBundle curves) {
    throw new NotImplementedException();
  }

  @Override
  public Double visitConstantCouponAnnuity(final ConstantCouponAnnuity annuity, final YieldCurveBundle curves) {
    throw new NotImplementedException();
  }

  @Override
  public Double visitVariableAnnuity(final VariableAnnuity annuity, final YieldCurveBundle curves) {
    throw new NotImplementedException();
  }

}
