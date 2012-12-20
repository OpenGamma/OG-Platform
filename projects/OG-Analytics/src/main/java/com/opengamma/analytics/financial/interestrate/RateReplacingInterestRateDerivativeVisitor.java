/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborSpread;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.fra.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFuture;
import com.opengamma.analytics.financial.interestrate.payments.ForexForward;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.CrossCurrencySwap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.FixedFloatSwap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.FloatingRateNote;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.TenorSwap;

/**
 * 
 */
public final class RateReplacingInterestRateDerivativeVisitor extends AbstractInstrumentDerivativeVisitor<Double, InstrumentDerivative> {
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

  // TODO is this really correct?
  @Override
  public AnnuityCouponIborSpread visitAnnuityCouponIborSpread(final AnnuityCouponIborSpread annuity, final Double rate) {
    return annuity.withSpread(rate);
  }

  @Override
  public ForwardRateAgreement visitForwardRateAgreement(final ForwardRateAgreement fra, final Double rate) {
    return new ForwardRateAgreement(fra.getCurrency(), fra.getPaymentTime(), fra.getFundingCurveName(), fra.getPaymentYearFraction(), fra.getNotional(), fra.getIndex(), fra.getFixingTime(),
        fra.getFixingPeriodStartTime(), fra.getFixingPeriodEndTime(), fra.getFixingYearFraction(), rate, fra.getForwardCurveName());
  }

  @Override
  public TenorSwap<? extends Payment> visitTenorSwap(final TenorSwap<? extends Payment> swap, final Double rate) {
    return new TenorSwap<CouponIborSpread>((AnnuityCouponIborSpread) swap.getFirstLeg(), visitAnnuityCouponIborSpread((AnnuityCouponIborSpread) swap.getSecondLeg(), rate));
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public SwapFixedCoupon<?> visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final Double rate) {
    return new SwapFixedCoupon(visitFixedCouponAnnuity(swap.getFixedLeg(), rate), swap.getSecondLeg());
  }

  @Override
  public FixedFloatSwap visitFixedFloatSwap(final FixedFloatSwap swap, final Double rate) {
    return new FixedFloatSwap(visitFixedCouponAnnuity(swap.getFixedLeg(), rate), swap.getSecondLeg());
  }

  @Override
  public FloatingRateNote visitFloatingRateNote(final FloatingRateNote frn, final Double spread) {
    return new FloatingRateNote(frn.getFloatingLeg().withSpread(spread), frn.getFirstLeg().getNthPayment(0), frn.getFirstLeg().getNthPayment(1));
  }

  /**
   * Sets a spread on the foreign ibor payments. Any existing spreads are removed. 
   * @param ccs The Cross Currency Swap
   * @param spread the new spread (any old spread is ignored)
   * @return A new CrossCurrencySwap with the spread set
   */
  @Override
  public CrossCurrencySwap visitCrossCurrencySwap(final CrossCurrencySwap ccs, final Double spread) {
    return new CrossCurrencySwap(visitFloatingRateNote(ccs.getDomesticLeg(), 0.0), visitFloatingRateNote(ccs.getForeignLeg(), spread), ccs.getSpotFX());
  }

  @Override
  //TODO remove (or rethink) this ASAP
  public ForexForward visitForexForward(final ForexForward fx, final Double forwardFX) {
    final double x = -fx.getPaymentCurrency1().getAmount() / forwardFX;
    final PaymentFixed fp = new PaymentFixed(fx.getCurrency2(), fx.getPaymentTime(), x, fx.getPaymentCurrency2().getFundingCurveName());
    return new ForexForward(fx.getPaymentCurrency1(), fp, fx.getSpotForexRate());
  }

  @Override
  public InterestRateFuture visitInterestRateFuture(final InterestRateFuture security, final Double rate) {
    return new InterestRateFuture(security.getLastTradingTime(), security.getIborIndex(), security.getFixingPeriodStartTime(), security.getFixingPeriodEndTime(),
        security.getFixingPeriodAccrualFactor(), 1 - rate, security.getNotional(), security.getPaymentAccrualFactor(), security.getQuantity(), security.getName(), security.getDiscountingCurveName(),
        security.getForwardCurveName());
  }

  @Override
  public BondFixedSecurity visitBondFixedSecurity(final BondFixedSecurity bond, final Double rate) {
    final double originalRate = bond.getCoupon().getNthPayment(0).getFixedRate();
    final double accruedInterest = rate * bond.getAccruedInterest() / originalRate;
    final AnnuityCouponFixed originalCoupons = (AnnuityCouponFixed) bond.getCoupon();
    final AnnuityCouponFixed coupons = visitFixedCouponAnnuity(originalCoupons, rate);
    return new BondFixedSecurity((AnnuityPaymentFixed) bond.getNominal(), coupons, bond.getSettlementTime(), accruedInterest, bond.getAccrualFactorToNextCoupon(), bond.getYieldConvention(),
        bond.getCouponPerYear(), bond.getRepoCurveName(), bond.getIssuer());
  }
}
