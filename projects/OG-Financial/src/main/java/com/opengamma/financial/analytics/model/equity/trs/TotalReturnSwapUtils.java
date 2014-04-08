/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.trs;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.security.irs.FloatingInterestRateSwapLeg;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleEntryIterator;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeriesBuilder;
import com.opengamma.util.ArgumentChecker;

/**
 * Utility methods for total return swaps.
 */
public class TotalReturnSwapUtils {


  /**
   * Gets the index time series and adds zone and time information.
   * @param leg The funding leg, not null
   * @param swapEffectiveDate The effective date of the swap, not null
   * @param now The valuation time, not null
   * @param timeSeries The time series bundle, not null
   * @return The time series with zone and time information added.
   */
  public static ZonedDateTimeDoubleTimeSeries getIndexTimeSeries(final FloatingInterestRateSwapLeg leg, final LocalDate swapEffectiveDate, final ZonedDateTime now,
      final HistoricalTimeSeriesBundle timeSeries) {
    ArgumentChecker.notNull(leg, "leg");
    ArgumentChecker.notNull(swapEffectiveDate, "swapEffectiveDate");
    ArgumentChecker.notNull(now, "now");
    ArgumentChecker.notNull(timeSeries, "timeSeries");
    final FloatingInterestRateSwapLeg floatingLeg = leg;
    if (now.toLocalDate().isBefore(swapEffectiveDate)) {
      return ImmutableZonedDateTimeDoubleTimeSeries.ofEmpty(now.getZone());
    }
    final ExternalIdBundle id = ExternalIdBundle.of(floatingLeg.getFloatingReferenceRateId());
    final HistoricalTimeSeries ts = timeSeries.get(MarketDataRequirementNames.MARKET_VALUE, id);
    if (ts == null) {
      return ImmutableZonedDateTimeDoubleTimeSeries.ofEmpty(now.getZone());
    }
    if (ts.getTimeSeries().isEmpty()) {
      return ImmutableZonedDateTimeDoubleTimeSeries.ofEmpty(now.getZone());
    }
    return convertTimeSeries(now.getZone(), ts.getTimeSeries());
  }

  /**
   * Converts a local date-based time series to a zoned date time-based series.
   * @param timeZone The time zone, not null
   * @param localDateTS The local date time series, not null
   * @return A zoned date time series
   */
  public static ZonedDateTimeDoubleTimeSeries convertTimeSeries(final ZoneId timeZone, final LocalDateDoubleTimeSeries localDateTS) {
    ArgumentChecker.notNull(timeZone, "timeZone");
    ArgumentChecker.notNull(localDateTS, "localDateTS");
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(timeZone);
    for (final LocalDateDoubleEntryIterator it = localDateTS.iterator(); it.hasNext();) {
      final LocalDate date = it.nextTime();
      final ZonedDateTime zdt = date.atStartOfDay(timeZone);
      bld.put(zdt, it.currentValueFast());
    }
    return bld.build();
  }
}
