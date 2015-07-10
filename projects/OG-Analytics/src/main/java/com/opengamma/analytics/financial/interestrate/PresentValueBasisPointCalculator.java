/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.fra.method.ForwardRateAgreementDiscountingBundleMethod;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * Computes the present value change when the rate/market quote changes by 1 (it is not rescaled to 1 basis point).
 * The meaning of "rate/market quote" will change for each instrument.
 * For Coupon, FRA and Deposit the result is the discounted accrual factor multiplied by the notional.
 * For PaymentFixed, it is 0 (there is no rate).
 * For annuities, it is the sum of pvbp of all payments.
 * For swaps it is the pvbp of the first leg.
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public final class PresentValueBasisPointCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueBasisPointCalculator INSTANCE = new PresentValueBasisPointCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueBasisPointCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueBasisPointCalculator() {
  }

  /**
   * Methods used in the calculator.
   */
  private static final ForwardRateAgreementDiscountingBundleMethod METHOD_FRA = ForwardRateAgreementDiscountingBundleMethod.getInstance();

  // -----     Deposit     ------

  @Override
  public Double visitCash(final Cash deposit, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(deposit.getYieldCurveName());
    return discountingCurve.getDiscountFactor(deposit.getEndTime()) * deposit.getAccrualFactor() * deposit.getNotional();
  }

  // -----     Payment/Coupon     ------

  @Override
  public Double visitFixedPayment(final PaymentFixed payment, final YieldCurveBundle data) {
    return 0.0;
  }

  public Double visitCoupon(final Coupon coupon, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(coupon);
    final YieldAndDiscountCurve fundingCurve = curves.getCurve(coupon.getFundingCurveName());
    return fundingCurve.getDiscountFactor(coupon.getPaymentTime()) * coupon.getPaymentYearFraction() * coupon.getNotional();
  }

  @Override
  public Double visitCouponFixed(final CouponFixed coupon, final YieldCurveBundle curves) {
    return visitCoupon(coupon, curves);
  }

  @Override
  public Double visitCouponIbor(final CouponIbor coupon, final YieldCurveBundle curves) {
    return visitCoupon(coupon, curves);
  }

  @Override
  public Double visitCouponIborSpread(final CouponIborSpread coupon, final YieldCurveBundle curves) {
    return visitCoupon(coupon, curves);
  }

  @Override
  public Double visitCouponIborCompounding(final CouponIborCompounding coupon, final YieldCurveBundle curves) {
    return visitCoupon(coupon, curves);
  }

  @Override
  public Double visitForwardRateAgreement(final ForwardRateAgreement fra, final YieldCurveBundle curves) {
    return METHOD_FRA.presentValueCouponSensitivity(fra, curves) * fra.getNotional();
  }

  // -----     Futures     ------

  @Override
  public Double visitInterestRateFutureTransaction(final InterestRateFutureTransaction future, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(future, "Futures");
    ArgumentChecker.notNull(curves, "Bundle");
    return future.getUnderlyingSecurity().getNotional() * future.getUnderlyingSecurity().getPaymentAccrualFactor() * future.getQuantity();
  }

  // -----     Annuity     ------

  @Override
  public Double visitGenericAnnuity(final Annuity<? extends Payment> annuity, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(annuity);
    double pvbp = 0;
    for (final Payment p : annuity.getPayments()) {
      pvbp += p.accept(this, curves);
    }
    return pvbp;
  }

  @Override
  public Double visitFixedCouponAnnuity(final AnnuityCouponFixed annuity, final YieldCurveBundle curves) {
    return visitGenericAnnuity(annuity, curves);
  }

  // -----     Swap     ------

  @Override
  public Double visitSwap(final Swap<?, ?> swap, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(swap);
    return swap.getFirstLeg().accept(this, curves);
  }

  @Override
  public Double visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final YieldCurveBundle curves) {
    return visitSwap(swap, curves);
  }

  // -----     Forex     ------

  @Override
  public Double visitForexSwap(final ForexSwap derivative, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve dsc2 = curves.getCurve(derivative.getFarLeg().getPaymentCurrency2().getFundingCurveName());
    final double pvPtCcy2 = dsc2.getDiscountFactor(derivative.getFarLeg().getPaymentTime()) * -derivative.getFarLeg().getPaymentCurrency1().getAmount();
    return curves.getFxRates().getFxRate(derivative.getFarLeg().getCurrency2(), derivative.getFarLeg().getCurrency1()) * pvPtCcy2;
  }

}
