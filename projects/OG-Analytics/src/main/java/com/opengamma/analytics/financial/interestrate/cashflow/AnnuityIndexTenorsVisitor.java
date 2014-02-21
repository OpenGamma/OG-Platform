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
import com.opengamma.util.time.Tenor;

/**
 * Gets the index tenors for the coupons in an annuity.
 */
public final class AnnuityIndexTenorsVisitor extends InstrumentDefinitionVisitorAdapter<LocalDate, Tenor[]> {
  /** A singleton instance */
  private static final InstrumentDefinitionVisitor<LocalDate, Tenor[]> INSTANCE = new AnnuityIndexTenorsVisitor();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static InstrumentDefinitionVisitor<LocalDate, Tenor[]> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private AnnuityIndexTenorsVisitor() {
  }

  @Override
  public Tenor[] visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity, final LocalDate date) {
    final int n = annuity.getNumberOfPayments();
    final List<Tenor> tenors = new ArrayList<>();
    int count = 0;
    for (int i = 0; i < n; i++) {
      final PaymentDefinition payment = annuity.getNthPayment(i);
      if (payment.getPaymentDate().toLocalDate().isAfter(date)) {
        tenors.add(payment.accept(CouponTenorVisitor.getInstance()));
        count++;
      }
    }
    return tenors.toArray(new Tenor[count]);
  }

}
