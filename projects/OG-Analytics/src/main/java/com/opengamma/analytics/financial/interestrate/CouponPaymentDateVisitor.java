/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import org.threeten.bp.LocalDate;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorAdapter;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingFlatSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborRatchetDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONSpreadDefinition;

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

  @Override
  public LocalDate visitCouponIborCompoundingDefinition(final CouponIborCompoundingDefinition payment) {
    return payment.getPaymentDate().toLocalDate();
  }

  @Override
  public LocalDate visitCouponIborCompoundingFlatSpreadDefinition(final CouponIborCompoundingFlatSpreadDefinition payment) {
    return payment.getPaymentDate().toLocalDate();
  }

  @Override
  public LocalDate visitCouponOISDefinition(final CouponONDefinition payment) {
    return payment.getPaymentDate().toLocalDate();
  }

  @Override
  public LocalDate visitCouponArithmeticAverageONSpreadDefinition(final CouponONArithmeticAverageSpreadDefinition payment) {
    return payment.getPaymentDate().toLocalDate();
  }

  @Override
  public LocalDate visitCouponFixedDefinition(final CouponFixedDefinition payment) {
    return payment.getPaymentDate().toLocalDate();
  }

  @Override
  public LocalDate visitCouponONSpreadDefinition(final CouponONSpreadDefinition payment) {
    return payment.getPaymentDate().toLocalDate();
  }

}
