/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import com.opengamma.OpenGammaRuntimeException;

/**
 * A document that holds a Holiday object and any associated meta-data merged into a descriptive name field  
 */
public class HolidayDocument {
  private Holiday _holiday;
  private String _name;
  public HolidayDocument(Holiday holiday) {
    _holiday = holiday;
    switch (holiday.getHolidayType()) {
      case BANK:
        _name = holiday.getRegionId().getScheme().getName() + ":" + holiday.getRegionId().getValue();
        break;
      case CURRENCY:
        _name = holiday.getCurrencyISO();
        break;
      case SETTLEMENT:
      case TRADING:
        _name = holiday.getExchangeId().getScheme().getName() + ":" + holiday.getExchangeId().getValue();
        break;
      default:
        throw new OpenGammaRuntimeException("Unsupported holiday type");
    }
  }
  
  public Holiday getHoliday() {
    return _holiday;
  }
  
  public String getName() {
    return _name;
  }
}
