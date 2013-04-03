/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.converter;

import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Converts an {@link OvernightIndexConvention} to an {@IndexON}.
 */
public class OvernightIndexConverter {
  private final RegionSource _regionSource;
  private final HolidaySource _holidaySource;

  /**
   * @param regionSource The region source, not null
   * @param holidaySource The holiday source, not null
   */
  public OvernightIndexConverter(final RegionSource regionSource, final HolidaySource holidaySource) {
    ArgumentChecker.notNull(regionSource, "region source");
    ArgumentChecker.notNull(holidaySource, "holiday source");
    _regionSource = regionSource;
    _holidaySource = holidaySource;
  }

  /**
   * Performs the conversion.
   * @param convention The convention, not null
   * @return An overnight index convention for use in the analytics library
   */
  public IndexON convert(final OvernightIndexConvention convention) {
    ArgumentChecker.notNull(convention, "convention");
    final String name = convention.getName();
    final Currency currency = convention.getCurrency();
    final DayCount dayCount = convention.getDayCount();
    final int publicationLag = convention.getPublicationLag();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, convention.getRegionCalendar());
    return new IndexON(name, currency, dayCount, publicationLag, calendar);
  }
}
