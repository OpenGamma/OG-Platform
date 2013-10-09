/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedInflationZeroCouponDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.analytics.ircurve.strips.ZeroCouponInflationNode;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.convention.InflationLegConvention;
import com.opengamma.financial.convention.PriceIndexConvention;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.id.ExternalId;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleEntryIterator;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeriesBuilder;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class ZeroCouponInflationNodeConverter extends CurveNodeVisitorAdapter<InstrumentDefinition<?>> {
  /** The convention source */
  private final ConventionSource _conventionSource;
  /** The holiday source */
  private final HolidaySource _holidaySource;
  /** The region source */
  private final RegionSource _regionSource;
  /** The market data */
  private final SnapshotDataBundle _marketData;
  /** The data id */
  private final ExternalId _dataId;
  /** The valuation time */
  private final ZonedDateTime _valuationTime;
  /** The time series bundle */
  private final HistoricalTimeSeriesBundle _timeSeries;

  /**
   * @param conventionSource The convention source, not null
   * @param holidaySource The holiday source, not null
   * @param regionSource The region source, not null
   * @param marketData The market data, not null
   * @param dataId The data id, not null
   * @param valuationTime The valuation time, not null
   * @param timeSeries The time series, not null
   */
  public ZeroCouponInflationNodeConverter(final ConventionSource conventionSource, final HolidaySource holidaySource, final RegionSource regionSource,
      final SnapshotDataBundle marketData, final ExternalId dataId, final ZonedDateTime valuationTime, final HistoricalTimeSeriesBundle timeSeries) {
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(regionSource, "region source");
    ArgumentChecker.notNull(marketData, "market data");
    ArgumentChecker.notNull(dataId, "data id");
    ArgumentChecker.notNull(valuationTime, "valuation time");
    ArgumentChecker.notNull(timeSeries, "time series");
    _conventionSource = conventionSource;
    _holidaySource = holidaySource;
    _regionSource = regionSource;
    _marketData = marketData;
    _dataId = dataId;
    _valuationTime = valuationTime;
    _timeSeries = timeSeries;
  }

  @SuppressWarnings({"synthetic-access" })
  @Override
  public InstrumentDefinition<?> visitZeroCouponInflationNode(final ZeroCouponInflationNode inflationNode) {
    final Double rate = _marketData.getDataPoint(_dataId);
    if (rate == null) {
      throw new OpenGammaRuntimeException("Could not get market data for " + _dataId);
    }
    final SwapFixedLegConvention fixedLegConvention = _conventionSource.getConvention(SwapFixedLegConvention.class, inflationNode.getFixedLegConvention());
    if (fixedLegConvention == null) {
      throw new OpenGammaRuntimeException("Convention with id " + inflationNode.getFixedLegConvention() + " was null");
    }
    final InflationLegConvention inflationLegConvention = _conventionSource.getConvention(InflationLegConvention.class, inflationNode.getInflationLegConvention());
    if (inflationLegConvention == null) {
      throw new OpenGammaRuntimeException("Convention with id " + inflationNode.getInflationLegConvention() + " was null");
    }
    final PriceIndexConvention priceIndexConvention = _conventionSource.getConvention(PriceIndexConvention.class, inflationLegConvention.getPriceIndexConvention());
    if (priceIndexConvention == null) {
      throw new OpenGammaRuntimeException("Convention with id " + inflationLegConvention.getPriceIndexConvention() + " was null");
    }
    final int settlementDays = fixedLegConvention.getSettlementDays();
    final Period tenor = inflationNode.getTenor().getPeriod();
    final double notional = 1;
    //TODO business day convention and currency are in both conventions - should we enforce that they're the same or use
    // different ones for each leg?
    final BusinessDayConvention businessDayConvention = fixedLegConvention.getBusinessDayConvention();
    final boolean endOfMonth = fixedLegConvention.isIsEOM();
    final Currency currency = priceIndexConvention.getCurrency();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, priceIndexConvention.getRegion());
    final ZoneId zone = _valuationTime.getZone(); //TODO time zone set to midnight UTC
    final ZonedDateTime settlementDate = ScheduleCalculator.getAdjustedDate(_valuationTime, settlementDays, calendar).toLocalDate().atStartOfDay(zone);
    final ZonedDateTime paymentDate = ScheduleCalculator.getAdjustedDate(settlementDate, tenor, businessDayConvention, calendar, endOfMonth).toLocalDate().atStartOfDay(zone);
    final CouponFixedCompoundingDefinition fixedCoupon = CouponFixedCompoundingDefinition.from(currency, settlementDate, paymentDate, notional, tenor.getYears(),
        rate);
    final HistoricalTimeSeries ts = _timeSeries.get(MarketDataRequirementNames.MARKET_VALUE, priceIndexConvention.getPriceIndexId());
    if (ts == null) {
      throw new OpenGammaRuntimeException("Could not get price index time series with id " + priceIndexConvention.getPriceIndexId());
    }
    final LocalDateDoubleTimeSeries localDateTS = ts.getTimeSeries();
    final DoubleTimeSeries<ZonedDateTime> priceIndexTimeSeries = convertTimeSeries(zone, localDateTS);
    final int conventionalMonthLag = inflationLegConvention.getMonthLag();
    final int monthLag = inflationLegConvention.getMonthLag();
    final IndexPrice index = new IndexPrice(priceIndexConvention.getName(), currency);
    switch (inflationNode.getInflationNodeType()) {
      case INTERPOLATED:
      {
        final CouponInflationZeroCouponInterpolationDefinition inflationCoupon = CouponInflationZeroCouponInterpolationDefinition.from(settlementDate, paymentDate,
            -notional, index, conventionalMonthLag, monthLag, false);
        return new SwapFixedInflationZeroCouponDefinition(fixedCoupon, inflationCoupon, calendar);
      }
      case MONTHLY:
      {
        final CouponInflationZeroCouponMonthlyDefinition inflationCoupon = CouponInflationZeroCouponMonthlyDefinition.from(settlementDate, paymentDate, -notional,
            index, conventionalMonthLag, monthLag, false);
        return new SwapFixedInflationZeroCouponDefinition(fixedCoupon, inflationCoupon, calendar);
      }
      default:
        throw new OpenGammaRuntimeException("Could not handle inflation nodes of type " + inflationNode.getInflationNodeType());
    }
  }

  private static ZonedDateTimeDoubleTimeSeries convertTimeSeries(final ZoneId timeZone, final LocalDateDoubleTimeSeries localDateTS) {
    // FIXME Converting a daily historical time series to an arbitrary time - should not happen
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(timeZone);
    for (final LocalDateDoubleEntryIterator it = localDateTS.iterator(); it.hasNext(); ) {
      final LocalDate date = it.nextTime();
      final ZonedDateTime zdt = date.atStartOfDay(timeZone);
      bld.put(zdt, it.currentValueFast());
    }
    return bld.build();
  }
}
