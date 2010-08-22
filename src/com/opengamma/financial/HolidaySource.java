/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import javax.time.calendar.LocalDate;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;

/**
 * Interface for querying information on holidays
 */
public interface HolidaySource {
  /**
   * This call can only be used for currency specific holidays, specifically it will throw an exception if used for
   * HolidayType.SETTLEMENT or HolidayType.TRADING, which require an exchange.
   * @param currency the currency which we're checking the holidays for
   * @param holidayDate the actual date we want to check if it's a holiday
   * @return true if it is a holiday
   */
  boolean isHoliday(Currency currency, LocalDate holidayDate);

  
  /**
   * This call can only be used for region specific holiday calendars, specifically not SETTLEMENT or TRADING 
   * holiday types.
   * @param regionOrExchangeId the identifier bundle for the region or exchange in which we're checking
   * @param holidayDate the actual date we want to check if it's a holiday
   * @param holidayType the type of holiday we're interested in (e.g. BANK).
   * @return true if it is a holiday
   */
  boolean isHoliday(IdentifierBundle regionOrExchangeId, LocalDate holidayDate, HolidayType holidayType);

  /**
   * This call can only be used for region specific holiday calendars, specifically not SETTLEMENT or TRADING 
   * holiday types.
   * @param regionOrExchangeId the region or exchange in which we're checking
   * @param holidayDate the actual date we want to check if it's a holiday
   * @param holidayType the type of holiday we're interested in (e.g. BANK).
   * @return true if it is a holiday
   */
  boolean isHoliday(Identifier regionOrExchangeId, LocalDate holidayDate, HolidayType holidayType);  
}
