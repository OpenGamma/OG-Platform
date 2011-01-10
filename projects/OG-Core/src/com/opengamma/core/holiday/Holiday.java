/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.holiday;

import java.util.List;

import javax.time.calendar.LocalDate;

import com.opengamma.core.common.Currency;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;

/**
 * A set of holidays when some form of market activity does not occur.
 * <p>
 * Markets have, on occasion, holidays when there is no business.
 * These can form part of algorithms and contracts.
 * Instances of this interface represent all known holiday dates for a single type of business.
 */
public interface Holiday extends UniqueIdentifiable {

  /**
   * The unique identifier of the holiday.
   * 
   * @return the unique identifier for this holiday entry, not null
   */
  UniqueIdentifier getUniqueId();

  /**
   * The type of the holiday.
   * 
   * @return the type of holiday, such as CURRENCY, BANK, SETTLEMENT or TRADING, not null
   */
  HolidayType getType();

  /**
   * The region key identifier, used when this is a holiday of type BANK.
   * 
   * @return an identifier for the region, if this is a BANK holiday, null otherwise
   */
  Identifier getRegionKey();

  /**
   * The exchange key identifier, used when this is a holiday of type SETTLEMENT or TRADING.
   * 
   * @return an identifier for the exchange, if this is a SETTLEMENT or TRADING holiday, null otherwise
   */
  Identifier getExchangeKey();

  /**
   * The currency, used when this is a holiday of type CURRENCY.
   * 
   * @return a currency, if this is a CURRENCY holiday, null otherwise
   */
  Currency getCurrency();

  /**
   * The list of dates on which the holiday occurs.
   * <p>
   * The list is ordered from the past to the future
   * 
   * @return a list of dates on which holidays fall, not null
   */
  List<LocalDate> getHolidayDates();

}
