/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.Temporal;
import org.threeten.bp.temporal.TemporalAdjuster;

import com.opengamma.financial.convention.calendar.Calendar;

/**
 * Allows a {@code BusinessDayConvention} to fulfill the {@code TemporalAdjuster} interface.
 */
/* package */class BusinessDayConventionWithCalendar implements TemporalAdjuster {

  /**
   * The convention.
   */
  private final BusinessDayConvention _businessDayConvention;
  /**
   * The working days.
   */
  private final Calendar _workingDayCalendar;

  /**
   * Creates an instance.
   * 
   * @param businessDayConvention  the convention, not null
   * @param workingDayCalendar  the working days, not null
   */
  protected BusinessDayConventionWithCalendar(final BusinessDayConvention businessDayConvention, final Calendar workingDayCalendar) {
    _businessDayConvention = businessDayConvention;
    _workingDayCalendar = workingDayCalendar;
  }

  //-------------------------------------------------------------------------
  @Override
  public Temporal adjustInto(Temporal temporal) {
    TemporalAdjuster result = _businessDayConvention.adjustDate(_workingDayCalendar, LocalDate.from(temporal));
    return temporal.with(result);
  }

}
