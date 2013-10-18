/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import org.threeten.bp.LocalDate;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorAdapter;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 *
 */
public final class AnnuityAccrualDatesVisitor extends InstrumentDefinitionVisitorAdapter<Void, Pair<LocalDate[], LocalDate[]>> {
  private static final InstrumentDefinitionVisitor<Void, Pair<LocalDate, LocalDate>> COUPON_VISITOR = new CouponAccrualDatesVisitor();
  private static final InstrumentDefinitionVisitor<Void, Pair<LocalDate[], LocalDate[]>> INSTANCE = new AnnuityAccrualDatesVisitor();

  public static InstrumentDefinitionVisitor<Void, Pair<LocalDate[], LocalDate[]>> getInstance() {
    return INSTANCE;
  }

  private AnnuityAccrualDatesVisitor() {
  }

  @Override
  public Pair<LocalDate[], LocalDate[]> visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity) {
    final int n = annuity.getNumberOfPayments();
    final LocalDate[] startDates = new LocalDate[n];
    final LocalDate[] endDates = new LocalDate[n];
    for (int i = 0; i < n; i++) {
      final Pair<LocalDate, LocalDate> dates = annuity.getNthPayment(i).accept(COUPON_VISITOR);
      startDates[i] = dates.getFirst();
      endDates[i] = dates.getSecond();
    }
    return Pairs.of(startDates, endDates);
  }
}
