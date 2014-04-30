/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.cashflow;

import java.util.ArrayList;

import org.threeten.bp.LocalDate;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorAdapter;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;

/**
 * Gets the fixing year fractions for the coupons in an annuity.
 */
public final class AnnuityFixingYearFractionsVisitor extends InstrumentDefinitionVisitorAdapter<LocalDate, Double[]> {
  /** A singleton instance */
  private static final InstrumentDefinitionVisitor<LocalDate, Double[]> INSTANCE = new AnnuityFixingYearFractionsVisitor();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static InstrumentDefinitionVisitor<LocalDate, Double[]> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private AnnuityFixingYearFractionsVisitor() {
  }

  @Override
  public Double[] visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity, final LocalDate date) {
    int n = annuity.getNumberOfPayments();
    final ArrayList<Double> fractions = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      final PaymentDefinition payment = annuity.getNthPayment(i);
      if (payment.getPaymentDate().toLocalDate().isAfter(date)) {
        try {
          fractions.add(payment.accept(CouponFixingYearFractionVisitor.getInstance()));
        } catch (UnsupportedOperationException ex) {
          fractions.add(null);
        }
      }
    }
    return fractions.toArray(new Double[fractions.size()]);
  }

}
