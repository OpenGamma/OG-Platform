/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.LocalDate;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponOISDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public final class FloatingCashFlowVisitor extends AbstractInstrumentDefinitionVisitor<LocalDate, Set<LocalDate>> {
  private static final FloatingCashFlowVisitor INSTANCE = new FloatingCashFlowVisitor();

  public static FloatingCashFlowVisitor getInstance() {
    return INSTANCE;
  }

  private FloatingCashFlowVisitor() {
  }

  @Override
  public Set<LocalDate> visit(final InstrumentDefinition<?> definition, final LocalDate fromDate) {
    return Collections.emptySet();
  }

  @Override
  public Set<LocalDate> visitForwardRateAgreementDefinition(final ForwardRateAgreementDefinition forwardRateAgreement, final LocalDate fromDate) {
    ArgumentChecker.notNull(forwardRateAgreement, "FRA");
    ArgumentChecker.notNull(fromDate, "date");
    final LocalDate endDate = forwardRateAgreement.getPaymentDate().toLocalDate();
    if (endDate.isBefore(fromDate)) {
      return Collections.emptySet();
    }
    return Collections.singleton(endDate);
  }

  @Override
  public Set<LocalDate> visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity, final LocalDate fromDate) {
    ArgumentChecker.notNull(annuity, "annuity");
    ArgumentChecker.notNull(fromDate, "date");
    return getDatesFromAnnuity(annuity, fromDate);
  }

  @Override
  public Set<LocalDate> visitSwapDefinition(final SwapDefinition swap, final LocalDate fromDate) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(fromDate, "date");
    return swap.getFirstLeg().isPayer() ? swap.getFirstLeg().accept(this, fromDate) : swap.getSecondLeg().accept(this, fromDate);
  }

  private Set<LocalDate> getDatesFromAnnuity(final AnnuityDefinition<? extends PaymentDefinition> annuity, final LocalDate fromDate) {
    final Set<LocalDate> result = new HashSet<LocalDate>();
    final FloatingPaymentVisitor fixedPaymentVisitor = FloatingPaymentVisitor.getInstance();
    for (final PaymentDefinition payment : annuity.getPayments()) {
      if (payment.accept(fixedPaymentVisitor)) {
        final LocalDate paymentDate = payment.getPaymentDate().toLocalDate();
        if (!paymentDate.isBefore(fromDate)) {
          final Set<LocalDate> payments = payment.accept(this, fromDate);
          for (final LocalDate entry : payments) {
            result.add(entry);
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
