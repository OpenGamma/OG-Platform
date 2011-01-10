/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.core.holiday;

import javax.time.calendar.LocalDate;

import com.opengamma.core.common.Currency;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * A source of holiday information as accessed by the main application.
 * <p>
 * This interface provides a simple view of holidays as used by most parts of the application.
 * This may be backed by a full-featured holiday master, or by a much simpler data structure.
 */
public interface HolidaySource {

  /**
   * Finds a specific holiday by unique identifier.
   * 
   * @param uid  the unique identifier, null returns null
   * @return the holiday, null if not found
   * @throws IllegalArgumentException if the identifier is invalid
   */
  Holiday getHoliday(UniqueIdentifier uid);

  /**
   * Checks if a date is a holiday for a CURRENCY type.
   * 
   * @param dateToCheck the date to check, not null
   * @param currency  the currency to check, not null
   * @return true if it is a holiday
   */
  boolean isHoliday(LocalDate dateToCheck, Currency currency);

  /**
   * Checks if a date is a holiday for a BANK, SETTLEMENT or TRADING type.
   * 
   * @param dateToCheck the date to check, not null
   * @param holidayType  the type of holiday, must not be CURRENCY, not null
   * @param regionOrExchangeIds  the regions or exchanges to check, not null
   * @return true if it is a holiday
   */
  boolean isHoliday(LocalDate dateToCheck, HolidayType holidayType, IdentifierBundle regionOrExchangeIds);

  /**
   * Checks if a date is a holiday for a BANK, SETTLEMENT or TRADING type.
   * 
   * @param dateToCheck the date to check, not null
   * @param holidayType  the type of holiday, must not be CURRENCY, not null
   * @param regionOrExchangeId  the region or exchange to check, not null
   * @return true if it is a holiday
   */
  boolean isHoliday(LocalDate dateToCheck, HolidayType holidayType, Identifier regionOrExchangeId);  

}
