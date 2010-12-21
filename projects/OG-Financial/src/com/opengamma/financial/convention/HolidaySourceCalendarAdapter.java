/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import java.io.Serializable;
import java.util.Set;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.common.Currency;
import com.opengamma.core.exchange.Exchange;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.core.region.Region;
import com.opengamma.financial.convention.calendar.Calendar;

/**
 * Temporary adapter to make the existing Calendar interface work with the holiday repository.  THIS MUST BE REFACTORED.
 */
public class HolidaySourceCalendarAdapter implements Calendar, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  private final HolidaySource _holidaySource;
  private Set<Region> _regions;
  private Exchange _exchange;
  private Currency _currency;
  private final HolidayType _type;

  public HolidaySourceCalendarAdapter(final HolidaySource holidaySource, final Set<Region> region) {
    Validate.notNull(region);
    Validate.notNull(holidaySource);
    Validate.noNullElements(region);
    _holidaySource = holidaySource;
    _regions = region;
    _type = HolidayType.BANK;
  }

  public HolidaySourceCalendarAdapter(final HolidaySource holidaySource, final Region region) {
    this(holidaySource, Sets.newHashSet(region));
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
        String name = "";
        for (final Region region : _regions) {
          name += region.getName() + ", ";
        }
        return name + "Bank";
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
        boolean isHoliday = false;
        for (final Region region : _regions) {
          isHoliday |= _holidaySource.isHoliday(date, _type, region.getIdentifiers());
        }
        return !isHoliday;
      case CURRENCY:
        return !_holidaySource.isHoliday(date, _currency);
      case SETTLEMENT:
        return !_holidaySource.isHoliday(date, _type, _exchange.getIdentifiers());
      case TRADING:
        return !_holidaySource.isHoliday(date, _type, _exchange.getIdentifiers());
    }
    throw new OpenGammaRuntimeException("switch doesn't support " + _type);
  }
}
