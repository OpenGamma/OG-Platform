/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import org.threeten.bp.LocalDate;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorAdapter;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborRatchetDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;

/**
 *
 */
public class CouponPaymentDateVisitor extends InstrumentDefinitionVisitorAdapter<Void, LocalDate> {

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
