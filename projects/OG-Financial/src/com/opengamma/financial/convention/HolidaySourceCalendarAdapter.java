/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.exchange.Exchange;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.core.region.Region;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;

/**
 * Temporary adapter to make the existing Calendar interface work with the holiday repository.  THIS MUST BE REFACTORED.
 */
public class HolidaySourceCalendarAdapter implements Calendar, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  private final HolidaySource _holidaySource;
  private Set<Region> _regions;
  private Exchange _exchange;
  private Set<Currency> _currencies;
  private final HolidayType _type;

  public HolidaySourceCalendarAdapter(final HolidaySource holidaySource, final Region[] regions) {
    Validate.notNull(regions, "Region set is null");
    Validate.notNull(holidaySource, "holiday source is null");
    Validate.noNullElements(regions, "Region set has null elements");
    _holidaySource = holidaySource;
    _regions = Sets.newHashSet(regions);
    _type = HolidayType.BANK;
  }

  public HolidaySourceCalendarAdapter(final HolidaySource holidaySource, final Region region) {
    this(holidaySource, new Region[] {region });
  }

  public HolidaySourceCalendarAdapter(final HolidaySource holidaySource, final Exchange exchange, final HolidayType type) {
    Validate.notNull(holidaySource);
    Validate.notNull(exchange);
    Validate.notNull(type);
    _holidaySource = holidaySource;
    _exchange = exchange;
    _type = type;
  }
  
  public HolidaySourceCalendarAdapter(final HolidaySource holidaySource, final Currency[] currencies) {
    Validate.notNull(holidaySource);
    Validate.notNull(currencies);
    _holidaySource = holidaySource;
    _currencies = Sets.newHashSet(currencies);
    _type = HolidayType.CURRENCY;
  }

  public HolidaySourceCalendarAdapter(final HolidaySource holidaySource, final Currency currency) {
    this(holidaySource, new Currency[] {currency });
  }

  @Override
  public String getConventionName() {
    switch (_type) {
      case BANK: {
        StringBuilder regionName = new StringBuilder();
        Iterator<Region> regionIter = _regions.iterator();
        while (regionIter.hasNext()) {
          regionName.append(regionIter.next().getName());
          if (regionIter.hasNext()) {
            regionName.append(", ");
          }  
        }
        regionName.append(" Bank");
        return regionName.toString();
      }
      case CURRENCY: {
        StringBuilder ccyName = new StringBuilder();
        Iterator<Currency> ccyIter = _currencies.iterator();
        while (ccyIter.hasNext()) {
          ccyName.append(ccyIter.next().getCode());
          if (ccyIter.hasNext()) {
            ccyName.append(", ");
          }
        }        
        ccyName.append(" Currency");
        return ccyName.toString();
      }
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
          isHoliday |= _holidaySource.isHoliday(date, _type, region.getExternalIdBundle());
        }
        return !isHoliday;
      case CURRENCY:
        boolean isCurrencyHoliday = false;
        for (final Currency currency : _currencies) {
          isCurrencyHoliday |= _holidaySource.isHoliday(date, currency);
        }
        return !isCurrencyHoliday;
      case SETTLEMENT:
        return !_holidaySource.isHoliday(date, _type, _exchange.getExternalIdBundle());
      case TRADING:
        return !_holidaySource.isHoliday(date, _type, _exchange.getExternalIdBundle());
    }
    throw new OpenGammaRuntimeException("switch doesn't support " + _type);
  }
}
