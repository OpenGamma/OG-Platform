/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.Currency;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.world.exchange.Exchange;
import com.opengamma.financial.world.holiday.HolidaySource;
import com.opengamma.financial.world.holiday.HolidayType;
import com.opengamma.financial.world.region.Region;

/**
 * Temporary adapter to make the existing Calendar interface work with the holiday repository.  THIS MUST BE REFACTORED.
 */
public class HolidaySourceCalendarAdapter implements Calendar {
  private final HolidaySource _holidaySource;
  private Region _region;
  private Exchange _exchange;
  private Currency _currency;
  private final HolidayType _type;

  public HolidaySourceCalendarAdapter(final HolidaySource holidaySource, final Region region) {
    Validate.notNull(region);
    Validate.notNull(holidaySource);
    _holidaySource = holidaySource;
    _region = region;
    _type = HolidayType.BANK;
  }

  public HolidaySourceCalendarAdapter(final HolidaySource holidaySource, final Exchange exchange, final HolidayType type) {
    Validate.notNull(holidaySource);
    Validate.notNull(exchange);
    Validate.notNull(type);
    _holidaySource = holidaySource;
    _exchange = exchange;
    _type = type;
  }

  public HolidaySourceCalendarAdapter(final HolidaySource holidaySource, final Currency currency) {
    Validate.notNull(holidaySource);
    Validate.notNull(currency);
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
  public boolean isWorkingDay(final LocalDate date) {
    switch (_type) {
      case BANK:
        if (_region == null || date == null || _type == null || _holidaySource == null) {
          System.err.println("bugger");
        }
        return !_holidaySource.isHoliday(_region.getIdentifiers(), date, _type);
      case CURRENCY:
        return !_holidaySource.isHoliday(_currency, date);
      case SETTLEMENT:
        return !_holidaySource.isHoliday(_exchange.getIdentifiers(), date, _type);
      case TRADING:
        return !_holidaySource.isHoliday(_exchange.getIdentifiers(), date, _type);
    }
    throw new OpenGammaRuntimeException("switch doesn't support " + _type);
  }

}
