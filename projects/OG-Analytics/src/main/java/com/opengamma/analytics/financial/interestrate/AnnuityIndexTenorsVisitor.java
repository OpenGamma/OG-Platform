/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorAdapter;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.util.time.Tenor;

/**
 * Gets the index tenors for the coupons in an annuity.
 */
public final class AnnuityIndexTenorsVisitor extends InstrumentDefinitionVisitorAdapter<ZonedDateTime, Set<Tenor>[]> {
  /** The coupon accrual year fraction visitor */
  private static final InstrumentDefinitionVisitor<Void, Set<Tenor>> COUPON_VISITOR = CouponTenorVisitor.getInstance();
  /** A singleton instance */
  private static final InstrumentDefinitionVisitor<ZonedDateTime, Set<Tenor>[]> INSTANCE = new AnnuityIndexTenorsVisitor();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static InstrumentDefinitionVisitor<ZonedDateTime, Set<Tenor>[]> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private AnnuityIndexTenorsVisitor() {
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<Tenor>[] visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity, final ZonedDateTime date) {
    final int n = annuity.getNumberOfPayments();
    final List<Set<Tenor>> tenors = new ArrayList<>();
    int count = 0;
    for (int i = 0; i < n; i++) {
      final PaymentDefinition payment = annuity.getNthPayment(i);
      if (!date.isAfter(payment.getPaymentDate())) {
        tenors.add(payment.accept(COUPON_VISITOR));
        count++;
      }
    }
    return tenors.toArray(new Set[count]);
  }

}
