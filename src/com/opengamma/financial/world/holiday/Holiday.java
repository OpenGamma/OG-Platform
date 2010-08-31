/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.world.holiday;

import java.util.Collection;

import javax.time.calendar.LocalDate;

import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;

/**
 * 
 */
public interface Holiday {

  /**
   * @return an identifier for the region, if this is a BANK holiday, null otherwise
   */
  public abstract Identifier getRegionId();

  /**
   * @return an identifier for the exchange, if this is a SETTLEMENT or TRADING holiday, null otherwise.
   */
  public abstract Identifier getExchangeId();

  /**
   * @return a currency, if this is a CURRENCY holiday, null otherwise
   */
  public abstract String getCurrencyISO();

  /**
   * @return the type of holiday: CURRENCY, BANK, SETTLEMENT or TRADING
   */
  public abstract HolidayType getHolidayType();

  /**
   * @return a collection of dates on which holidays fall
   */
  public abstract Collection<LocalDate> getHolidays();

  /**
   * @return the UniqueIdentifier for this holiday entry
   */
  public abstract UniqueIdentifier getUniqueIdentifier();

}
