/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.converter;

import org.threeten.bp.Period;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Converts an {@link IborIndexConvention} to an {@IborIndex}.
 */
public class IborIndexConverter {
  private final RegionSource _regionSource;
  private final HolidaySource _holidaySource;

  /**
   * @param regionSource The region source, not null
   * @param holidaySource The holiday source, not null
   */
  public IborIndexConverter(final RegionSource regionSource, final HolidaySource holidaySource) {
    ArgumentChecker.notNull(regionSource, "region source");
    ArgumentChecker.notNull(holidaySource, "holiday source");
    _regionSource = regionSource;
    _holidaySource = holidaySource;
  }

  /**
   * Performs the conversion.
   * @param convention The convention, not null
   * @param tenor The tenor of the ibor rate, not null
   * @return An ibor index convention for use in the analytics library
   */
  public IborIndex convert(final IborIndexConvention convention, final Tenor tenor) {
    ArgumentChecker.notNull(convention, "convention");
    ArgumentChecker.notNull(tenor, "tenor");
    final Currency currency = convention.getCurrency();
    final Period period = tenor.getPeriod();
    final int spotLag = convention.getDaysToSettle();
    final DayCount dayCount = convention.getDayCount();
    final BusinessDayConvention businessDayConvention = convention.getBusinessDayConvention();
    final boolean isEOM = convention.isIsEOM();
    final String name = convention.getName();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, convention.getRegionCalendar());
    return new IborIndex(currency, period, spotLag, calendar, dayCount, businessDayConvention, isEOM, name);
  }
}
