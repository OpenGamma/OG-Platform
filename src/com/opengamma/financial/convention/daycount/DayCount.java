/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import javax.time.calendar.ZonedDateTime;

/**
 * 
 * Interface for day count conventions.
 * 
 * @author emcleod
 */

public interface DayCount {

  /**
   * 
   * The basis is the number of days the convention defines as being in a year.
   * 
   * @param The
   *          date for which the basis is required. This parameter is needed
   *          because some day count conventions define a year length as 366 in
   *          a leap year but 365 otherwise.
   * @return The number of days in a year.
   */
  public double getBasis(final ZonedDateTime date);

  /**
   * 
   * Given two dates, this method returns the fraction of a year between these
   * dates.
   * 
   * @param firstDate
   *          The earlier date.
   * @param secondDate
   *          The later date.
   * @return The fraction.
   */
  public double getDayCountFraction(final ZonedDateTime firstDate, final ZonedDateTime secondDate);
}
