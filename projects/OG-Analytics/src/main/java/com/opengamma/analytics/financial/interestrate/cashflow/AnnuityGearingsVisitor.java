/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.cashflow;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import org.threeten.bp.LocalDate;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorAdapter;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;

/**
 * Gets the gearings for the coupons in an annuity.
 */
public final class AnnuityGearingsVisitor extends InstrumentDefinitionVisitorAdapter<LocalDate, double[]> {
  /** A singleton instance */
  private static final InstrumentDefinitionVisitor<LocalDate, double[]> INSTANCE = new AnnuityGearingsVisitor();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static InstrumentDefinitionVisitor<LocalDate, double[]> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private AnnuityGearingsVisitor() {
  }

  @Override
  public double[] visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity, final LocalDate date) {
    final int n = annuity.getNumberOfPayments();
    final DoubleArrayList fractions = new DoubleArrayList();
    for (int i = 0; i < n; i++) {
      final PaymentDefinition payment = annuity.getNthPayment(i);
      if (payment.getPaymentDate().toLocalDate().isAfter(date)) {
        fractions.add(payment.accept(CouponGearingVisitor.getInstance()));
      }
    }
    return fractions.toDoubleArray();
  }

}
