/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import javax.time.calendar.LocalDate;


/**
 * Interface for querying information on holidays
 */
public interface HolidayRepository {
  /**
   * This call can only be used for currency specific holidays, specifically it will throw an exception if used for
   * HolidayType.SETTLEMENT or HolidayType.TRADING, which require an exchange.
   * @param versionDate this is the 'information as of' date - the date we're assuming is 'today',  NOT IMPLEMENTED, CAN BE NULL CURRENTLY
   * @param currency the currency which we're checking the holidays for
   * @param holidayDate the actual date we want to check if it's a holiday
   * @param holidayType the type of holiday we're interested in (CURRENCY).
   * @return true if it is a holiday
   */
  boolean isHoliday(LocalDate versionDate, Currency currency, LocalDate holidayDate, HolidayType holidayType);
  
  /**
   * This call can only be used for region specific holiday calendars, specifically not SETTLEMENT or TRADING 
   * holiday types.
   * @param versionDate this is the 'information as of' date - the date we're assuming is 'today'.  NOT IMPLEMENTED, CAN BE NULL CURRENTLY
   * @param region the region in which we're checking
   * @param holidayDate the actual date we want to check if it's a holiday
   * @param holidayType the type of holiday we're interested in (e.g. BANK).
   * @return true if it is a holiday
   */
  boolean isHoliday(LocalDate versionDate, Region region, LocalDate holidayDate, HolidayType holidayType);
  
  /**
   * 
   * @param versionDate this is the 'information as of' date - the date we're assuming is 'today'.  NOT IMPLEMENTED, CAN BE NULL CURRENTLY
   * @param exchange the exchange we're checking
   * @param holidayDate the actual date we want to check if it's a holiday
   * @param holidayType the type of holiday we're interested in
   * @return true if it is a holiday
   */
  boolean isHoliday(LocalDate versionDate, Exchange exchange, LocalDate holidayDate, HolidayType holidayType);
}
