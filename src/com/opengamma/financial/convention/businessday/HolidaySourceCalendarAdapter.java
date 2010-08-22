/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

import javax.time.calendar.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.world.Exchange;
import com.opengamma.engine.world.Region;
import com.opengamma.financial.Currency;
import com.opengamma.financial.HolidaySource;
import com.opengamma.financial.HolidayType;
import com.opengamma.financial.convention.calendar.Calendar;

/**
 * Temporary adapter to make the existing Calendar interface work with the holiday repository.  THIS MUST BE REFACTORED.
 */
public class HolidaySourceCalendarAdapter implements Calendar {
  private HolidaySource _holidaySource;
  private Region _region;
  private Exchange _exchange;
  private Currency _currency;
  private HolidayType _type;

  public HolidaySourceCalendarAdapter(HolidaySource holidaySource, Region region) {
    _holidaySource = holidaySource;
    _region = region;
    _type = HolidayType.BANK;
  }
  
  public HolidaySourceCalendarAdapter(HolidaySource holidaySource, Exchange exchange, HolidayType type) {
    _holidaySource = holidaySource;
    _exchange = exchange;
    _type = type;
  }
  
  public HolidaySourceCalendarAdapter(HolidaySource holidaySource, Currency currency) {
    _holidaySource = holidaySource;
    _currency = currency;
    _type = HolidayType.CURRENCY;
  }

  @Override
  public String getConventionName() {
    switch (_type) {
      case BANK:
        return _region.getName() + " Bank";
      case CURRENCY:
        return _currency.getISOCode() + " Currency";
      case SETTLEMENT:
        return _exchange.getName() + " Settlement";
      case TRADING:
        return _exchange.getName() + " Trading";
    }
    return null;
  }

  @Override
  public boolean isWorkingDay(LocalDate date) {
    switch (_type) {
      case BANK:
        return _holidaySource.isHoliday(_region.getIdentifiers(), date, _type);
      case CURRENCY:
        return _holidaySource.isHoliday(_currency, date);
      case SETTLEMENT:
        return _holidaySource.isHoliday(_exchange.getIdentifiers(), date, _type);
      case TRADING:
        return _holidaySource.isHoliday(_exchange.getIdentifiers(), date, _type);
    }
    throw new OpenGammaRuntimeException("switch doesn't support " + _type);
  }

}
