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
import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexNonDeliverableForwardDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborSpreadDefinition;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * Returns all of the known pay cash-flows, including floating payments that have fixed. If there are no known cash-flows, an empty map is returned.
 * The payments are always positive.
 */
public final class FixedPayCashFlowVisitor extends AbstractInstrumentDefinitionVisitor<DoubleTimeSeries<ZonedDateTime>, Map<LocalDate, MultipleCurrencyAmount>> {
  private static final FixedPayCashFlowVisitor INSTANCE = new FixedPayCashFlowVisitor();

  public static FixedPayCashFlowVisitor getInstance() {
    return INSTANCE;
  }

  private FixedPayCashFlowVisitor() {
  }

  /**
   * The default behaviour of this visitor is to return an empty map.
   * @param definition The instrument definition
   * @return An empty map
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visit(final InstrumentDefinition<?> definition, final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries) {
    return Collections.emptyMap();
  }

  /**
   * If the notional is positive (i.e. an amount is to be received), returns an empty map.
   * Otherwise, returns a map containing a single payment date and amount to be paid.
   * @param cash The cash definition, not null
   * @return A map containing the (single) payment date and amount, or an empty map, as appropriate
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitCashDefinition(final CashDefinition cash, final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries) {
    ArgumentChecker.notNull(cash, "cash");
    if (cash.getNotional() > 0) {
      return Collections.emptyMap();
    }
    final LocalDate endDate = cash.getEndDate().toLocalDate();
    return Collections.singletonMap(endDate, MultipleCurrencyAmount.of(cash.getCurrency(), Math.abs(cash.getInterestAmount())));
  }

  /**
   * If the notional is positive (i.e. the payment will be received), returns an empty map.
   * Otherwise, returns a map containing a single payment date and amount to be paid.
   * @param payment The payment, not null
   * @return A map containing the (single) payment date and amount
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitPaymentFixedDefinition(final PaymentFixedDefinition payment,
      final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries) {
    ArgumentChecker.notNull(payment, "payment");
    if (payment.getReferenceAmount() > 0) {
      return Collections.emptyMap();
    }
    final LocalDate endDate = payment.getPaymentDate().toLocalDate();
    return Collections.singletonMap(endDate, MultipleCurrencyAmount.of(payment.getCurrency(), Math.abs(payment.getReferenceAmount())));
  }

  /**
   * If the notional is positive (i.e. the coupon will be received), returns an empty map.
   * Otherwise, returns a map containing a single payment date and amount to be paid.
   * @param coupon The fixed coupon, not null
   * @return A map containing the (single) payment date and amount
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitCouponFixedDefinition(final CouponFixedDefinition coupon, final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries) {
    ArgumentChecker.notNull(coupon, "payment");
    if (coupon.getNotional() > 0) {
      return Collections.emptyMap();
    }
    final LocalDate endDate = coupon.getPaymentDate().toLocalDate();
    return Collections.singletonMap(endDate, MultipleCurrencyAmount.of(coupon.getCurrency(), Math.abs(coupon.getAmount())));
  }

  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitCouponIborDefinition(final CouponIborDefinition coupon, final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries) {

  }

  /**
   * If the FRA is a payer, or if the FRA is a receiver and a value for the fixing date is present in the index fixing time series 
   * (i.e. the presence of that data implies the fixing has taken place), returns a map containing a single payment
   * @param forwardRateAgreement The FRA, not null
   * @param indexFixingTimeSeries The fixing time series for the floating index, not null
   * @return A map containing the (single) payment date and amount
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitForwardRateAgreementDefinition(final ForwardRateAgreementDefinition forwardRateAgreement,
      final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries) {
    ArgumentChecker.notNull(forwardRateAgreement, "FRA");
    ArgumentChecker.notNull(indexFixingTimeSeries, "index fixing time series");
    final LocalDate endDate = forwardRateAgreement.getPaymentDate().toLocalDate();
    if (forwardRateAgreement.getNotional() < 0) {
      final ZonedDateTime fixingDate = forwardRateAgreement.getFixingDate().withTime(11, 0);
      if (!indexFixingTimeSeries.getLatestTime().isBefore(fixingDate)) {
        final double fixedRate = indexFixingTimeSeries.getValue(fixingDate);
        final double payment = forwardRateAgreement.getPaymentYearFraction() * forwardRateAgreement.getNotional() * fixedRate;
        return Collections.singletonMap(endDate, MultipleCurrencyAmount.of(forwardRateAgreement.getCurrency(), payment));
      }
      return Collections.emptyMap();
    }
    final double payment = forwardRateAgreement.getReferenceAmount() * forwardRateAgreement.getRate() * forwardRateAgreement.getFixingPeriodAccrualFactor();
    return Collections.singletonMap(endDate, MultipleCurrencyAmount.of(forwardRateAgreement.getCurrency(), payment));
  }

  /**
   * Returns a map containing all of the known payments in an annuity after a certain date. If there are no payments after this date, an empty map is returned.
   * @param annuity The annuity, not null
   * @param indexFixingTimeSeries The fixing time series for the floating index, not null
   * @return A map containing the payment dates and amounts
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity,
      final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries) {
    ArgumentChecker.notNull(annuity, "annuity");
    ArgumentChecker.notNull(indexFixingTimeSeries, "index fixing time series");
    return getDatesAndPaymentsFromAnnuity(annuity, indexFixingTimeSeries);
  }

  /**
   * If the swap is a payer, returns a map containing all of the fixed payments. If the swap is a receiver, returns a map containing
   * all of the payment amounts that have been fixed.
   * @param swap The swap, not null
   * @param indexFixingTimeSeries The fixing time series for the floating index, not null
   * @return A map containing the payment dates and amounts
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitSwapFixedIborDefinition(final SwapFixedIborDefinition swap,
      final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(indexFixingTimeSeries, "index fixing time series");
    if (swap.getFixedLeg().isPayer()) {
      return swap.getFixedLeg().accept(this, indexFixingTimeSeries);
    }
    return swap.getIborLeg().accept(this, indexFixingTimeSeries);
  }

  /**
   * If the swap is a payer, returns a map containing all of the fixed payments. If the swap is a receiver, returns a map containing
   * all of the payments amounts that have been fixed.
   * @param swap The swap, not null
   * @param indexFixingTimeSeries The fixing time series for the floating index, not null
   * @return A map containing the payment dates and amounts
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitSwapFixedIborSpreadDefinition(final SwapFixedIborSpreadDefinition swap,
      final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(indexFixingTimeSeries, "index fixing time series");
    if (swap.getFixedLeg().isPayer()) {
      return swap.getFixedLeg().accept(this, indexFixingTimeSeries);
    }
    return swap.getIborLeg().accept(this, indexFixingTimeSeries);
  }

  /**
   * Returns a map containing a single date and payment.
   * @param fx The FX instrument, not null
   * @return A map containing the (single) payment date and amount
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitForexDefinition(final ForexDefinition fx, final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries) {
    ArgumentChecker.notNull(fx, "fx");
    if (fx.getPaymentCurrency1().getReferenceAmount() < 0) {
      return fx.getPaymentCurrency1().accept(this);
    }
    return fx.getPaymentCurrency2().accept(this);
  }

  /**
   * If the cash settlement amount is negative (i.e. it must be paid), returns a map containing a single date and payment. Otherwise, returns
   * an empty map. 
   * @param ndf The NDF, not null
   * @return A map containing the (single) payment date and amount
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitForexNonDeliverableForwardDefinition(final ForexNonDeliverableForwardDefinition ndf) {
    ArgumentChecker.notNull(ndf, "ndf");
    if (ndf.getNotional() > 0) {
      return Collections.emptyMap();
    }
    final LocalDate endDate = ndf.getPaymentDate().toLocalDate();
    return Collections.singletonMap(endDate, MultipleCurrencyAmount.of(ndf.getCurrency2(), Math.abs(ndf.getNotional())));
  }

  private Map<LocalDate, MultipleCurrencyAmount> getDatesAndPaymentsFromAnnuity(final AnnuityDefinition<? extends PaymentDefinition> annuity,
      final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries) {
    final Map<LocalDate, MultipleCurrencyAmount> result = new HashMap<LocalDate, MultipleCurrencyAmount>();
    final FixedPaymentVisitor fixedPaymentVisitor = FixedPaymentVisitor.getInstance();
    for (final PaymentDefinition payment : annuity.getPayments()) {
      if (payment.accept(fixedPaymentVisitor)) {
        final Map<LocalDate, MultipleCurrencyAmount> payments = payment.accept(this, indexFixingTimeSeries);
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
