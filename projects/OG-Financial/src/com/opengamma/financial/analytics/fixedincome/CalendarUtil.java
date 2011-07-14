/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fixedincome;

import java.util.HashSet;
import java.util.Set;

import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.region.RegionUtils;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.id.Identifier;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class CalendarUtil {

  public static Calendar getCalendar(final RegionSource regionSource, final HolidaySource holidaySource,
      final Identifier regionId) {
    if (regionId.isScheme(RegionUtils.FINANCIAL) && regionId.getValue().contains("+")) {
      final String[] regions = regionId.getValue().split("\\+");
      final Set<Region> resultRegions = new HashSet<Region>();
      for (final String region : regions) {
        resultRegions.add(regionSource.getHighestLevelRegion(RegionUtils.financialRegionId(region)));
      }
      return new HolidaySourceCalendarAdapter(holidaySource, resultRegions);
    } else {
      final Region region = regionSource.getHighestLevelRegion(regionId); // we've checked that they are the same.
      return new HolidaySourceCalendarAdapter(holidaySource, region);
    }
  }

  public static Calendar getCalendar(final HolidaySource holidaySource, final Currency currency) {
    return new HolidaySourceCalendarAdapter(holidaySource, currency);
  }
}
