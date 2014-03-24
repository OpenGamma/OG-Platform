/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.cashflow;

import org.threeten.bp.LocalDate;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorAdapter;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingFlatSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborRatchetDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * A class that gets the fixing period start and end dates.
 */
public final class CouponFixingDatesVisitor extends InstrumentDefinitionVisitorAdapter<Void, Pair<LocalDate, LocalDate>> {
  /** The singleton instance */
  private static final InstrumentDefinitionVisitor<Void, Pair<LocalDate, LocalDate>> INSTANCE = new CouponFixingDatesVisitor();

  /**
   * Gets the singleton instance.
   * @return The singleton instance
   */
  public static InstrumentDefinitionVisitor<Void, Pair<LocalDate, LocalDate>> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponFixingDatesVisitor() {
  }

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
  public Pair<LocalDate, LocalDate> visitCouponIborCompoundingDefinition(CouponIborCompoundingDefinition payment) {
    return Pairs.of(payment.getFixingPeriodStartDates()[0].toLocalDate(),
                    payment.getFixingPeriodEndDates()[payment.getFixingPeriodEndDates().length - 1].toLocalDate());
  }
  
  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborCompoundingFlatSpreadDefinition(CouponIborCompoundingFlatSpreadDefinition payment) {
    return Pairs.of(payment.getFixingSubperiodStartDates()[0].toLocalDate(),
                    payment.getFixingSubperiodEndDates()[payment.getFixingSubperiodEndDates().length - 1].toLocalDate());
  }
  
  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborCompoundingSpreadDefinition(CouponIborCompoundingSpreadDefinition payment) {
    return Pairs.of(payment.getFixingPeriodStartDates()[0].toLocalDate(),
                    payment.getFixingPeriodEndDates()[payment.getFixingPeriodEndDates().length - 1].toLocalDate());
  }
}
