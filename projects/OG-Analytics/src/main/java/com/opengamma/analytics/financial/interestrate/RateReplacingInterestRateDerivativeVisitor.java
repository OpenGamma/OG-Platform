/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;

/**
 * @deprecated This calculator uses {@link InstrumentDerivative}s that refer to curve names.
 * Use {@link RateReplacingVisitor}.
 */
@Deprecated
public final class RateReplacingInterestRateDerivativeVisitor extends InstrumentDerivativeVisitorAdapter<Double, InstrumentDerivative> {
  private static final RateReplacingInterestRateDerivativeVisitor INSTANCE = new RateReplacingInterestRateDerivativeVisitor();

  public static RateReplacingInterestRateDerivativeVisitor getInstance() {
    return INSTANCE;
  }

  private RateReplacingInterestRateDerivativeVisitor() {
  }

  @Override
  public Cash visitCash(final Cash cash, final Double rate) {
    return new Cash(cash.getCurrency(), cash.getStartTime(), cash.getEndTime(), cash.getNotional(), rate, cash.getAccrualFactor(), cash.getYieldCurveName());
  }

  @Override
  public AnnuityCouponFixed visitFixedCouponAnnuity(final AnnuityCouponFixed annuity, final Double rate) {
    final CouponFixed[] payments = annuity.getPayments();
    final int n = payments.length;
    final CouponFixed[] temp = new CouponFixed[n];
    for (int i = 0; i < n; i++) {
      temp[i] = visitCouponFixed(payments[i], rate);
    }
    return new AnnuityCouponFixed(temp);
  }

  @Override
  public CouponFixed visitCouponFixed(final CouponFixed payment, final Double rate) {
    return new CouponFixed(payment.getCurrency(), payment.getPaymentTime(), payment.getFundingCurveName(), payment.getPaymentYearFraction(), payment.getNotional(), rate,
        payment.getAccrualStartDate(), payment.getAccrualEndDate());
  }

  @Override
  public ForwardRateAgreement visitForwardRateAgreement(final ForwardRateAgreement fra, final Double rate) {
    return new ForwardRateAgreement(fra.getCurrency(), fra.getPaymentTime(), fra.getFundingCurveName(), fra.getPaymentYearFraction(), fra.getNotional(), fra.getIndex(), fra.getFixingTime(),
        fra.getFixingPeriodStartTime(), fra.getFixingPeriodEndTime(), fra.getFixingYearFraction(), rate, fra.getForwardCurveName());
  }

  @Override
  public SwapFixedCoupon<?> visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final Double rate) {
    return new SwapFixedCoupon<>(visitFixedCouponAnnuity(swap.getFixedLeg(), rate), swap.getSecondLeg());
  }

  @Override
  public InterestRateFutureTransaction visitInterestRateFutureTransaction(final InterestRateFutureTransaction futures, final Double rate) {
    return new InterestRateFutureTransaction(futures.getUnderlyingFuture(), 1 - rate, futures.getQuantity());
  }

  @Override
  public BondFixedSecurity visitBondFixedSecurity(final BondFixedSecurity bond, final Double rate) {
    final double originalRate = bond.getCoupon().getNthPayment(0).getFixedRate();
    final double accruedInterest = rate * bond.getAccruedInterest() / originalRate;
    final AnnuityCouponFixed originalCoupons = (AnnuityCouponFixed) bond.getCoupon();
    final AnnuityCouponFixed coupons = visitFixedCouponAnnuity(originalCoupons, rate);
    return new BondFixedSecurity((AnnuityPaymentFixed) bond.getNominal(), coupons, bond.getSettlementTime(), accruedInterest, bond.getFactorToNextCoupon(), bond.getYieldConvention(),
        bond.getCouponPerYear(), bond.getRepoCurveName(), bond.getIssuerEntity());
  }
}
