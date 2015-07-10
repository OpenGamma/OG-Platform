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
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Compute the cash flow equivalent of simple instruments (in single or multi-curve framework).
 * The cash-flow equivalent have at most one payment by time and the times are sorted in ascending order.
 * Reference: Henrard, M. The Irony in the derivatives discounting Part II: the crisis. Wilmott Journal, 2010, 2, 301-316
 */
public class CashFlowEquivalentCalculator extends InstrumentDerivativeVisitorAdapter<MulticurveProviderInterface, AnnuityPaymentFixed> {

  /**
   * The unique instance of the calculator.
   */
  private static final CashFlowEquivalentCalculator s_instance = new CashFlowEquivalentCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static CashFlowEquivalentCalculator getInstance() {
    return s_instance;
  }

  /**
   * Constructor.
   */
  CashFlowEquivalentCalculator() {
  }

  @Override
  public AnnuityPaymentFixed visitFixedPayment(final PaymentFixed payment, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(payment, "Payment");
    return new AnnuityPaymentFixed(new PaymentFixed[] {payment});
  }

  @Override
  public AnnuityPaymentFixed visitCouponFixed(final CouponFixed coupon, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(coupon, "Coupon");
    return new AnnuityPaymentFixed(new PaymentFixed[] {coupon.toPaymentFixed()});
  }

  @Override
  public AnnuityPaymentFixed visitCouponIbor(final CouponIbor payment, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(payment, "Payment");
    ArgumentChecker.notNull(multicurves, "Multicurves provider");
    final Currency ccy = payment.getCurrency();
    final double fixingStartTime = payment.getFixingPeriodStartTime();
    final double fixingEndTime = payment.getFixingPeriodEndTime();
    final double paymentTime = payment.getPaymentTime();
    final double beta = (1.0 + payment.getFixingAccrualFactor() * multicurves.getSimplyCompoundForwardRate(payment.getIndex(), fixingStartTime, fixingEndTime, payment.getFixingAccrualFactor()))
        * multicurves.getDiscountFactor(ccy, paymentTime) / multicurves.getDiscountFactor(ccy, fixingStartTime);
    final PaymentFixed paymentStart = new PaymentFixed(payment.getCurrency(), fixingStartTime, beta * payment.getNotional() * payment.getPaymentYearFraction() / payment.getFixingAccrualFactor());
    final PaymentFixed paymentEnd = new PaymentFixed(payment.getCurrency(), paymentTime, -payment.getNotional() * payment.getPaymentYearFraction() / payment.getFixingAccrualFactor());
    return new AnnuityPaymentFixed(new PaymentFixed[] {paymentStart, paymentEnd});
  }

  @Override
  public AnnuityPaymentFixed visitCouponIborSpread(final CouponIborSpread payment, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(payment, "Payment");
    ArgumentChecker.notNull(multicurves, "Multicurves provider");
    final Currency ccy = payment.getCurrency();
    final double fixingStartTime = payment.getFixingPeriodStartTime();
    final double fixingEndTime = payment.getFixingPeriodEndTime();
    final double paymentTime = payment.getPaymentTime();
    final double beta = (1.0 + payment.getFixingAccrualFactor() * multicurves.getSimplyCompoundForwardRate(payment.getIndex(), fixingStartTime, fixingEndTime, payment.getFixingAccrualFactor()))
        * multicurves.getDiscountFactor(ccy, paymentTime) / multicurves.getDiscountFactor(ccy, fixingStartTime);
    final PaymentFixed paymentStart = new PaymentFixed(payment.getCurrency(), fixingStartTime, beta * payment.getNotional() * payment.getPaymentYearFraction() / payment.getFixingAccrualFactor());
    final PaymentFixed paymentEnd = new PaymentFixed(payment.getCurrency(), paymentTime, (-payment.getNotional() + payment.getSpreadAmount()) * payment.getPaymentYearFraction()
        / payment.getFixingAccrualFactor());
    return new AnnuityPaymentFixed(new PaymentFixed[] {paymentStart, paymentEnd});
  }

  @Override
  public AnnuityPaymentFixed visitCouponIborGearing(final CouponIborGearing payment, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(payment, "Payment");
    ArgumentChecker.notNull(multicurves, "Multicurves provider");
    final Currency ccy = payment.getCurrency();
    final double fixingStartTime = payment.getFixingPeriodStartTime();
    final double fixingEndTime = payment.getFixingPeriodEndTime();
    final double paymentTime = payment.getPaymentTime();
    final double beta = (1.0 + payment.getFixingAccrualFactor() * multicurves.getSimplyCompoundForwardRate(payment.getIndex(), fixingStartTime, fixingEndTime, payment.getFixingAccrualFactor()))
        * multicurves.getDiscountFactor(ccy, paymentTime) / multicurves.getDiscountFactor(ccy, fixingStartTime);
    final PaymentFixed paymentStart = new PaymentFixed(payment.getCurrency(), fixingStartTime, payment.getFactor() * beta * payment.getNotional() * payment.getPaymentYearFraction()
        / payment.getFixingAccrualFactor());
    final PaymentFixed paymentEnd = new PaymentFixed(payment.getCurrency(), paymentTime, (-payment.getFactor() / payment.getFixingAccrualFactor() + payment.getSpread()) *
        payment.getPaymentYearFraction() * payment.getNotional());
    return new AnnuityPaymentFixed(new PaymentFixed[] {paymentStart, paymentEnd});
  }

  @Override
  public AnnuityPaymentFixed visitGenericAnnuity(final Annuity<? extends Payment> annuity, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(annuity, "Annuity");
    ArgumentChecker.notNull(multicurves, "Multicurves provider");
    final TreeMap<Double, Double> flow = new TreeMap<>();
    final Currency ccy = annuity.getCurrency();
    for (final Payment p : annuity.getPayments()) {
      final AnnuityPaymentFixed cfe = p.accept(this, multicurves);
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

  @Override
  public AnnuityPaymentFixed visitFixedCouponAnnuity(final AnnuityCouponFixed annuity, final MulticurveProviderInterface multicurves) {
    return visitGenericAnnuity(annuity, multicurves);
  }

  @Override
  public AnnuityPaymentFixed visitSwap(final Swap<?, ?> swap, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(swap, "Swap");
    ArgumentChecker.notNull(multicurves, "Multicurves provider");
    final Currency ccy = swap.getFirstLeg().getCurrency();
    Validate.isTrue(ccy.equals(swap.getSecondLeg().getCurrency()), "Cash flow equivalent available only for single currency swaps.");
    final TreeMap<Double, Double> flow = new TreeMap<>();
    final AnnuityPaymentFixed cfeLeg1 = swap.getFirstLeg().accept(this, multicurves);
    final AnnuityPaymentFixed cfeLeg2 = swap.getSecondLeg().accept(this, multicurves);
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
  public AnnuityPaymentFixed visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final MulticurveProviderInterface multicurves) {
    return visitSwap(swap, multicurves);
  }

  @Override
  public AnnuityPaymentFixed visitBondFixedSecurity(final BondFixedSecurity bond, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(bond, "Bond");
    ArgumentChecker.notNull(multicurves, "Multicurves provider");
    final Currency ccy = bond.getCurrency();
    final TreeMap<Double, Double> flow = new TreeMap<>();
    final AnnuityPaymentFixed cfeNom = bond.getNominal().accept(this, multicurves);
    final AnnuityPaymentFixed cfeCpn = bond.getCoupon().accept(this, multicurves);
    for (final PaymentFixed p : cfeNom.getPayments()) {
      flow.put(p.getPaymentTime(), p.getAmount());
    }
    for (final PaymentFixed p : cfeCpn.getPayments()) {
      addcf(flow, p.getPaymentTime(), p.getAmount());
    }
    final PaymentFixed[] agregatedCfe = new PaymentFixed[flow.size()];
    int loopcf = 0;
    for (final double time : flow.keySet()) {
      agregatedCfe[loopcf++] = new PaymentFixed(ccy, time, flow.get(time));
    }
    return new AnnuityPaymentFixed(agregatedCfe);

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
