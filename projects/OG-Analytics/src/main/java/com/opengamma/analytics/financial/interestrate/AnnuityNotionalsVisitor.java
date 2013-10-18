/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorAdapter;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.util.money.CurrencyAmount;

/**
 *
 */
public final class AnnuityNotionalsVisitor extends InstrumentDefinitionVisitorAdapter<Void, CurrencyAmount[]> {
  private static final InstrumentDefinitionVisitor<Void, CurrencyAmount> COUPON_VISITOR = new CouponNotionalVisitor();
  private static final InstrumentDefinitionVisitor<Void, CurrencyAmount[]> INSTANCE = new AnnuityNotionalsVisitor();

  public static InstrumentDefinitionVisitor<Void, CurrencyAmount[]> getInstance() {
    return INSTANCE;
  }

  private AnnuityNotionalsVisitor() {
  }

  @Override
  public CurrencyAmount[] visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity) {
    final int n = annuity.getNumberOfPayments();
    final CurrencyAmount[] ca = new CurrencyAmount[n];
    for (int i = 0; i < n; i++) {
      ca[i] = annuity.getNthPayment(i).accept(COUPON_VISITOR);
    }
    return ca;
  }
}
