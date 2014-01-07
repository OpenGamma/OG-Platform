/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

import java.io.Serializable;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.TemporalAdjuster;

import com.opengamma.financial.convention.calendar.Calendar;

/**
 * Abstract implementation of a convention for handling business days.
 */
public abstract class AbstractBusinessDayConvention implements BusinessDayConvention, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  @Override
  public ZonedDateTime adjustDate(final Calendar workingDayCalendar, final ZonedDateTime dateTime) {
    LocalDate adjusted = adjustDate(workingDayCalendar, dateTime.toLocalDate());
    return adjusted.atTime(dateTime.toLocalTime()).atZone(dateTime.getZone());
  }

  @Override
  public TemporalAdjuster getTemporalAdjuster(final Calendar workingDayCalendar) {
    return new BusinessDayConventionWithCalendar(this, workingDayCalendar);
  }

  @Override
  public String toString() {
    return "BusinessDayConvention [" + getName() + "]";
  }

  /**
   * Gets the name of the convention.
   * 
   * @return the name, not null
   * @deprecated use getName()
   */
  @Override
  @Deprecated
  public String getConventionName() {
    return getName();
  }

}
