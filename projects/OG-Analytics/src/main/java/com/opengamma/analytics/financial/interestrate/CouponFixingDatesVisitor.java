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
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 *
 */
public class CouponFixingDatesVisitor extends InstrumentDefinitionVisitorAdapter<Void, Pair<LocalDate, LocalDate>> {

  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborDefinition(final CouponIborDefinition payment) {
    return Pairs.of(payment.getFixingPeriodStartDate().toLocalDate(), payment.getFixingPeriodEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborSpreadDefinition(final CouponIborSpreadDefinition payment) {
    return Pairs.of(payment.getFixingPeriodStartDate().toLocalDate(), payment.getFixingPeriodEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborGearingDefinition(final CouponIborGearingDefinition payment) {
    return Pairs.of(payment.getFixingPeriodStartDate().toLocalDate(), payment.getFixingPeriodEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborRatchetDefinition(final CouponIborRatchetDefinition payment) {
    return Pairs.of(payment.getFixingPeriodStartDate().toLocalDate(), payment.getFixingPeriodEndDate().toLocalDate());
  }

}
