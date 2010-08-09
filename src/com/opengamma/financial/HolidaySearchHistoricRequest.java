/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import javax.time.Instant;
import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.Identifier;

/**
 * Holds the parameters for a historic holiday search
 */
public class HolidaySearchHistoricRequest {
  private Currency _currency;
  private LocalDate _holidayDate;
  private HolidayType _holidayType;
  private Identifier _regionId;
  private Identifier _exchangeId;
  private Instant _version;
  private Instant _correction;

  /**
   * Constructor for a currency-specific holiday date series lookup (holiday type is inferred to be CURRENCY)
   * The result will have the a collection of matching Holiday documents, which will contain the full schedule
   * the holidayDate parameter
   * @param version the in history, from the point of view of the correction date.  null is interpreted to mean 'now'
   * @param correction the point from which the history is viewed.  null is interpreted to mean 'now'
   * @param currency of holiday calendar
   */
  public HolidaySearchHistoricRequest(Instant version, Instant correction, Currency currency) {
    Validate.notNull(currency, "Currency");
    _currency = currency;
    _version = version;
    _correction = correction;
    _holidayType = HolidayType.CURRENCY;
  }
  
  /**
   * Constructor for a currency-specific holiday single-date point lookup (holiday type is inferred to be CURRENCY)
   * The result will have the boolean isHoliday set accordingly and the holiday date will be set to the same as
   * the holidayDate parameter
   * @param version the in history, from the point of view of the correction date.  null is interpreted to mean 'now'
   * @param correction the point from which the history is viewed.  null is interpreted to mean 'now'   * @param currency of holiday calendar
   * @param currency of holiday calendar
   * @param holidayDate date you're interested in.
   */
  public HolidaySearchHistoricRequest(Instant version, Instant correction, Currency currency, LocalDate holidayDate) {
    Validate.notNull(currency, "Currency");
    Validate.notNull(holidayDate, "Holiday Date");
    _currency = currency;
    _version = version;
    _correction = correction;
    _holidayDate = holidayDate;
    _holidayType = HolidayType.CURRENCY;
  }
  
  

  /**
   * Constructor for an exchange or region id specific holiday date series lookup
   * The result will have the a collection of matching Holiday documents, which will contain the full schedule
   * @param version the in history, from the point of view of the correction date.  null is interpreted to mean 'now'
   * @param correction the point from which the history is viewed.  null is interpreted to mean 'now'
   * @param exchangeOrRegionId an Identifier for the region or exchange this holiday is associated with
   * @param holidayType the type of holiday
   */
  public HolidaySearchHistoricRequest(Instant version, Instant correction, Identifier exchangeOrRegionId, HolidayType holidayType) {
    Validate.notNull(exchangeOrRegionId, "Exchange or Region Id");
    Validate.notNull(holidayType, "Holiday Type");
    _version = version;
    _correction = correction;
    _holidayType = holidayType;
    switch (holidayType) {
      case CURRENCY:
        throw new OpenGammaRuntimeException("Use currency constructor to request a currency holiday");
      case BANK:
        _regionId = exchangeOrRegionId;
        break;
      case TRADING:
      case SETTLEMENT:
        _exchangeId = exchangeOrRegionId;
        break;
    }
  }
  
  /**
   * Constructor for an exchange or region id specific holiday single-date point lookup
   * The result will have the boolean isHoliday set accordingly and the holiday date will be set to the same as
   * the holidayDate parameter
   * @param version the in history, from the point of view of the correction date.  null is interpreted to mean 'now'
   * @param correction the point from which the history is viewed.  null is interpreted to mean 'now'
   * @param exchangeOrRegionId an Identifier for the region or exchange this holiday is associated with
   * @param holidayType the type of holiday
   * @param holidayDate the date to check for a holiday
   */
  public HolidaySearchHistoricRequest(Instant version, Instant correction, Identifier exchangeOrRegionId, HolidayType holidayType, LocalDate holidayDate) {
    Validate.notNull(exchangeOrRegionId, "Exchange or Region Id");
    Validate.notNull(holidayDate, "Holiday Date");
    Validate.notNull(holidayType, "Holiday Type");
    _version = version;
    _correction = correction;
    _holidayDate = holidayDate;
    _holidayType = holidayType;
    switch (holidayType) {
      case CURRENCY:
        throw new OpenGammaRuntimeException("Use currency constructor to request a currency holiday");
      case BANK:
        _regionId = exchangeOrRegionId;
        break;
      case TRADING:
      case SETTLEMENT:
        _exchangeId = exchangeOrRegionId;
        break;
    }
  }
  
  /**
   * @return the type of holiday
   */
  public HolidayType getHolidayType() {
    return _holidayType;
  }
  
  /**
   * @return the Currency if a CURRENCY holiday, or null otherwise 
   */
  public Currency getCurrency() {
    return _currency;
  }
  
  /**
   * @return the region id if a BANK holiday, or null otherwise
   */
  public Identifier getRegionId() {
    return _regionId;
  }
  
  /**
   * @return the exchangeId if a SETTLEMENT or TRADING holiday, or null otherwise
   */
  public Identifier getExchangeId() {
    return _exchangeId;
  }
  
  /**
   * @return the holiday date for a single-point lookup or null
   */
  public LocalDate getHolidayDate() {
    return _holidayDate;
  }
  
  /**
   * @return the version instant
   */
  public Instant getVersion() {
    return _version;
  }
  
  /**
   * @return the correction instant
   */
  public Instant getCorrection() {
    return _correction;
  }
}
