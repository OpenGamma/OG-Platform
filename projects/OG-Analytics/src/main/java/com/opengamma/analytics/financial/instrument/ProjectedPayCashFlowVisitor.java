/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Returns all of the projected pay floating cash-flows of an instrument.
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public final class ProjectedPayCashFlowVisitor extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, List<MultipleCurrencyAmount>> {
  private static final ProjectedPayCashFlowVisitor INSTANCE = new ProjectedPayCashFlowVisitor();

  public static ProjectedPayCashFlowVisitor getInstance() {
    return INSTANCE;
  }

  private ProjectedPayCashFlowVisitor() {
    //    super(Collections.<MultipleCurrencyAmount>emptyList());
  }

  /**
   * If the notional is positive (i.e. the amount is to be received), returns
   * an empty list. Otherwise, returns a list containing the payment estimated
   * from the forward curve.
   * @param coupon The coupon instrument, not null
   * @param data The yield curves, not null
   * @return A list containing the (single) amount, or an empty list, as appropriate
   */
  @Override
  public List<MultipleCurrencyAmount> visitCouponIbor(final CouponIbor coupon, final YieldCurveBundle data) {
    ArgumentChecker.notNull(coupon, "coupon");
    ArgumentChecker.notNull(data, "data");
    if (coupon.getNotional() > 0) {
      return Collections.emptyList();
    }
    final YieldAndDiscountCurve forwardCurve = data.getCurve(coupon.getForwardCurveName());
    final double forward = (forwardCurve.getDiscountFactor(coupon.getFixingPeriodStartTime()) / forwardCurve.getDiscountFactor(coupon.getFixingPeriodEndTime()) - 1) / coupon.getFixingAccrualFactor();
    final double amount = -coupon.getNotional() * coupon.getPaymentYearFraction() * forward;
    return Collections.singletonList(MultipleCurrencyAmount.of(CurrencyAmount.of(coupon.getCurrency(), amount)));
  }

  /**
   * If the notional is positive (i.e. the amount is to be received), returns
   * an empty list. Otherwise, returns a list containing the payment estimated
   * from the forward curve.
   * @param coupon The coupon instrument, not null
   * @param data The yield curves, not null
   * @return A list containing the (single) amount, or an empty list, as appropriate
   */
  @Override
  public List<MultipleCurrencyAmount> visitCouponIborSpread(final CouponIborSpread coupon, final YieldCurveBundle data) {
    ArgumentChecker.notNull(coupon, "coupon");
    ArgumentChecker.notNull(data, "data");
    if (coupon.getNotional() > 0) {
      return Collections.emptyList();
    }
    final YieldAndDiscountCurve forwardCurve = data.getCurve(coupon.getForwardCurveName());
    final double forward = (forwardCurve.getDiscountFactor(coupon.getFixingPeriodStartTime()) / forwardCurve.getDiscountFactor(coupon.getFixingPeriodEndTime()) - 1) / coupon.getFixingAccrualFactor();
    final double amount = -(coupon.getNotional() * coupon.getPaymentYearFraction() * forward + coupon.getSpreadAmount());
    return Collections.singletonList(MultipleCurrencyAmount.of(CurrencyAmount.of(coupon.getCurrency(), amount)));
  }

  /**
   * If the notional is positive (i.e. the amount is to be received), returns
   * an empty list. Otherwise, returns a list containing the payment
   * estimated from the forward curve.
   * @param coupon The coupon instrument, not null
   * @param data The yield curves, not null
   * @return A list containing the (single) amount, or an empty list, as appropriate
   */
  @Override
  public List<MultipleCurrencyAmount> visitCouponIborGearing(final CouponIborGearing coupon, final YieldCurveBundle data) {
    ArgumentChecker.notNull(coupon, "coupon");
    ArgumentChecker.notNull(data, "data");
    if (coupon.getNotional() > 0) {
      return Collections.emptyList();
    }
    final YieldAndDiscountCurve forwardCurve = data.getCurve(coupon.getForwardCurveName());
    final double forward = (forwardCurve.getDiscountFactor(coupon.getFixingPeriodStartTime()) / forwardCurve.getDiscountFactor(coupon.getFixingPeriodEndTime()) - 1) / coupon.getFixingAccrualFactor();
    final double amount = -(coupon.getNotional() * coupon.getPaymentYearFraction() * (coupon.getFactor() * forward) + coupon.getSpreadAmount());
    return Collections.singletonList(MultipleCurrencyAmount.of(CurrencyAmount.of(coupon.getCurrency(), amount)));
  }

  /**
   * If the notional is positive (i.e. the FRA is a payer), returns an empty list. Otherwise, returns
   * a list containing the payment estimated from the forward curve.
   * @param fra The FRA, not null
   * @param data The yield curves, not null
   * @return A list containing the (single) amount, or an empty list, as appropriate
   */
  @Override
  public List<MultipleCurrencyAmount> visitForwardRateAgreement(final ForwardRateAgreement fra, final YieldCurveBundle data) {
    ArgumentChecker.notNull(fra, "FRA");
    ArgumentChecker.notNull(data, "data");
    if (fra.getNotional() > 0) {
      return Collections.emptyList();
    }
    final YieldAndDiscountCurve forwardCurve = data.getCurve(fra.getForwardCurveName());
    final double forward = (forwardCurve.getDiscountFactor(fra.getFixingPeriodStartTime()) / forwardCurve.getDiscountFactor(fra.getFixingPeriodEndTime()) - 1) / fra.getFixingYearFraction();
    final double amount = -fra.getPaymentYearFraction() * fra.getNotional() * forward / (1 + fra.getPaymentYearFraction() * forward);
    return Collections.singletonList(MultipleCurrencyAmount.of(CurrencyAmount.of(fra.getCurrency(), amount)));
  }

  /**
   * Returns a list containing all of the estimated floating payments of an annuity. If there are no floating payments to be made,
   * an empty list is returned
   * @param annuity The annuity, not null
   * @param data The yield curves, not null
   * @return A list containing the amounts
   */
  @Override
  public List<MultipleCurrencyAmount> visitGenericAnnuity(final Annuity<? extends Payment> annuity, final YieldCurveBundle data) {
    ArgumentChecker.notNull(annuity, "annuity");
    ArgumentChecker.notNull(data, "data");
    return getDatesFromAnnuity(annuity, data);
  }

  /**
   * Returns a list containing all of the estimated floating payments to be made.
   * @param swap The swap, not null
   * @param data The yield curves, not null
   * @return A list containing floating payments
   */
  @Override
  public List<MultipleCurrencyAmount> visitSwap(final Swap<?, ?> swap, final YieldCurveBundle data) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(data, "data");
    if (swap.getFirstLeg().isPayer()) {
      return swap.getFirstLeg().accept(this, data);
    }
    return swap.getSecondLeg().accept(this, data);
  }

  private List<MultipleCurrencyAmount> getDatesFromAnnuity(final Annuity<? extends Payment> annuity, final YieldCurveBundle data) {
    final List<MultipleCurrencyAmount> result = new ArrayList<>();
    for (final Payment payment : annuity.getPayments()) {
      final List<MultipleCurrencyAmount> payments = payment.accept(this, data);
      for (final MultipleCurrencyAmount mca : payments) {
        final int scale = mca.getCurrencyAmounts()[0].getAmount() < 0 ? -1 : 1;
        result.add(mca.multipliedBy(scale));
      }
    }
    return result;
  }

}
