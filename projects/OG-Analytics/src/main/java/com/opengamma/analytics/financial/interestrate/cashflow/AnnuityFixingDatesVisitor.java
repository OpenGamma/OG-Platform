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
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Gets the fixing period start and end dates for annuity from a particular date.
 */
public final class AnnuityFixingDatesVisitor extends InstrumentDefinitionVisitorAdapter<LocalDate, Pair<LocalDate[], LocalDate[]>> {
  /** A singleton instance */
  private static final InstrumentDefinitionVisitor<LocalDate, Pair<LocalDate[], LocalDate[]>> INSTANCE = new AnnuityFixingDatesVisitor();

  /**
   * Gets the singleton instance of this class.
   * @return The instance
   */
  public static InstrumentDefinitionVisitor<LocalDate, Pair<LocalDate[], LocalDate[]>> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private AnnuityFixingDatesVisitor() {
  }

  @Override
  public Pair<LocalDate[], LocalDate[]> visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity, final LocalDate date) {
    final int n = annuity.getNumberOfPayments();
    final List<LocalDate> startDates = new ArrayList<>();
    final List<LocalDate> endDates = new ArrayList<>();
    int count = 0;
    for (int i = 0; i < n; i++) {
      final PaymentDefinition payment = annuity.getNthPayment(i);
      if (payment.getPaymentDate().toLocalDate().isAfter(date)) {
        final Pair<LocalDate, LocalDate> dates = annuity.getNthPayment(i).accept(CouponFixingDatesVisitor.getInstance());
        startDates.add(dates.getFirst());
        endDates.add(dates.getSecond());
        count++;
      }
    }
    return Pairs.of(startDates.toArray(new LocalDate[count]), endDates.toArray(new LocalDate[count]));
  }

}
