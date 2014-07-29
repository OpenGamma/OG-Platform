/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import org.threeten.bp.LocalDate;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorAdapter;
import com.opengamma.analytics.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedAccruedCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageFixingDatesCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageFixingDatesDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageFixingDatesCompoundingFlatSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageIndexDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingFlatSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborRatchetDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONCompoundedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONSpreadDefinition;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 *
 */
public class CouponAccrualDatesVisitor extends InstrumentDefinitionVisitorAdapter<Void, Pair<LocalDate, LocalDate>> {

  @Override
  public Pair<LocalDate, LocalDate> visitCouponFixedDefinition(final CouponFixedDefinition payment) {
    return Pairs.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponFixedAccruedCompoundingDefinition(final CouponFixedAccruedCompoundingDefinition payment) {
    return Pairs.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborDefinition(final CouponIborDefinition payment) {
    return Pairs.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborAverageDefinition(final CouponIborAverageIndexDefinition payment) {
    return Pairs.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborSpreadDefinition(final CouponIborSpreadDefinition payment) {
    return Pairs.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborGearingDefinition(final CouponIborGearingDefinition payment) {
    return Pairs.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborCompoundingDefinition(final CouponIborCompoundingDefinition payment) {
    return Pairs.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborCompoundingSpreadDefinition(final CouponIborCompoundingSpreadDefinition payment) {
    return Pairs.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborCompoundingFlatSpreadDefinition(final CouponIborCompoundingFlatSpreadDefinition payment) {
    return Pairs.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborRatchetDefinition(final CouponIborRatchetDefinition payment) {
    return Pairs.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponOISDefinition(final CouponONDefinition payment) {
    return Pairs.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponONCompoundedDefinition(final CouponONCompoundedDefinition payment) {
    return Pairs.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponCMSDefinition(final CouponCMSDefinition payment) {
    return Pairs.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponFixedCompoundingDefinition(final CouponFixedCompoundingDefinition payment) {
    return Pairs.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponArithmeticAverageONSpreadDefinition(final CouponONArithmeticAverageSpreadDefinition payment) {
    return Pairs.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponONSpreadDefinition(CouponONSpreadDefinition payment, Void data) {
    return Pairs.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponONSpreadDefinition(CouponONSpreadDefinition payment) {
    return Pairs.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborAverageFixingDatesDefinition(CouponIborAverageFixingDatesDefinition payment, Void data) {
    return Pairs.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborAverageFixingDatesDefinition(CouponIborAverageFixingDatesDefinition payment) {
    return Pairs.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborAverageCompoundingDefinition(CouponIborAverageFixingDatesCompoundingDefinition payment, Void data) {
    return Pairs.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborAverageCompoundingDefinition(CouponIborAverageFixingDatesCompoundingDefinition payment) {
    return Pairs.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborAverageFlatCompoundingSpreadDefinition(CouponIborAverageFixingDatesCompoundingFlatSpreadDefinition payment, Void data) {
    return Pairs.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborAverageFlatCompoundingSpreadDefinition(CouponIborAverageFixingDatesCompoundingFlatSpreadDefinition payment) {
    return Pairs.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }
}
