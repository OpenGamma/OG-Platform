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

import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositIborDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborRatchetDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponOISDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapIborIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapXCcyIborIborDefinition;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * 
 */
public final class FloatingCashFlowVisitor extends AbstractInstrumentDefinitionVisitor<LocalDate, Map<LocalDate, MultipleCurrencyAmount>> {
  private static final FloatingCashFlowVisitor INSTANCE = new FloatingCashFlowVisitor();

  public static FloatingCashFlowVisitor getInstance() {
    return INSTANCE;
  }

  private FloatingCashFlowVisitor() {
  }

  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visit(final InstrumentDefinition<?> definition, final LocalDate fromDate) {
    return Collections.emptyMap();
  }

  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitDepositIborDefinition(final DepositIborDefinition deposit, final LocalDate fromDate) {
    ArgumentChecker.notNull(deposit, "deposit");
    ArgumentChecker.notNull(fromDate, "date");
    final LocalDate endDate = deposit.getEndDate().toLocalDate();
    if (endDate.isBefore(fromDate)) {
      return Collections.emptyMap();
    }
    return Collections.singletonMap(endDate, MultipleCurrencyAmount.of(CurrencyAmount.of(deposit.getCurrency(), deposit.getNotional())));
  }

  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitCouponIborDefinition(final CouponIborDefinition coupon, final LocalDate fromDate) {
    ArgumentChecker.notNull(coupon, "coupon");
    ArgumentChecker.notNull(fromDate, "date");
    final LocalDate endDate = coupon.getPaymentDate().toLocalDate();
    if (endDate.isBefore(fromDate)) {
      return Collections.emptyMap();
    }
    return Collections.singletonMap(endDate, MultipleCurrencyAmount.of(CurrencyAmount.of(coupon.getCurrency(), coupon.getNotional())));
  }

  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitCouponIborSpreadDefinition(final CouponIborSpreadDefinition coupon, final LocalDate fromDate) {
    ArgumentChecker.notNull(coupon, "coupon");
    ArgumentChecker.notNull(fromDate, "date");
    final LocalDate endDate = coupon.getPaymentDate().toLocalDate();
    if (endDate.isBefore(fromDate)) {
      return Collections.emptyMap();
    }
    return Collections.singletonMap(endDate, MultipleCurrencyAmount.of(CurrencyAmount.of(coupon.getCurrency(), coupon.getNotional())));
  }

  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitCouponIborGearingDefinition(final CouponIborGearingDefinition coupon, final LocalDate fromDate) {
    ArgumentChecker.notNull(coupon, "coupon");
    ArgumentChecker.notNull(fromDate, "date");
    final LocalDate endDate = coupon.getPaymentDate().toLocalDate();
    if (endDate.isBefore(fromDate)) {
      return Collections.emptyMap();
    }
    return Collections.singletonMap(endDate, MultipleCurrencyAmount.of(CurrencyAmount.of(coupon.getCurrency(), coupon.getNotional())));
  }

  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitCouponIborCompoundedDefinition(final CouponIborCompoundedDefinition coupon, final LocalDate fromDate) {
    ArgumentChecker.notNull(coupon, "coupon");
    ArgumentChecker.notNull(fromDate, "date");
    final LocalDate endDate = coupon.getPaymentDate().toLocalDate();
    if (endDate.isBefore(fromDate)) {
      return Collections.emptyMap();
    }
    return Collections.singletonMap(endDate, MultipleCurrencyAmount.of(CurrencyAmount.of(coupon.getCurrency(), coupon.getNotional())));
  }

  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitCouponIborRatchetDefinition(final CouponIborRatchetDefinition coupon, final LocalDate fromDate) {
    ArgumentChecker.notNull(coupon, "coupon");
    ArgumentChecker.notNull(fromDate, "date");
    final LocalDate endDate = coupon.getPaymentDate().toLocalDate();
    if (endDate.isBefore(fromDate)) {
      return Collections.emptyMap();
    }
    return Collections.singletonMap(endDate, MultipleCurrencyAmount.of(CurrencyAmount.of(coupon.getCurrency(), coupon.getNotional())));
  }

  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitCouponOISDefinition(final CouponOISDefinition coupon, final LocalDate fromDate) {
    ArgumentChecker.notNull(coupon, "coupon");
    ArgumentChecker.notNull(fromDate, "date");
    final LocalDate endDate = coupon.getPaymentDate().toLocalDate();
    if (endDate.isBefore(fromDate)) {
      return Collections.emptyMap();
    }
    return Collections.singletonMap(endDate, MultipleCurrencyAmount.of(CurrencyAmount.of(coupon.getCurrency(), coupon.getNotional())));
  }

  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitForwardRateAgreementDefinition(final ForwardRateAgreementDefinition forwardRateAgreement, final LocalDate fromDate) {
    ArgumentChecker.notNull(forwardRateAgreement, "FRA");
    ArgumentChecker.notNull(fromDate, "date");
    final LocalDate endDate = forwardRateAgreement.getPaymentDate().toLocalDate();
    if (endDate.isBefore(fromDate)) {
      return Collections.emptyMap();
    }
    return Collections.singletonMap(endDate, MultipleCurrencyAmount.of(CurrencyAmount.of(forwardRateAgreement.getCurrency(), forwardRateAgreement.getNotional())));
  }

  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity, final LocalDate fromDate) {
    ArgumentChecker.notNull(annuity, "annuity");
    ArgumentChecker.notNull(fromDate, "date");
    return getDatesFromAnnuity(annuity, fromDate);
  }

  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitSwapDefinition(final SwapDefinition swap, final LocalDate fromDate) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(fromDate, "date");
    return swap.getFirstLeg().isPayer() ? swap.getFirstLeg().accept(this, fromDate) : swap.getSecondLeg().accept(this, fromDate);
  }

  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitSwapFixedIborDefinition(final SwapFixedIborDefinition swap, final LocalDate fromDate) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(fromDate, "date");
    if (swap.getIborLeg().isPayer()) {
      return swap.getIborLeg().accept(this, fromDate);
    }
    return Collections.emptyMap();
  }

  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitSwapFixedIborSpreadDefinition(final SwapFixedIborSpreadDefinition swap, final LocalDate fromDate) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(fromDate, "date");
    if (swap.getIborLeg().isPayer()) {
      return swap.getIborLeg().accept(this, fromDate);
    }
    return Collections.emptyMap();
  }

  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitSwapIborIborDefinition(final SwapIborIborDefinition swap, final LocalDate fromDate) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(fromDate, "date");
    if (swap.getFirstLeg().isPayer()) {
      return swap.getFirstLeg().accept(this, fromDate);
    }
    return swap.getSecondLeg().accept(this, fromDate);
  }

  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitSwapXCcyIborIborDefinition(final SwapXCcyIborIborDefinition swap, final LocalDate fromDate) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(fromDate, "date");
    if (swap.getFirstLeg().isPayer()) {
      return swap.getFirstLeg().accept(this, fromDate);
    }
    return swap.getSecondLeg().accept(this, fromDate);
  }

  private Map<LocalDate, MultipleCurrencyAmount> getDatesFromAnnuity(final AnnuityDefinition<? extends PaymentDefinition> annuity, final LocalDate fromDate) {
    final Map<LocalDate, MultipleCurrencyAmount> result = new HashMap<LocalDate, MultipleCurrencyAmount>();
    final FloatingPaymentVisitor floatingPaymentVisitor = FloatingPaymentVisitor.getInstance();
    for (final PaymentDefinition payment : annuity.getPayments()) {
      if (payment.accept(floatingPaymentVisitor)) {
        final LocalDate paymentDate = payment.getPaymentDate().toLocalDate();
        if (!paymentDate.isBefore(fromDate)) {
          final Map<LocalDate, MultipleCurrencyAmount> payments = payment.accept(this, fromDate);
          for (final Map.Entry<LocalDate, MultipleCurrencyAmount> entry : payments.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
          }
        }
      }
    }
    return result;
  }

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
