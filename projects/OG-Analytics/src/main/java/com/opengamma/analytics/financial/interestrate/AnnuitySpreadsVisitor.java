/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorAdapter;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;

/**
 * Gets the spreads for the coupons in an annuity.
 */
public final class AnnuitySpreadsVisitor extends InstrumentDefinitionVisitorAdapter<ZonedDateTime, double[]> {
  /** The coupon accrual year fraction visitor */
  private static final InstrumentDefinitionVisitor<Void, Double> COUPON_VISITOR = CouponSpreadVisitor.getInstance();
  /** A singleton instance */
  private static final InstrumentDefinitionVisitor<ZonedDateTime, double[]> INSTANCE = new AnnuitySpreadsVisitor();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static InstrumentDefinitionVisitor<ZonedDateTime, double[]> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private AnnuitySpreadsVisitor() {
  }

  @Override
  public double[] visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity, final ZonedDateTime date) {
    final int n = annuity.getNumberOfPayments();
    final DoubleArrayList fractions = new DoubleArrayList();
    for (int i = 0; i < n; i++) {
      final PaymentDefinition payment = annuity.getNthPayment(i);
      if (!date.isAfter(payment.getPaymentDate())) {
        fractions.add(payment.accept(COUPON_VISITOR));
      }
    }
    return fractions.toDoubleArray();
  }

}
