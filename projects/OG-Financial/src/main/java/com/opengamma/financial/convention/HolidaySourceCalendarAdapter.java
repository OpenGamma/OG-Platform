/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.exchange.Exchange;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.core.region.Region;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.id.ExternalId;
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
  private Set<ExternalId> _customIds; // or bundle?

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

  public HolidaySourceCalendarAdapter(final HolidaySource holidaySource, final ExternalId customId) {
    this(holidaySource, new ExternalId[] {customId });
  }

  public HolidaySourceCalendarAdapter(final HolidaySource holidaySource, final ExternalId[] customIds) {
    Validate.notNull(holidaySource);
    Validate.notNull(customIds);
    Validate.noNullElements(customIds);
    _holidaySource = holidaySource;
    _customIds = Sets.newHashSet(customIds);
    _type = HolidayType.CUSTOM;
  }

  @Override
  public String getConventionName() {
    return getName();
  }
  
  @Override
  public String getName() {
    switch (_type) {
      case BANK: {
        final StringBuilder regionName = new StringBuilder();
        final Iterator<Region> regionIter = _regions.iterator();
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
        final StringBuilder ccyName = new StringBuilder();
        final Iterator<Currency> ccyIter = _currencies.iterator();
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
      case CUSTOM:
        return Iterables.toString(_customIds);
    }
    return null;
  }

  @Override
  public boolean isWorkingDay(final LocalDate date) {
    switch (_type) {
      case BANK:
        for (final Region region : _regions) {
          if (_holidaySource.isHoliday(date, HolidayType.BANK, region.getExternalIdBundle())) {
            return false;
          }
        }
        return true;
      case CURRENCY:
        for (final Currency currency : _currencies) {
          if (_holidaySource.isHoliday(date, currency)) {
            return false;
          }
        }
        return true;
      case SETTLEMENT:
        return !_holidaySource.isHoliday(date, _type, _exchange.getExternalIdBundle());
      case TRADING:
        return !_holidaySource.isHoliday(date, _type, _exchange.getExternalIdBundle());
      case CUSTOM:
        for (final ExternalId id : _customIds) {
          if (_holidaySource.isHoliday(date, _type, id)) {
            return false;
          }
        }
        return true;
    }
    throw new OpenGammaRuntimeException("switch doesn't support " + _type);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_currencies == null) ? 0 : _currencies.hashCode());
    result = prime * result + ((_exchange == null) ? 0 : _exchange.hashCode());
    result = prime * result + _holidaySource.hashCode();
    result = prime * result + _regions.hashCode();
    result = prime * result + ((_type == null) ? 0 : _type.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof HolidaySourceCalendarAdapter)) {
      return false;
    }
    final HolidaySourceCalendarAdapter other = (HolidaySourceCalendarAdapter) obj;
    if (_type != other._type) {
      return false;
    }
    if (!ObjectUtils.equals(_currencies, other._currencies)) {
      return false;
    }
    if (!ObjectUtils.equals(_exchange, other._exchange)) {
      return false;
    }
    if (!ObjectUtils.equals(_holidaySource, other._holidaySource)) {
      return false;
    }
    if (!ObjectUtils.equals(_regions, other._regions)) {
      return false;
    }
    return true;
  }

}
