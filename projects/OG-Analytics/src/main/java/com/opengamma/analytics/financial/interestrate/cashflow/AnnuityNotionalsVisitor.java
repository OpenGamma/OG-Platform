/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.cashflow;

import java.util.ArrayList;
import java.util.List;

import org.threeten.bp.LocalDate;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorAdapter;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Gets all notionals for an annuity.
 */
public final class AnnuityNotionalsVisitor extends InstrumentDefinitionVisitorAdapter<LocalDate, CurrencyAmount[]> {
  /** The singleton instance */
  private static final InstrumentDefinitionVisitor<LocalDate, CurrencyAmount[]> INSTANCE = new AnnuityNotionalsVisitor();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static InstrumentDefinitionVisitor<LocalDate, CurrencyAmount[]> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private AnnuityNotionalsVisitor() {
  }

  @Override
  public CurrencyAmount[] visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity, final LocalDate date) {
    final int n = annuity.getNumberOfPayments();
    final List<CurrencyAmount> ca = new ArrayList<>();
    int count = 0;
    for (int i = 0; i < n; i++) {
      final PaymentDefinition payment = annuity.getNthPayment(i);
      if (payment.getPaymentDate().toLocalDate().isAfter(date)) {
        ca.add(payment.accept(CouponNotionalVisitor.getInstance()));
        count++;
      }
    }
    return ca.toArray(new CurrencyAmount[count]);
  }
}
