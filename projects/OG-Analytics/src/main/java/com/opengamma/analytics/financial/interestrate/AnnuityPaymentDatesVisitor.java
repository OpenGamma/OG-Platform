/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import java.util.ArrayList;
import java.util.List;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorAdapter;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;

/**
 * Gets the fixing period start and end dates for annuity from a particular date.
 */
public final class AnnuityPaymentDatesVisitor extends InstrumentDefinitionVisitorAdapter<ZonedDateTime, LocalDate[]> {
  /** A singleton instance */
  private static final InstrumentDefinitionVisitor<ZonedDateTime, LocalDate[]> INSTANCE = new AnnuityPaymentDatesVisitor();

  /**
   * Gets the single instance of this class.
   * @return The instance
   */
  public static InstrumentDefinitionVisitor<ZonedDateTime, LocalDate[]> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private AnnuityPaymentDatesVisitor() {
  }

  @Override
  public LocalDate[] visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity, final ZonedDateTime date) {
    final int n = annuity.getNumberOfPayments();
    final List<LocalDate> dates = new ArrayList<>();
    int count = 0;
    for (int i = 0; i < n; i++) {
      final PaymentDefinition payment = annuity.getNthPayment(i);
      if (!date.isAfter(payment.getPaymentDate())) {
        final LocalDate paymentDate = annuity.getNthPayment(i).getPaymentDate().toLocalDate();
        dates.add(paymentDate);
        count++;
      }
    }
    return dates.toArray(new LocalDate[count]);
  }

}
