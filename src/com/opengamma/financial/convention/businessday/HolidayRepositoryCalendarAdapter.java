/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

import javax.time.calendar.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.Currency;
import com.opengamma.financial.Exchange;
import com.opengamma.financial.HolidayRepository;
import com.opengamma.financial.HolidayType;
import com.opengamma.financial.Region;
import com.opengamma.financial.convention.calendar.Calendar;

/**
 * Temporary adapter to make the existing Calendar interface work with the holiday repository.  THIS MUST BE REFACTORED.
 */
public class HolidayRepositoryCalendarAdapter implements Calendar {
  private HolidayRepository _holidayRepo;
  private Region _region;
  private Exchange _exchange;
  private Currency _currency;
  private HolidayType _type;

  public HolidayRepositoryCalendarAdapter(HolidayRepository holidayRepo, Region region) {
    _holidayRepo = holidayRepo;
    _region = region;
    _type = HolidayType.BANK;
  }
  
  public HolidayRepositoryCalendarAdapter(HolidayRepository holidayRepo, Exchange exchange, HolidayType type) {
    _holidayRepo = holidayRepo;
    _exchange = exchange;
    _type = type;
  }
  
  public HolidayRepositoryCalendarAdapter(HolidayRepository holidayRepo, Currency currency) {
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
        return _holidayRepo.isHoliday(null, _region, date, _type);
      case CURRENCY:
        return _holidayRepo.isHoliday(null, _currency, date, _type);
      case SETTLEMENT:
        return _holidayRepo.isHoliday(null, _exchange, date, _type);
      case TRADING:
        return _holidayRepo.isHoliday(null, _exchange, date, _type);
    }
    throw new OpenGammaRuntimeException("switch doesn't support " + _type);
  }

}
