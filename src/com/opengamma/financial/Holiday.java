/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import java.util.Collection;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;

/**
 * 
 */
public class Holiday {
  private Identifier _regionId;
  private Currency _currency;
  private HolidayType _holidayType;
  private Identifier _exchangeId;
  private Collection<LocalDate> _holidaySeries;
  private UniqueIdentifier _uniqueIdentifier;

  /**
   * Create a CURRENCY holiday with a collection of holiday dates.  This constructor does not take a
   * UniqueIdentifier as one is presumed to be unavailable until after construction and registration.
   * Use the setUniqueIdentifier() method once registered.
   * @param currency the currency of this CURRENCY holiday schedule
   * @param holidaySeries the dates on which holidays fall
   */
  public Holiday(Currency currency, Collection<LocalDate> holidaySeries) {
    _currency = currency;
    _holidayType = HolidayType.CURRENCY;
    _holidaySeries = holidaySeries;
  }

  /**
   * Create a BANK, SETTLEMENT or TRADING holiday with a collection of holiday dates.  This constructor does not take a
   * UniqueIdentifier as one is presumed to be unavailable until after construction and registration.
   * Use the setUniqueIdentifier() method once registered.
   * @param regionOrExchangeId an Identifier for either a region (for a BANK holiday) or an exchange (for a SETTLEMENT or TRADING holiday)
   * @param holidayType the type of the holiday
   * @param holidaySeries a collection of dates on which holidays fall
   */
  public Holiday(Identifier regionOrExchangeId, HolidayType holidayType, Collection<LocalDate> holidaySeries) {
    switch (holidayType) {
      case BANK:
        _regionId = regionOrExchangeId;
        break;
      case CURRENCY:
        throw new IllegalArgumentException("Use the Currency constructor for a currency related Holiday");
      case SETTLEMENT:
      case TRADING:
        _exchangeId = regionOrExchangeId;
        break;
    }
    _holidayType = holidayType;
    _holidaySeries = holidaySeries;
  }
  
  /**
   * @return an identifier for the region, if this is a BANK holiday, null otherwise
   */
  public Identifier getRegionId() {
    return _regionId;
  }
  
  /**
   * @return an identifier for the exchange, if this is a SETTLEMENT or TRADING holiday, null otherwise.
   */
  public Identifier getExchangeId() {
    return _exchangeId;
  }
  
  /**
   * @return a currency, if this is a CURRENCY holiday, null otherwise
   */
  public Currency getCurrency() {
    return _currency;
  }
  
  /**
   * @return the type of holiday: CURRENCY, BANK, SETTLEMENT or TRADING
   */
  public HolidayType getHolidayType() {
    return _holidayType;
  }
  
  /**
   * @return a collection of dates on which holidays fall
   */
  public Collection<LocalDate> getHolidays() {
    return _holidaySeries;
  }
  
  /**
   * @param uniqueIdentifier the unique identifier for this holiday entry
   */
  public void setUniqueIdentifier(UniqueIdentifier uniqueIdentifier) {
    _uniqueIdentifier = uniqueIdentifier;
  }
  
  /**
   * @return the UniqueIdentifier for this holiday entry
   */
  public UniqueIdentifier getUniqueIdentifier() {
    return _uniqueIdentifier;
  }
  
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
