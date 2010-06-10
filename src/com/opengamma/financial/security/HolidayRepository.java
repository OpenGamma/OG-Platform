/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import javax.time.calendar.LocalDate;

/**
 * Interface for querying information on holidays
 */
public interface HolidayRepository {
  /**
   * 
   * @param versionDate this is the 'information as of' date - the date we're assuming is 'today'
   * @param region the region in which we're checking
   * @param holidayDate the actual date we want to check if it's a holiday.
   * @return true if it is a holiday
   */
  boolean isHoliday(LocalDate versionDate, Region region, LocalDate holidayDate);
}
