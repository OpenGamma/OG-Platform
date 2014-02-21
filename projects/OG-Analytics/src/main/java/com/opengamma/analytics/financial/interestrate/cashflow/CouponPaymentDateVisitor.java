/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.cashflow;

import org.threeten.bp.LocalDate;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorAdapter;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborRatchetDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;

/**
 * Gets the coupon payment date.
 */
public final class CouponPaymentDateVisitor extends InstrumentDefinitionVisitorAdapter<Void, LocalDate> {
  /** The singleton instance */
  private static final InstrumentDefinitionVisitor<Void, LocalDate> INSTANCE = new CouponPaymentDateVisitor();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static InstrumentDefinitionVisitor<Void, LocalDate> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponPaymentDateVisitor() {
  }

  @Override
  public LocalDate visitCouponIborDefinition(final CouponIborDefinition payment) {
    return payment.getPaymentDate().toLocalDate();
  }

  @Override
  public LocalDate visitCouponIborSpreadDefinition(final CouponIborSpreadDefinition payment) {
    return payment.getPaymentDate().toLocalDate();
  }

  @Override
  public LocalDate visitCouponIborGearingDefinition(final CouponIborGearingDefinition payment) {
    return payment.getPaymentDate().toLocalDate();
  }

  @Override
  public LocalDate visitCouponIborRatchetDefinition(final CouponIborRatchetDefinition payment) {
    return payment.getPaymentDate().toLocalDate();
  }

}
