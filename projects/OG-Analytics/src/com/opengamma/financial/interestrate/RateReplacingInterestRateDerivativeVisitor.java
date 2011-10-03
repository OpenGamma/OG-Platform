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
import com.opengamma.financial.interestrate.fra.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureSecurity;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureTransaction;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.financial.interestrate.swap.definition.CrossCurrencySwap;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.FloatingRateNote;
import com.opengamma.financial.interestrate.swap.definition.ForexForward;
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
    return new Cash(cash.getCurrency(), cash.getMaturity(), cash.getNotional(), rate, cash.getTradeTime(), cash.getYearFraction(), cash.getYieldCurveName());
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
    return new ForwardRateAgreement(fra.getCurrency(), fra.getPaymentTime(), fra.getFundingCurveName(), fra.getPaymentYearFraction(), fra.getNotional(), fra.getIndex(), fra.getFixingTime(),
        fra.getFixingPeriodStartTime(), fra.getFixingPeriodEndTime(), fra.getFixingYearFraction(), rate, fra.getForwardCurveName());
  }

  @Override
  public TenorSwap<? extends Payment> visitTenorSwap(final TenorSwap<? extends Payment> swap, final Double rate) {
    return new TenorSwap<CouponIbor>((AnnuityCouponIbor) swap.getFirstLeg(), visitForwardLiborAnnuity((AnnuityCouponIbor) swap.getSecondLeg(), rate));
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
   * @param ccs The swap
   * @param spread The spread
   * @return A cross-currency swap with a different FX rate 
   */
  @Override
  public CrossCurrencySwap visitCrossCurrencySwap(final CrossCurrencySwap ccs, final Double spread) {
    return new CrossCurrencySwap(visitFloatingRateNote(ccs.getDomesticLeg(), 0.0), visitFloatingRateNote(ccs.getForeignLeg(), spread), ccs.getSpotFX());
  }

  @Override //TODO remove (or rethink) this ASAP
  public ForexForward visitForexForward(final ForexForward fx, Double forwardFX) {
    double x = -fx.getPaymentCurrency1().getAmount() / forwardFX;
    PaymentFixed fp = new PaymentFixed(fx.getCurrency2(), fx.getPaymentTime(), x, fx.getPaymentCurrency2().getFundingCurveName());
    return new ForexForward(fx.getPaymentCurrency1(), fp, fx.getSpotForexRate());
  }

  @Override
  public InterestRateFutureTransaction visitInterestRateFutureTransaction(final InterestRateFutureTransaction future, final Double rate) {
    final InterestRateFutureSecurity security = future.getUnderlyingFuture();
    return new InterestRateFutureTransaction(new InterestRateFutureSecurity(security.getLastTradingTime(), security.getIborIndex(), security.getFixingPeriodStartTime(),
        security.getFixingPeriodEndTime(), security.getFixingPeriodAccrualFactor(), security.getNotional(), security.getPaymentAccrualFactor(), security.getName(), security.getDiscountingCurveName(),
        security.getForwardCurveName()), future.getQuantity(), 1 - rate);
  }
}
