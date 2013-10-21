/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import java.util.ArrayList;
import java.util.List;

import org.threeten.bp.LocalDate;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorAdapter;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.util.money.CurrencyAmount;

/**
 *
 */
public final class AnnuityNotionalsVisitor extends InstrumentDefinitionVisitorAdapter<LocalDate, CurrencyAmount[]> {
  private static final InstrumentDefinitionVisitor<Void, CurrencyAmount> COUPON_VISITOR = new CouponNotionalVisitor();
  private static final InstrumentDefinitionVisitor<LocalDate, CurrencyAmount[]> INSTANCE = new AnnuityNotionalsVisitor();

  public static InstrumentDefinitionVisitor<LocalDate, CurrencyAmount[]> getInstance() {
    return INSTANCE;
  }

  private AnnuityNotionalsVisitor() {
  }

  @Override
  public CurrencyAmount[] visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity, final LocalDate date) {
    final int n = annuity.getNumberOfPayments();
    final List<CurrencyAmount> ca = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      if (annuity.getNthPayment(i).getPaymentDate().toLocalDate().isAfter(date)) {
        ca.add(annuity.getNthPayment(i).accept(COUPON_VISITOR));
      }
    }
    return ca.toArray(new CurrencyAmount[ca.size()]);
  }
}
