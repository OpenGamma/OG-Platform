/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.Payment;
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
  public Bond visitBond(final Bond bond, final Double rate) {
    final CouponFixed[] payments = bond.getCouponAnnuity().getPayments();
    final int n = payments.length;
    final double[] times = new double[n];
    final double[] coupons = new double[n];
    final double[] yearFrac = new double[n];
    for (int i = 0; i < n; i++) {
      final CouponFixed temp = payments[i];
      times[i] = temp.getPaymentTime();
      coupons[i] = rate;
      yearFrac[i] = temp.getPaymentYearFraction();
    }
    return new Bond(bond.getCurrency(), times, coupons, yearFrac, bond.getAccruedInterest(), payments[0].getFundingCurveName());
  }

  @Override
  public Cash visitCash(final Cash cash, final Double rate) {
    return new Cash(cash.getMaturity(), rate, cash.getTradeTime(), cash.getYearFraction(), cash.getYieldCurveName());
  }

  @Override
  public AnnuityCouponFixed visitFixedCouponAnnuity(final AnnuityCouponFixed annuity, final Double rate) {
    final CouponFixed[] payments = annuity.getPayments();
    final int n = payments.length;
    final CouponFixed[] temp = new CouponFixed[n];
    for (int i = 0; i < n; i++) {
      temp[i] = (CouponFixed) visit(payments[i], rate);
    }
    return new AnnuityCouponFixed(temp);
  }

  @Override
  public CouponFixed visitFixedCouponPayment(final CouponFixed payment, final Double rate) {
    return new CouponFixed(payment.getCurrency(), payment.getPaymentTime(), payment.getFundingCurveName(), payment.getPaymentYearFraction(), payment.getNotional(), rate);
  }

  // TODO is this really correct?
  @Override
  public AnnuityCouponIbor visitForwardLiborAnnuity(final AnnuityCouponIbor annuity, final Double rate) {
    return annuity.withSpread(rate);
  }

  @Override
  public ForwardRateAgreement visitForwardRateAgreement(final ForwardRateAgreement fra, final Double rate) {
    return new ForwardRateAgreement(fra.getSettlementDate(), fra.getMaturity(), fra.getFixingDate(), fra.getForwardYearFraction(), fra.getDiscountingYearFraction(), rate, fra.getFundingCurveName(),
        fra.getIndexCurveName());
  }

  @Override
  public InterestRateFuture visitInterestRateFuture(final InterestRateFuture future, final Double rate) {
    return new InterestRateFuture(future.getSettlementDate(), future.getFixingDate(), future.getMaturity(), future.getIndexYearFraction(), future.getValueYearFraction(), 100 * (1 - rate),
        future.getCurveName());
  }

  @Override
  public TenorSwap<? extends Payment> visitTenorSwap(final TenorSwap<? extends Payment> swap, final Double rate) {
    return new TenorSwap<CouponIbor>((AnnuityCouponIbor) swap.getFirstLeg(), visitForwardLiborAnnuity((AnnuityCouponIbor) swap.getSecondLeg(), rate));
  }

  @Override
  public FixedFloatSwap visitFixedFloatSwap(final FixedFloatSwap swap, final Double rate) {
    return new FixedFloatSwap(visitFixedCouponAnnuity(swap.getFixedLeg(), rate), swap.getSecondLeg());
  }

}
