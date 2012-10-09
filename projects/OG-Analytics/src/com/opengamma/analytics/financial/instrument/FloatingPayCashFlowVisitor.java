/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import java.util.Collections;
import java.util.Map;

import javax.time.calendar.LocalDate;

import com.opengamma.analytics.financial.instrument.cash.DepositIborDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponOISDefinition;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Returns all of the floating cash-flows of an instrument. The notionals returned are those that will be paid 
 * (i.e. a semi-annual swap with a notional of $1MM will return notionals of ~$0.5MM)
 */
public final class FloatingPayCashFlowVisitor extends AbstractInstrumentDefinitionVisitor<Object, Map<LocalDate, MultipleCurrencyAmount>> {
  private static final FloatingPayCashFlowVisitor INSTANCE = new FloatingPayCashFlowVisitor();

  public static FloatingPayCashFlowVisitor getInstance() {
    return INSTANCE;
  }

  private FloatingPayCashFlowVisitor() {
  }

  /**
   * @param definition The instrument definition, not null
   * @return An empty map
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visit(final InstrumentDefinition<?> definition) {
    ArgumentChecker.notNull(definition, "definition");
    return definition.accept(this);
  }

  /**
   * If the notional is positive (i.e. the amount is to be received) returns
   * an empty map. Otherwise, returns a map containing a single payment date and the notional amount 
   * multiplied by the accrual period. 
   * @param deposit The deposit instrument, not null
   * @return A map containing the (single) payment date and amount, or an empty map, as appropriate
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitDepositIborDefinition(final DepositIborDefinition deposit) {
    ArgumentChecker.notNull(deposit, "deposit");
    final LocalDate endDate = deposit.getEndDate().toLocalDate();
    if (deposit.getNotional() > 0) {
      return Collections.emptyMap();
    }
    final double amount = -deposit.getNotional() * deposit.getAccrualFactor();
    return Collections.singletonMap(endDate, MultipleCurrencyAmount.of(CurrencyAmount.of(deposit.getCurrency(), amount)));
  }

  /**
   * If the notional is positive (i.e. the amount is to be received), returns
   * an empty map. Otherwise, returns a map containing a single payment date and the notional amount 
   * multiplied by the accrual period.
   * @param coupon The coupon instrument, not null
   * @return A map containing the (single) payment date and amount, or an empty map, as appropriate
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitCouponIborDefinition(final CouponIborDefinition coupon) {
    ArgumentChecker.notNull(coupon, "coupon");
    final LocalDate endDate = coupon.getPaymentDate().toLocalDate();
    if (coupon.getNotional() > 0) {
      return Collections.emptyMap();
    }
    final double amount = -coupon.getNotional() * coupon.getFixingPeriodAccrualFactor();
    return Collections.singletonMap(endDate, MultipleCurrencyAmount.of(CurrencyAmount.of(coupon.getCurrency(), amount)));
  }

  /**
   * If the notional is positive (i.e. the amount is to be received), returns
   * an empty map. Otherwise, returns a map containing a single payment date and the notional amount 
   * multiplied by the accrual period.
   * @param coupon The coupon instrument, not null
   * @return A map containing the (single) payment date and amount, or an empty map, as appropriate
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitCouponIborSpreadDefinition(final CouponIborSpreadDefinition coupon) {
    ArgumentChecker.notNull(coupon, "coupon");
    final LocalDate endDate = coupon.getPaymentDate().toLocalDate();
    if (coupon.getNotional() > 0) {
      return Collections.emptyMap();
    }
    final double amount = -coupon.getNotional() * coupon.getFixingPeriodAccrualFactor();
    return Collections.singletonMap(endDate, MultipleCurrencyAmount.of(CurrencyAmount.of(coupon.getCurrency(), amount)));
  }

  /**
   * If the notional is positive (i.e. the amount is to be received), returns
   * an empty map. Otherwise, returns a map containing a single payment date and the notional amount 
   * multiplied by the accrual period.
   * @param coupon The coupon instrument, not null
   * @return A map containing the (single) payment date and amount, or an empty map, as appropriate
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitCouponIborGearingDefinition(final CouponIborGearingDefinition coupon) {
    ArgumentChecker.notNull(coupon, "coupon");
    final LocalDate endDate = coupon.getPaymentDate().toLocalDate();
    if (coupon.getNotional() > 0) {
      return Collections.emptyMap();
    }
    final double amount = -coupon.getNotional() * coupon.getFixingPeriodAccrualFactor() * coupon.getFactor();
    return Collections.singletonMap(endDate, MultipleCurrencyAmount.of(CurrencyAmount.of(coupon.getCurrency(), amount)));
  }

  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitForwardRateAgreementDefinition(final ForwardRateAgreementDefinition forwardRateAgreement) {
    ArgumentChecker.notNull(forwardRateAgreement, "FRA");
    final LocalDate endDate = forwardRateAgreement.getPaymentDate().toLocalDate();
    if (forwardRateAgreement.getNotional() < 0) {
      return Collections.emptyMap();
    }
    return Collections.singletonMap(endDate, MultipleCurrencyAmount.of(CurrencyAmount.of(forwardRateAgreement.getCurrency(), forwardRateAgreement.getNotional())));
  }

  //  @Override
  //  public Map<LocalDate, MultipleCurrencyAmount> visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity) {
  //    ArgumentChecker.notNull(annuity, "annuity");
  //    ArgumentChecker.notNull(fromDate, "date");
  //    return getDatesFromAnnuity(annuity, fromDate);
  //  }
  //
  //  @Override
  //  public Map<LocalDate, MultipleCurrencyAmount> visitSwapDefinition(final SwapDefinition swap) {
  //    ArgumentChecker.notNull(swap, "swap");
  //    ArgumentChecker.notNull(fromDate, "date");
  //    return swap.getFirstLeg().isPayer() ? swap.getFirstLeg().accept(this, fromDate) : swap.getSecondLeg().accept(this, fromDate);
  //  }
  //
  //  @Override
  //  public Map<LocalDate, MultipleCurrencyAmount> visitSwapFixedIborDefinition(final SwapFixedIborDefinition swap) {
  //    ArgumentChecker.notNull(swap, "swap");
  //    ArgumentChecker.notNull(fromDate, "date");
  //    if (swap.getIborLeg().isPayer()) {
  //      return swap.getIborLeg().accept(this, fromDate);
  //    }
  //    return Collections.emptyMap();
  //  }
  //
  //  @Override
  //  public Map<LocalDate, MultipleCurrencyAmount> visitSwapFixedIborSpreadDefinition(final SwapFixedIborSpreadDefinition swap) {
  //    ArgumentChecker.notNull(swap, "swap");
  //    ArgumentChecker.notNull(fromDate, "date");
  //    if (swap.getIborLeg().isPayer()) {
  //      return swap.getIborLeg().accept(this, fromDate);
  //    }
  //    return Collections.emptyMap();
  //  }
  //
  //  @Override
  //  public Map<LocalDate, MultipleCurrencyAmount> visitSwapIborIborDefinition(final SwapIborIborDefinition swap) {
  //    ArgumentChecker.notNull(swap, "swap");
  //    ArgumentChecker.notNull(fromDate, "date");
  //    if (swap.getFirstLeg().isPayer()) {
  //      return swap.getFirstLeg().accept(this, fromDate);
  //    }
  //    return swap.getSecondLeg().accept(this, fromDate);
  //  }
  //
  //  @Override
  //  public Map<LocalDate, MultipleCurrencyAmount> visitSwapXCcyIborIborDefinition(final SwapXCcyIborIborDefinition swap) {
  //    ArgumentChecker.notNull(swap, "swap");
  //    ArgumentChecker.notNull(fromDate, "date");
  //    if (swap.getFirstLeg().isPayer()) {
  //      return swap.getFirstLeg().accept(this, fromDate);
  //    }
  //    return swap.getSecondLeg().accept(this, fromDate);
  //  }
  //
  //  private Map<LocalDate, MultipleCurrencyAmount> getDatesFromAnnuity(final AnnuityDefinition<? extends PaymentDefinition> annuity) {
  //    final Map<LocalDate, MultipleCurrencyAmount> result = new HashMap<LocalDate, MultipleCurrencyAmount>();
  //    final FloatingPaymentVisitor floatingPaymentVisitor = FloatingPaymentVisitor.getInstance();
  //    for (final PaymentDefinition payment : annuity.getPayments()) {
  //      if (payment.accept(floatingPaymentVisitor)) {
  //        final LocalDate paymentDate = payment.getPaymentDate().toLocalDate();
  //        if (!paymentDate.isBefore(fromDate)) {
  //          final Map<LocalDate, MultipleCurrencyAmount> payments = payment.accept(this, fromDate);
  //          for (final Map.Entry<LocalDate, MultipleCurrencyAmount> entry : payments.entrySet()) {
  //            result.put(entry.getKey(), entry.getValue());
  //          }
  //        }
  //      }
  //    }
  //    return result;
  //  }

  private static final class FloatingPaymentVisitor extends AbstractInstrumentDefinitionVisitor<Object, Boolean> {
    private static final FloatingPaymentVisitor s_instance = new FloatingPaymentVisitor();

    static FloatingPaymentVisitor getInstance() {
      return s_instance;
    }

    private FloatingPaymentVisitor() {
    }

    @Override
    public Boolean visit(final InstrumentDefinition<?> definition) {
      return false;
    }

    @Override
    public Boolean visitCouponCMSDefinition(final CouponCMSDefinition definition) {
      return true;
    }

    @Override
    public Boolean visitCouponIborDefinition(final CouponIborDefinition definition) {
      return true;
    }

    @Override
    public Boolean visitCouponOISDefinition(final CouponOISDefinition definition) {
      return true;
    }
  }
}
