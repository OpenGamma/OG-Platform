/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.convention;

import javax.time.calendar.DateAdjuster;
import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

/**
 * Convention for handling business days.
 */
public abstract class BusinessDayConvention {

  /**
   * Adjusts the specified date using the working day calendar.
   * @param workingDayCalendar  the working days, not null
   * @param date  the date to adjust, not null
   * @return the adjusted date, not null
   */
  public abstract LocalDate adjustDate(Calendar workingDayCalendar, LocalDate date);

  /**
   * Adjusts the specified date-time using the working day calendar.
   * @param workingDayCalendar  the working days, not null
   * @param dateTime  the date-time to adjust, not null
   * @return the adjusted date-time, not null
   */
  public ZonedDateTime adjustDate(final Calendar workingDayCalendar, final ZonedDateTime dateTime) {
    LocalDate adjusted = adjustDate(workingDayCalendar, dateTime.toLocalDate());
    return ZonedDateTime.of(adjusted, dateTime.toLocalTime(), dateTime.getZone());
  }

  /**
   * Converts this convention to a {@code DateAdjuster} using the specified working day calendar.
   * @param workingDayCalendar  the working days, not null
   * @return the date adjuster, not null
   */
  public DateAdjuster getDateAdjuster(final Calendar workingDayCalendar) {
    return new BusinessDayConventionWithCalendar(this, workingDayCalendar);
  }

  /**
   * Gets the name of the convention.
   * @return the name, not null
   */
  public abstract String getConventionName();

}
