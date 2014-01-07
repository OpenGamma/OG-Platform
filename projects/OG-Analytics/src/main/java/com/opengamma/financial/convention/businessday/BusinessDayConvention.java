/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

import org.joda.convert.FromStringFactory;
import org.joda.convert.ToString;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.TemporalAdjuster;

import com.opengamma.financial.convention.NamedInstance;
import com.opengamma.financial.convention.calendar.Calendar;

/**
 * Convention for handling business days.
 * <p>
 * This provides a mechanism to handle working and non-working days allowing
 * a date to be adjusted when it falls on a non-working day.
 */
@FromStringFactory(factory = BusinessDayConventionFactory.class)
public interface BusinessDayConvention extends NamedInstance {

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
   * Converts this convention to a {@code TemporalAdjuster} using the specified working day calendar.
   *
   * @param workingDayCalendar  the working days, not null
   * @return the date adjuster, not null
   */
  TemporalAdjuster getTemporalAdjuster(final Calendar workingDayCalendar);

  /**
   * Gets the name of the convention.
   *
   * @return the name, not null
   * @deprecated use getName()
   */
  @Deprecated
  String getConventionName();

  /**
   * Gets the name of the convention.
   *
   * @return the name, not null
   */
  @Override
  @ToString
  String getName();

}
