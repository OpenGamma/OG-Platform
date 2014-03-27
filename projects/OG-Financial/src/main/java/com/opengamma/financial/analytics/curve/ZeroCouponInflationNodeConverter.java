/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

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
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.analytics.ircurve.strips.ZeroCouponInflationNode;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.convention.InflationLegConvention;
import com.opengamma.financial.convention.PriceIndexConvention;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.security.index.PriceIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class ZeroCouponInflationNodeConverter extends CurveNodeVisitorAdapter<InstrumentDefinition<?>> {
  /** The security source */
  private final SecuritySource _securitySource;
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
   * @param securitySource The security source. Not null.
   * @param conventionSource The convention source, not null
   * @param holidaySource The holiday source, not null
   * @param regionSource The region source, not null
   * @param marketData The market data, not null
   * @param dataId The data id, not null
   * @param valuationTime The valuation time, not null
   * @param timeSeries The time series, not null
   */
  public ZeroCouponInflationNodeConverter(final SecuritySource securitySource, final ConventionSource conventionSource, final HolidaySource holidaySource, final RegionSource regionSource,
      final SnapshotDataBundle marketData, final ExternalId dataId, final ZonedDateTime valuationTime, final HistoricalTimeSeriesBundle timeSeries) {
    ArgumentChecker.notNull(securitySource, "security source");
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(regionSource, "region source");
    ArgumentChecker.notNull(marketData, "market data");
    ArgumentChecker.notNull(dataId, "data id");
    ArgumentChecker.notNull(valuationTime, "valuation time");
    ArgumentChecker.notNull(timeSeries, "time series");
    _securitySource = securitySource;
    _conventionSource = conventionSource;
    _holidaySource = holidaySource;
    _regionSource = regionSource;
    _marketData = marketData;
    _dataId = dataId;
    _valuationTime = valuationTime;
    _timeSeries = timeSeries;
  }

  @Override
  public InstrumentDefinition<?> visitZeroCouponInflationNode(final ZeroCouponInflationNode inflationNode) {
    final Double rate = _marketData.getDataPoint(_dataId);
    if (rate == null) {
      throw new OpenGammaRuntimeException("Could not get market data for " + _dataId);
    }
    final SwapFixedLegConvention fixedLegConvention = _conventionSource.getSingle(inflationNode.getFixedLegConvention(), SwapFixedLegConvention.class);
    final InflationLegConvention inflationLegConvention = _conventionSource.getSingle(inflationNode.getInflationLegConvention(), InflationLegConvention.class);
    final Security sec = _securitySource.getSingle(inflationLegConvention.getPriceIndexConvention().toBundle());
    if (sec == null) {
      throw new OpenGammaRuntimeException("CurveNodeCurrencyVisitor.visitInflationLegConvention: index with id " + inflationLegConvention.getPriceIndexConvention()
          + " was null");
    }
    if (!(sec instanceof PriceIndex)) {
      throw new OpenGammaRuntimeException("CurveNodeCurrencyVisitor.visitInflationLegConvention: index with id " + inflationLegConvention.getPriceIndexConvention()
          + " not of type PriceIndex");
    }
    final PriceIndex indexSecurity = (PriceIndex) sec;
    final PriceIndexConvention priceIndexConvention = _conventionSource.getSingle(indexSecurity.getConventionId(), PriceIndexConvention.class);
    if (priceIndexConvention == null) {
      throw new OpenGammaRuntimeException("CurveNodeCurrencyVisitor.visitInflationLegConvention: Convention with id " + indexSecurity.getConventionId() + " was null");
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
    final int conventionalMonthLag = inflationLegConvention.getMonthLag();
    final int monthLag = inflationLegConvention.getMonthLag();

    final IndexPrice indexPrice = new IndexPrice(indexSecurity.getName(), currency);
    switch (inflationNode.getInflationNodeType()) {
      case INTERPOLATED: {
        final CouponInflationZeroCouponInterpolationDefinition inflationCoupon = CouponInflationZeroCouponInterpolationDefinition.from(settlementDate, paymentDate,
            -notional, indexPrice, conventionalMonthLag, monthLag, false);
        return new SwapFixedInflationZeroCouponDefinition(fixedCoupon, inflationCoupon, calendar);
      }
      case MONTHLY: {
        final CouponInflationZeroCouponMonthlyDefinition inflationCoupon = CouponInflationZeroCouponMonthlyDefinition.from(settlementDate, paymentDate, -notional,
            indexPrice, conventionalMonthLag, monthLag, false);
        return new SwapFixedInflationZeroCouponDefinition(fixedCoupon, inflationCoupon, calendar);
      }
      default:
        throw new OpenGammaRuntimeException("Could not handle inflation nodes of type " + inflationNode.getInflationNodeType());
    }
  }
}
