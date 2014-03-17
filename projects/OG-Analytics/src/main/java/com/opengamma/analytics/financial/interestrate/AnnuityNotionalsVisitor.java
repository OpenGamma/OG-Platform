/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import java.util.ArrayList;
import java.util.List;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorAdapter;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Gets all notionals for an annuity.
 */
public final class AnnuityNotionalsVisitor extends InstrumentDefinitionVisitorAdapter<ZonedDateTime, CurrencyAmount[]> {
  /** Gets the notional for a coupon */
  private static final InstrumentDefinitionVisitor<Void, CurrencyAmount> COUPON_VISITOR = new CouponNotionalVisitor();
  /** The singleton instance */
  private static final InstrumentDefinitionVisitor<ZonedDateTime, CurrencyAmount[]> INSTANCE = new AnnuityNotionalsVisitor();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static InstrumentDefinitionVisitor<ZonedDateTime, CurrencyAmount[]> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private AnnuityNotionalsVisitor() {
  }

  @Override
  public CurrencyAmount[] visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity, final ZonedDateTime date) {
    final int n = annuity.getNumberOfPayments();
    final List<CurrencyAmount> ca = new ArrayList<>();
    int count = 0;
    for (int i = 0; i < n; i++) {
      final PaymentDefinition payment = annuity.getNthPayment(i);
      if (!date.isAfter(payment.getPaymentDate())) {
        ca.add(payment.accept(COUPON_VISITOR));
        count++;
      }
    }
    return ca.toArray(new CurrencyAmount[count]);
  }
}
