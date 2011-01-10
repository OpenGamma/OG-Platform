/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

import javax.time.calendar.DateAdjuster;
import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.convention.calendar.Calendar;

/**
 * Convention for handling business days.
 * <p>
 * This provides a mechanism to handle working and non-working days allowing
 * a date to be adjusted when it falls on a non-working day.
 */
public interface BusinessDayConvention {

  /**
   * Adjusts the specified date using the working day calendar.
   * 
   * @param workingDayCalendar  the working days, not null
   * @param date  the date to adjust, not null
   * @return the adjusted date, not null
   */
  LocalDate adjustDate(Calendar workingDayCalendar, LocalDate date);

  /**
   * Adjusts the specified date-time using the working day calendar.
   * 
   * @param workingDayCalendar  the working days, not null
   * @param dateTime  the date-time to adjust, not null
   * @return the adjusted date-time, not null
   */
  ZonedDateTime adjustDate(final Calendar workingDayCalendar, final ZonedDateTime dateTime);

  /**
   * Converts this convention to a {@code DateAdjuster} using the specified working day calendar.
   * 
   * @param workingDayCalendar  the working days, not null
   * @return the date adjuster, not null
   */
  DateAdjuster getDateAdjuster(final Calendar workingDayCalendar);

  /**
   * Gets the name of the convention.
   * 
   * @return the name, not null
   */
  String getConventionName();

}
