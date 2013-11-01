/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.schedule;

import java.io.Serializable;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.calendar.Calendar;

/**
 * Working days calendar implementation that has no holidays.
 */
public class NoHolidayCalendar implements Calendar, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  @Override
  public boolean isWorkingDay(final LocalDate date) {
    return true;
  }

  /**
   * @deprecated use getName()
   * @return the convention name
   */
  @Override
  @Deprecated
  public String getConventionName() {
    return getName();
  }

  @Override
  public String getName() {
    return "No holidays";
  }

}
