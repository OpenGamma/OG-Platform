/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.discounting;

import java.util.TreeMap;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Calculator to compute the cash flow equivalent of some interest rate derivative. The "equivalence" is based on
 * a single curve simplifying assumption.
 * <p>
 * The calculator also provides methods to compute present value and present value sensitivity of the objects 
 * describing the cash-flows.
 */
public class CashflowEquivalentTheoreticalCalculator
    extends InstrumentDerivativeVisitorAdapter<Object, AnnuityPaymentFixed> {

  /**
   * The unique instance of the calculator.
   */
  private static final CashflowEquivalentTheoreticalCalculator INSTANCE = new CashflowEquivalentTheoreticalCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static CashflowEquivalentTheoreticalCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  CashflowEquivalentTheoreticalCalculator() {
  }

  /*     =====     Coupon     =====     */
  @Override
  public AnnuityPaymentFixed visitFixedPayment(final PaymentFixed payment) {
    ArgumentChecker.notNull(payment, "Payment");
    return new AnnuityPaymentFixed(new PaymentFixed[] {payment});
  }

  @Override
  public AnnuityPaymentFixed visitCouponFixed(final CouponFixed coupon) {
    ArgumentChecker.notNull(coupon, "Coupon");
    return new AnnuityPaymentFixed(new PaymentFixed[] {coupon.toPaymentFixed()});
  }

  @Override
  public AnnuityPaymentFixed visitCouponIbor(final CouponIbor payment) {
    ArgumentChecker.notNull(payment, "Payment");
    final double fixingStartTime = payment.getFixingPeriodStartTime();
    final double fixingEndTime = payment.getFixingPeriodEndTime();
    final PaymentFixed paymentStart = new PaymentFixed(payment.getCurrency(), fixingStartTime, payment.getNotional());
    final PaymentFixed paymentEnd = new PaymentFixed(payment.getCurrency(), fixingEndTime, -payment.getNotional());
    return new AnnuityPaymentFixed(new PaymentFixed[] {paymentStart, paymentEnd });
  }

  @Override
  public AnnuityPaymentFixed visitCouponIborSpread(final CouponIborSpread payment) {
    ArgumentChecker.notNull(payment, "Payment");
    final double fixingStartTime = payment.getFixingPeriodStartTime();
    final double fixingEndTime = payment.getFixingPeriodEndTime();
    final PaymentFixed paymentStart = new PaymentFixed(payment.getCurrency(), fixingStartTime, payment.getNotional());
    final PaymentFixed paymentEnd = new PaymentFixed(payment.getCurrency(), fixingEndTime, 
        -payment.getNotional() + payment.getSpreadAmount());
    return new AnnuityPaymentFixed(new PaymentFixed[] {paymentStart, paymentEnd });
  }

  @Override
  public AnnuityPaymentFixed visitCouponIborGearing(final CouponIborGearing payment) {
    ArgumentChecker.notNull(payment, "Payment");
    final double fixingStartTime = payment.getFixingPeriodStartTime();
    final double fixingEndTime = payment.getFixingPeriodEndTime();
    final PaymentFixed paymentStart = new PaymentFixed(payment.getCurrency(), fixingStartTime,
        payment.getNotional() * payment.getFactor());
    final PaymentFixed paymentEnd = new PaymentFixed(payment.getCurrency(), fixingEndTime,
        -payment.getNotional() * payment.getFactor() + payment.getSpreadAmount());
    return new AnnuityPaymentFixed(new PaymentFixed[] {paymentStart, paymentEnd });
  }
  
  // TODO: OIS, FRA, Compounding

  /*     =====     Annuity     =====     */

  @Override
  public AnnuityPaymentFixed visitGenericAnnuity(final Annuity<? extends Payment> annuity) {
    ArgumentChecker.notNull(annuity, "Annuity");
    final TreeMap<Double, Double> flow = new TreeMap<>();
    final Currency ccy = annuity.getCurrency();
    for (final Payment p : annuity.getPayments()) {
      final AnnuityPaymentFixed cfe = p.accept(this);
      for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
        addcf(flow, cfe.getNthPayment(loopcf).getPaymentTime(), cfe.getNthPayment(loopcf).getAmount());
      }
    }
    final PaymentFixed[] agregatedCfe = new PaymentFixed[flow.size()];
    int loopcf = 0;
    for (final double time : flow.keySet()) {
      agregatedCfe[loopcf++] = new PaymentFixed(ccy, time, flow.get(time));
    }
    return new AnnuityPaymentFixed(agregatedCfe);
  }

  /*     =====     Swap     =====     */

  @Override
  public AnnuityPaymentFixed visitSwap(final Swap<?, ?> swap) {
    ArgumentChecker.notNull(swap, "Swap");
    final Currency ccy = swap.getFirstLeg().getCurrency();
    Validate.isTrue(ccy.equals(swap.getSecondLeg().getCurrency()), 
        "Cash flow equivalent available only for single currency swaps.");
    final TreeMap<Double, Double> flow = new TreeMap<>();
    final AnnuityPaymentFixed cfeLeg1 = swap.getFirstLeg().accept(this);
    final AnnuityPaymentFixed cfeLeg2 = swap.getSecondLeg().accept(this);
    for (final PaymentFixed p : cfeLeg1.getPayments()) {
      flow.put(p.getPaymentTime(), p.getAmount());
    }
    for (final PaymentFixed p : cfeLeg2.getPayments()) {
      addcf(flow, p.getPaymentTime(), p.getAmount());
    }
    final PaymentFixed[] agregatedCfe = new PaymentFixed[flow.size()];
    int loopcf = 0;
    for (final double time : flow.keySet()) {
      agregatedCfe[loopcf++] = new PaymentFixed(ccy, time, flow.get(time));
    }
    return new AnnuityPaymentFixed(agregatedCfe);
  }

  @Override
  public AnnuityPaymentFixed visitFixedCouponSwap(final SwapFixedCoupon<?> swap) {
    return visitSwap(swap);
  }

  /**
   * Add a cash flow amount at a given time in the flow map. If the time is present, the amount is added; if the time is not present a new entry is created.
   * @param flow The map describing the cash flows.
   * @param time The time of the flow to add.
   * @param amount The amount of the flow to add.
   */
  private static void addcf(final TreeMap<Double, Double> flow, final double time, final double amount) {
    if (flow.containsKey(time)) {
      flow.put(time, flow.get(time) + amount);
    } else {
      flow.put(time, amount);
    }
  }

}
