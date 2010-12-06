/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.convention;

import javax.time.calendar.DateAdjuster;
import javax.time.calendar.LocalDate;


/**
 * Allows a {@code BusinessDayConvention} to fulfill the {@code DateAdjuster} interface.
 */
/* package */class BusinessDayConventionWithCalendar implements DateAdjuster {

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
   * @param businessDayConvention  the convention, not null
   * @param workingDayCalendar  the working days, not null
   */
  protected BusinessDayConventionWithCalendar(final BusinessDayConvention businessDayConvention, final Calendar workingDayCalendar) {
    _businessDayConvention = businessDayConvention;
    _workingDayCalendar = workingDayCalendar;
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDate adjustDate(LocalDate date) {
    return _businessDayConvention.adjustDate(_workingDayCalendar, date);
  }

}
