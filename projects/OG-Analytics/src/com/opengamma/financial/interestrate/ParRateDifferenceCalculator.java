/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import org.apache.commons.lang.NotImplementedException;
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
**/
public final class ParRateDifferenceCalculator implements InterestRateDerivativeVisitor<YieldCurveBundle, Double> {

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
  public Double visitTenorSwap(final TenorSwap swap, final YieldCurveBundle curves) {
    final double spread = swap.getReceiveLeg().getNthPayment(0).getSpread();
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
  public Double visitForwardRateAgreement(final ForwardRateAgreement fra, final YieldCurveBundle curves) {
    return RATE_CAL.getValue(fra, curves) - fra.getStrike();
  }

  @Override
  public Double visitInterestRateFuture(final InterestRateFuture future, final YieldCurveBundle curves) {
    return RATE_CAL.getValue(future, curves) - (1 - future.getPrice() / 100);
  }

  @Override
  public Double visitFloatingRateNote(final FloatingRateNote frn, final YieldCurveBundle curves) {
    final double spread = frn.getReceiveLeg().getNthPayment(0).getSpread(); // assume constant spreads
    return RATE_CAL.getValue(frn, curves) - spread;
  }

  @Override
  public Double visitFixedCouponSwap(final FixedCouponSwap<?> swap, final YieldCurveBundle curves) {
    return RATE_CAL.getValue(swap, curves) - swap.getFixedLeg().getNthPayment(0).getCoupon();
  }

  @Override
  public Double visitFixedPayment(final FixedPayment payment, final YieldCurveBundle data) {
    throw new NotImplementedException();
  }

  @Override
  public Double visitForwardLiborPayment(final ForwardLiborPayment payment, final YieldCurveBundle data) {
    throw new NotImplementedException();
  }

  @Override
  public Double visitGenericAnnuity(final GenericAnnuity<? extends Payment> annuity, final YieldCurveBundle data) {
    throw new NotImplementedException();
  }

  @Override
  public Double visitSwap(final Swap<?, ?> swap, final YieldCurveBundle data) {
    throw new NotImplementedException();
  }

  @Override
  public Double visitContinuouslyMonitoredAverageRatePayment(final ContinuouslyMonitoredAverageRatePayment payment, final YieldCurveBundle data) {
    throw new NotImplementedException();
  }

  // @Override
  // public Double visitFixedAnnuity(final FixedAnnuity annuity, final YieldCurveBundle curves) {
  // throw new NotImplementedException();
  // }
  //
  // @Override
  // public Double visitConstantCouponAnnuity(final FixedCouponAnnuity annuity, final YieldCurveBundle curves) {
  // throw new NotImplementedException();
  // }
  //
  // @Override
  // public Double visitVariableAnnuity(final ForwardLiborAnnuity annuity, final YieldCurveBundle curves) {
  // throw new NotImplementedException();
  // }

}
