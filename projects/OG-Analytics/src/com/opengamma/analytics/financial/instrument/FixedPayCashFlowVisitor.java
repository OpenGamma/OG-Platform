/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.time.calendar.LocalDate;

import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexNonDeliverableForwardDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborSpreadDefinition;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Returns all of the known cash-flows to be paid of an instrument from a particular date. If there are no known cash-flows, an empty map is returned.
 * The payments are always positive.
 */
public final class FixedPayCashFlowVisitor extends AbstractInstrumentDefinitionVisitor<LocalDate, Map<LocalDate, MultipleCurrencyAmount>> {
  private static final FixedPayCashFlowVisitor INSTANCE = new FixedPayCashFlowVisitor();

  public static FixedPayCashFlowVisitor getInstance() {
    return INSTANCE;
  }

  private FixedPayCashFlowVisitor() {
  }

  /**
   * The default behaviour of this visitor is to return an empty map.
   * @param definition The instrument definition
   * @param fromDate The date from which to list the cash-flows
   * @return An empty map
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visit(final InstrumentDefinition<?> definition, final LocalDate fromDate) {
    return Collections.emptyMap();
  }

  /**
   * If the date is after the maturity of the instrument, or if the notional is positive (i.e. an amount is to be received), returns an empty map.
   * Otherwise, returns a map containing a single payment date and amount to be paid.
   * @param cash The cash definition, not null
   * @param fromDate The date from which to calculate the cash flow, not null
   * @return A map containing the (single) payment date and amount
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitCashDefinition(final CashDefinition cash, final LocalDate fromDate) {
    ArgumentChecker.notNull(cash, "cash");
    ArgumentChecker.notNull(fromDate, "date");
    if (cash.getNotional() > 0) {
      return Collections.emptyMap();
    }
    final LocalDate endDate = cash.getEndDate().toLocalDate();
    if (endDate.isBefore(fromDate)) {
      return Collections.emptyMap();
    }
    return Collections.singletonMap(endDate, MultipleCurrencyAmount.of(cash.getCurrency(), Math.abs(cash.getInterestAmount())));
  }

  /**
   * If the date is after the maturity of the payment, or if the notional is positive (i.e. the payment will be received), returns an empty map.
   * Otherwise, returns a map containing a single payment date and amount to be paid.
   * @param payment The payment, not null
   * @param fromDate The date from which to calculate the cash flow, not null
   * @return A map containing the (single) payment date and amount
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitPaymentFixedDefinition(final PaymentFixedDefinition payment, final LocalDate fromDate) {
    ArgumentChecker.notNull(payment, "payment");
    ArgumentChecker.notNull(fromDate, "date");
    if (payment.getReferenceAmount() > 0) {
      return Collections.emptyMap();
    }
    final LocalDate endDate = payment.getPaymentDate().toLocalDate();
    if (endDate.isBefore(fromDate)) {
      return Collections.emptyMap();
    }
    return Collections.singletonMap(endDate, MultipleCurrencyAmount.of(payment.getCurrency(), Math.abs(payment.getReferenceAmount())));
  }

  /**
   * If the date is after the payment date of the coupon, or if the notional is positive (i.e. the coupon will be received), returns an empty map.
   * Otherwise, returns a map containing a single payment date and amount to be paid.
   * @param coupon The fixed coupon, not null
   * @param fromDate The date from which to calculate the cash flow, not null
   * @return A map containing the (single) payment date and amount
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitCouponFixedDefinition(final CouponFixedDefinition coupon, final LocalDate fromDate) {
    ArgumentChecker.notNull(coupon, "payment");
    ArgumentChecker.notNull(fromDate, "date");
    if (coupon.getNotional() > 0) {
      return Collections.emptyMap();
    }
    final LocalDate endDate = coupon.getPaymentDate().toLocalDate();
    if (endDate.isBefore(fromDate)) {
      return Collections.emptyMap();
    }
    return Collections.singletonMap(endDate, MultipleCurrencyAmount.of(coupon.getCurrency(), Math.abs(coupon.getAmount())));
  }

  /**
   * If the date is after the payment date or if the FRA is a receiver, returns an empty map. Otherwise, returns a map containing a single payment date
   * and the amount to be paid.
   * @param forwardRateAgreement The FRA, not null
   * @param fromDate The date from which to calculate the cash flow, not null
   * @return A map containing the (single) payment date and amount
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitForwardRateAgreementDefinition(final ForwardRateAgreementDefinition forwardRateAgreement, final LocalDate fromDate) {
    ArgumentChecker.notNull(forwardRateAgreement, "FRA");
    ArgumentChecker.notNull(fromDate, "date");
    if (forwardRateAgreement.getNotional() < 0) {
      return Collections.emptyMap();
    }
    final LocalDate endDate = forwardRateAgreement.getPaymentDate().toLocalDate();
    if (endDate.isBefore(fromDate)) {
      return Collections.emptyMap();
    }
    final double payment = forwardRateAgreement.getReferenceAmount() * forwardRateAgreement.getRate() * forwardRateAgreement.getFixingPeriodAccrualFactor();
    return Collections.singletonMap(endDate, MultipleCurrencyAmount.of(forwardRateAgreement.getCurrency(), payment));
  }

  /**
   * Returns a map containing all of the known payments in an annuity after a certain date. If there are no payments after this date, an empty map is returned.
   * @param annuity The annuity, not null
   * @param fromDate The date from which to calculate the cash flows, not null
   * @return A map containing the payment dates and amounts
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity, final LocalDate fromDate) {
    ArgumentChecker.notNull(annuity, "annuity");
    ArgumentChecker.notNull(fromDate, "date");
    return getDatesAndPaymentsFromAnnuity(annuity, fromDate);
  }

  /**
   * If the swap is a payer, returns a map containing all of the fixed payments after a certain date. If there are no payments after this date, an empty map
   * is returned.<p> 
   * @param swap The swap, not null
   * @param fromDate The date from which to calculate the cash flows, not null
   * @return A map containing the payment dates and amounts
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitSwapFixedIborDefinition(final SwapFixedIborDefinition swap, final LocalDate fromDate) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(fromDate, "date");
    if (swap.getFixedLeg().isPayer()) {
      return (swap.getFixedLeg().accept(this, fromDate));
    }
    return Collections.emptyMap();
  }

  /**
   * If the swap is a payer, returns a map containing all of the fixed payments after a certain date. If there are no payments after this date, an empty map
   * is returned.<p> 
   * @param swap The swap, not null
   * @param fromDate The date from which to calculate the cash flows, not null
   * @return A map containing the payment dates and amounts
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitSwapFixedIborSpreadDefinition(final SwapFixedIborSpreadDefinition swap, final LocalDate fromDate) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(fromDate, "date");
    if (swap.getFixedLeg().isPayer()) {
      return swap.getFixedLeg().accept(this, fromDate);
    }
    return Collections.emptyMap();
  }

  /**
   * If the date is after the maturity of the instrument, returns an empty map. Otherwise, returns a map containing a single date and payment.
   * @param fx The FX instrument, not null
   * @param fromDate The date from which to calculate the cash flows, not null
   * @return A map containing the (single) payment date and amount
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitForexDefinition(final ForexDefinition fx, final LocalDate fromDate) {
    ArgumentChecker.notNull(fx, "fx");
    ArgumentChecker.notNull(fromDate, "date");
    if (fx.getPaymentCurrency1().getReferenceAmount() < 0) {
      return fx.getPaymentCurrency1().accept(this, fromDate);
    }
    return fx.getPaymentCurrency2().accept(this, fromDate);
  }

  /**
   * If the date is after the maturity of the instrument, returns an empty map. Otherwise, returns a map containing a single date and payment.
   * @param ndf The NDF, not null
   * @param fromDate The date from which to calculate the cash flows, not null
   * @return A map containing the (single) payment date and amount
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitForexNonDeliverableForwardDefinition(final ForexNonDeliverableForwardDefinition ndf, final LocalDate fromDate) {
    ArgumentChecker.notNull(ndf, "ndf");
    ArgumentChecker.notNull(fromDate, "date");
    if (ndf.getNotional() > 0) {
      return Collections.emptyMap();
    }
    final LocalDate endDate = ndf.getPaymentDate().toLocalDate();
    if (endDate.isBefore(fromDate)) {
      return Collections.emptyMap();
    }
    return Collections.singletonMap(endDate, MultipleCurrencyAmount.of(ndf.getCurrency2(), Math.abs(ndf.getNotional())));
  }

  private Map<LocalDate, MultipleCurrencyAmount> getDatesAndPaymentsFromAnnuity(final AnnuityDefinition<? extends PaymentDefinition> annuity, final LocalDate fromDate) {
    final Map<LocalDate, MultipleCurrencyAmount> result = new HashMap<LocalDate, MultipleCurrencyAmount>();
    final FixedPaymentVisitor fixedPaymentVisitor = FixedPaymentVisitor.getInstance();
    for (final PaymentDefinition payment : annuity.getPayments()) {
      if (payment.accept(fixedPaymentVisitor)) {
        final LocalDate paymentDate = payment.getPaymentDate().toLocalDate();
        if (!paymentDate.isBefore(fromDate)) {
          final Map<LocalDate, MultipleCurrencyAmount> payments = payment.accept(this, fromDate);
          for (final Map.Entry<LocalDate, MultipleCurrencyAmount> entry : payments.entrySet()) {
            final int scale = entry.getValue().getCurrencyAmounts()[0].getAmount() < 0 ? -1 : 1;
            final MultipleCurrencyAmount mca = entry.getValue().multipliedBy(scale);
            final LocalDate key = entry.getKey();
            if (result.containsKey(key)) {
              result.put(key, result.get(key).plus(mca));
            } else {
              result.put(key, mca);
            }
          }
        }
      }
    }
    return result;
  }

  private static final class FixedPaymentVisitor extends AbstractInstrumentDefinitionVisitor<Object, Boolean> {
    private static final FixedPaymentVisitor s_instance = new FixedPaymentVisitor();

    static FixedPaymentVisitor getInstance() {
      return s_instance;
    }

    private FixedPaymentVisitor() {
    }

    @Override
    public Boolean visit(final InstrumentDefinition<?> definition) {
      return false;
    }

    @Override
    public Boolean visitPaymentFixedDefinition(final PaymentFixedDefinition payment) {
      return true;
    }

    @Override
    public Boolean visitCouponFixedDefinition(final CouponFixedDefinition coupon) {
      return true;
    }
  }
}
