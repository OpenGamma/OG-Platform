/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import org.threeten.bp.LocalDate;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorAdapter;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageIndexDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingFlatSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingSimpleSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborRatchetDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONSpreadDefinition;
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

  @Override
  public Pair<LocalDate, LocalDate> visitCouponOISDefinition(final CouponONDefinition payment) {
    return Pairs.of(payment.getFixingPeriodDate()[0].toLocalDate(),
                    payment.getFixingPeriodDate()[payment.getFixingPeriodDate().length - 1].toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponONSpreadDefinition(final CouponONSpreadDefinition payment) {
    return Pairs.of(payment.getFixingPeriodDate()[0].toLocalDate(),
                    payment.getFixingPeriodDate()[payment.getFixingPeriodDate().length - 1].toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponArithmeticAverageONDefinition(
      CouponONArithmeticAverageDefinition payment) {
    return Pairs.of(payment.getFixingPeriodStartDates()[0].toLocalDate(),
                    payment.getFixingPeriodEndDates()[payment.getFixingPeriodEndDates().length - 1].toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponArithmeticAverageONSpreadDefinition(
      CouponONArithmeticAverageSpreadDefinition payment) {
    return Pairs.of(payment.getFixingPeriodDates()[0].toLocalDate(),
                    payment.getFixingPeriodDates()[payment.getFixingPeriodDates().length - 1].toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborAverageDefinition(CouponIborAverageIndexDefinition payment) {
    return Pairs.of(payment.getFixingPeriodStartDate1().toLocalDate(), payment.getFixingPeriodEndDate2().toLocalDate());
  }
  
  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborCompoundingDefinition(CouponIborCompoundingDefinition payment) {
    return Pairs.of(payment.getFixingPeriodStartDates()[0].toLocalDate(),
                    payment.getFixingPeriodEndDates()[payment.getFixingPeriodEndDates().length - 1].toLocalDate());
  }
  
  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborCompoundingFlatSpreadDefinition(
      CouponIborCompoundingFlatSpreadDefinition payment) {
    return Pairs.of(payment.getFixingSubperiodStartDates()[0].toLocalDate(),
                    payment.getFixingSubperiodEndDates()[payment.getFixingSubperiodEndDates().length - 1].toLocalDate());
  }
  
  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborCompoundingSimpleSpreadDefinition(
      CouponIborCompoundingSimpleSpreadDefinition payment) {
    return Pairs.of(payment.getFixingSubperiodStartDates()[0].toLocalDate(),
                    payment.getFixingSubperiodEndDates()[payment.getFixingSubperiodEndDates().length - 1].toLocalDate());
  }
  
  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborCompoundingSpreadDefinition(
      CouponIborCompoundingSpreadDefinition payment) {
    return Pairs.of(payment.getFixingPeriodStartDates()[0].toLocalDate(),
                    payment.getFixingPeriodEndDates()[payment.getFixingPeriodEndDates().length - 1].toLocalDate());
  }
}
