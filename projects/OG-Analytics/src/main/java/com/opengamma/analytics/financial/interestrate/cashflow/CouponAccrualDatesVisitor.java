/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.cashflow;

import org.threeten.bp.LocalDate;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorAdapter;
import com.opengamma.analytics.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedAccruedCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingFlatSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborRatchetDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONCompoundedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONDefinition;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Returns the accrual start and end dates for a coupon.
 */
public final class CouponAccrualDatesVisitor extends InstrumentDefinitionVisitorAdapter<Void, Pair<LocalDate, LocalDate>> {
  /** Static instance */
  private static final InstrumentDefinitionVisitor<Void, Pair<LocalDate, LocalDate>> INSTANCE = new CouponAccrualDatesVisitor();

  /**
   * Gets a static instance.
   * @return The instance.
   */
  public static InstrumentDefinitionVisitor<Void, Pair<LocalDate, LocalDate>> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponAccrualDatesVisitor() {
  }

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
  public Pair<LocalDate, LocalDate> visitCouponIborAverageDefinition(final CouponIborAverageDefinition payment) {
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

}
