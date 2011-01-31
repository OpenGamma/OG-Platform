/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import com.opengamma.financial.interestrate.annuity.definition.FixedCouponAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.ForwardLiborAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.payments.FixedCouponPayment;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;

/**
 * 
 */
public final class RateReplacingInterestRateDerivativeVisitor extends AbstractInterestRateDerivativeVisitor<Double, InterestRateDerivative> {
  private static final RateReplacingInterestRateDerivativeVisitor INSTANCE = new RateReplacingInterestRateDerivativeVisitor();

  public static RateReplacingInterestRateDerivativeVisitor getInstance() {
    return INSTANCE;
  }

  private RateReplacingInterestRateDerivativeVisitor() {
  }

  @Override
  public Bond visitBond(Bond bond, Double rate) {
    final FixedCouponPayment[] payments = bond.getCouponAnnuity().getPayments();
    final int n = payments.length;
    final double[] times = new double[n];
    final double[] coupons = new double[n];
    final double[] yearFrac = new double[n];
    for (int i = 0; i < n; i++) {
      final FixedCouponPayment temp = payments[i];
      times[i] = temp.getPaymentTime();
      coupons[i] = rate;
      yearFrac[i] = temp.getYearFraction();
    }
    return new Bond(times, coupons, yearFrac, bond.getAccruedInterest(), payments[0].getFundingCurveName());
  }

  @Override
  public Cash visitCash(Cash cash, Double rate) {
    return new Cash(cash.getMaturity(), rate, cash.getTradeTime(), cash.getYearFraction(), cash.getYieldCurveName());
  }

  @Override
  public FixedCouponAnnuity visitFixedCouponAnnuity(FixedCouponAnnuity annuity, final Double rate) {
    final FixedCouponPayment[] payments = annuity.getPayments();
    final int n = payments.length;
    final FixedCouponPayment[] temp = new FixedCouponPayment[n];
    for (int i = 0; i < n; i++) {
      temp[i] = (FixedCouponPayment) visit(payments[i], rate);
    }
    return new FixedCouponAnnuity(temp);
  }

  @Override
  public FixedCouponPayment visitFixedCouponPayment(FixedCouponPayment payment, final Double rate) {
    return new FixedCouponPayment(payment.getPaymentTime(), payment.getNotional(), payment.getYearFraction(), rate, payment.getFundingCurveName());
  }

  // TODO is this really correct?
  @Override
  public ForwardLiborAnnuity visitForwardLiborAnnuity(ForwardLiborAnnuity annuity, final Double rate) {
    return annuity.withSpread(rate);
  }

  @Override
  public ForwardRateAgreement visitForwardRateAgreement(ForwardRateAgreement fra, final Double rate) {
    return new ForwardRateAgreement(fra.getSettlementDate(), fra.getMaturity(), fra.getFixingDate(), fra.getForwardYearFraction(), fra.getDiscountingYearFraction(), rate, fra.getFundingCurveName(),
        fra.getIndexCurveName());
  }

  @Override
  public InterestRateFuture visitInterestRateFuture(InterestRateFuture future, final Double rate) {
    return new InterestRateFuture(future.getSettlementDate(), future.getFixingDate(), future.getMaturity(), future.getIndexYearFraction(), future.getValueYearFraction(), 100 * (1 - rate),
        future.getCurveName());
  }

  @Override
  public TenorSwap visitTenorSwap(TenorSwap swap, Double rate) {
    return new TenorSwap(swap.getPayLeg(), visitForwardLiborAnnuity((ForwardLiborAnnuity) swap.getReceiveLeg(), rate));
  }

  @Override
  public FixedFloatSwap visitFixedFloatSwap(FixedFloatSwap swap, final Double rate) {
    return new FixedFloatSwap(visitFixedCouponAnnuity(swap.getFixedLeg(), rate), swap.getReceiveLeg());
  }

}
