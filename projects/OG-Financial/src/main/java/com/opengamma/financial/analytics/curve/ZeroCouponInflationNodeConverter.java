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
import com.opengamma.core.link.ConventionLink;
import com.opengamma.core.link.SecurityLink;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
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
   * @param holidaySource The holiday source, not null
   * @param regionSource The region source, not null
   * @param marketData The market data, not null
   * @param dataId The data id, not null
   * @param valuationTime The valuation time, not null
   * @param timeSeries The time series, not null
   */
  public ZeroCouponInflationNodeConverter(HolidaySource holidaySource,
                                          RegionSource regionSource,
                                          SnapshotDataBundle marketData,
                                          ExternalId dataId,
                                          ZonedDateTime valuationTime,
                                          HistoricalTimeSeriesBundle timeSeries) {
    _holidaySource = ArgumentChecker.notNull(holidaySource, "holidaySource");
    _regionSource = ArgumentChecker.notNull(regionSource, "regionSource");
    _marketData = ArgumentChecker.notNull(marketData, "marketData");
    _dataId = ArgumentChecker.notNull(dataId, "dataId");
    _valuationTime = ArgumentChecker.notNull(valuationTime, "valuationTime");
    _timeSeries = ArgumentChecker.notNull(timeSeries, "timeSeries");
  }

  /**
   * @param securitySource The security source. Not required.
   * @param conventionSource The convention source, not required
   * @param holidaySource The holiday source, not null
   * @param regionSource The region source, not null
   * @param marketData The market data, not null
   * @param dataId The data id, not null
   * @param valuationTime The valuation time, not null
   * @param timeSeries The time series, not null
   * @deprecated use constructor without securitySource and conventionSource
   */
  @Deprecated
  public ZeroCouponInflationNodeConverter(
      SecuritySource securitySource, ConventionSource conventionSource, HolidaySource holidaySource,
      RegionSource regionSource, SnapshotDataBundle marketData,
      ExternalId dataId, ZonedDateTime valuationTime, HistoricalTimeSeriesBundle timeSeries) {
    this(holidaySource, regionSource, marketData, dataId, valuationTime, timeSeries);
  }

  @Override
  public InstrumentDefinition<?> visitZeroCouponInflationNode(ZeroCouponInflationNode inflationNode) {
    Double rate = _marketData.getDataPoint(_dataId);
    if (rate == null) {
      throw new OpenGammaRuntimeException("Could not get market data for " + _dataId);
    }
    SwapFixedLegConvention fixedLegConvention =
        ConventionLink.resolvable(inflationNode.getFixedLegConvention(), SwapFixedLegConvention.class).resolve();
    InflationLegConvention inflationLegConvention =
        ConventionLink.resolvable(inflationNode.getInflationLegConvention(), InflationLegConvention.class).resolve();
    PriceIndex indexSecurity =
        SecurityLink.resolvable(inflationLegConvention.getPriceIndexConvention(), PriceIndex.class).resolve();
    PriceIndexConvention priceIndexConvention =
        ConventionLink.resolvable(indexSecurity.getConventionId(), PriceIndexConvention.class).resolve();
    int settlementDays = fixedLegConvention.getSettlementDays();
    Period tenor = inflationNode.getTenor().getPeriod();
    double notional = 1;
    //TODO business day convention and currency are in both conventions - should we enforce that they're the same or use
    // different ones for each leg?
    BusinessDayConvention businessDayConvention = fixedLegConvention.getBusinessDayConvention();
    boolean endOfMonth = fixedLegConvention.isIsEOM();
    Currency currency = priceIndexConvention.getCurrency();
    Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, priceIndexConvention.getRegion());
    ZoneId zone = _valuationTime.getZone(); //TODO time zone set to midnight UTC
    ZonedDateTime settlementDate =
        ScheduleCalculator.getAdjustedDate(_valuationTime, settlementDays, calendar).toLocalDate().atStartOfDay(zone);
    ZonedDateTime paymentDate =
        ScheduleCalculator.getAdjustedDate(settlementDate, tenor, businessDayConvention, calendar, endOfMonth)
            .toLocalDate().atStartOfDay(zone);
    CouponFixedCompoundingDefinition fixedCoupon =
        CouponFixedCompoundingDefinition.from(currency, settlementDate, paymentDate, notional, tenor.getYears(), rate);
    HistoricalTimeSeries ts =
        _timeSeries.get(MarketDataRequirementNames.MARKET_VALUE, priceIndexConvention.getPriceIndexId());
    if (ts == null) {
      throw new OpenGammaRuntimeException(
          "Could not get price index time series with id " + priceIndexConvention.getPriceIndexId());
    }
    int conventionalMonthLag = inflationLegConvention.getMonthLag();
    int monthLag = inflationLegConvention.getMonthLag();

    final IndexPrice indexPrice = new IndexPrice(indexSecurity.getName(), currency);
    switch (inflationNode.getInflationNodeType()) {
      case INTERPOLATED: {
        CouponInflationZeroCouponInterpolationDefinition inflationCoupon =
            CouponInflationZeroCouponInterpolationDefinition.from(
                settlementDate, paymentDate, -notional, indexPrice, conventionalMonthLag, monthLag, false);
        return new SwapFixedInflationZeroCouponDefinition(fixedCoupon, inflationCoupon, calendar);
      }
      case MONTHLY: {
        CouponInflationZeroCouponMonthlyDefinition inflationCoupon =
            CouponInflationZeroCouponMonthlyDefinition.from(
                settlementDate, paymentDate, -notional, indexPrice, conventionalMonthLag, monthLag, false);
        return new SwapFixedInflationZeroCouponDefinition(fixedCoupon, inflationCoupon, calendar);
      }
      default:
        throw new OpenGammaRuntimeException(
            "Could not handle inflation nodes of type " + inflationNode.getInflationNodeType());
    }
  }
}
