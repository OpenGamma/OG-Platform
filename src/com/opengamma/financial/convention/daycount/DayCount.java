/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import javax.time.calendar.ZonedDateTime;

/**
 * Convention for calculating the day count.
 */
public interface DayCount {

  /**
   * Gets the basis of the day count for the specified date.
   * <p>
   * The basis is the number of days the convention defines as being in a year.
   * This method is needed because some day count conventions define a year length
   * as 366 in a leap year but 365 otherwise.
   * 
   * @param date  the date for which the basis is required, not null
   * @return the number of days in a year
   */
  double getBasis(final ZonedDateTime date);

  /**
   * Gets the day count between the specified dates.
   * <p>
   * Given two dates, this method returns the fraction of a year between these dates
   * according to the convention.
   * 
   * @param firstDate  the earlier date, not null
   * @param secondDate  the later date, not null
   * @return the day count fraction
   */
  double getDayCountFraction(final ZonedDateTime firstDate, final ZonedDateTime secondDate);

  /**
   * Gets the name of the convention.
   * @return the name, not null
   */
  String getConventionName();

}
