/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.link.ConventionLink;
import com.opengamma.core.link.SecurityLink;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.analytics.ircurve.strips.RollDateFRANode;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.RollDateFRAConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.rolldate.RollDateAdjuster;
import com.opengamma.financial.convention.rolldate.RollDateAdjusterFactory;
import com.opengamma.financial.convention.rolldate.RollDateAdjusterUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class RollDateFRANodeConverter extends CurveNodeVisitorAdapter<InstrumentDefinition<?>> {
  /** The holiday source */
  private final HolidaySource _holidaySource;
  /** The region source */
  private final RegionSource _regionSource;
  /** The market data */
  private final SnapshotDataBundle _marketData;
  /** The market data id */
  private final ExternalId _dataId;
  /** The valuation time */
  private final ZonedDateTime _valuationTime;

  /**
   * @param securitySource The security source, not required
   * @param conventionSource The convention source, not required
   * @param holidaySource The holiday source, not null
   * @param regionSource The region source, not null
   * @param marketData The market data, not null
   * @param dataId The id of the market data, not null
   * @param valuationTime The valuation time, not null
   * @deprecated use constructor without securitySource and conventionSource
   */
  @Deprecated
  public RollDateFRANodeConverter(SecuritySource securitySource, ConventionSource conventionSource,
                                  HolidaySource holidaySource, RegionSource regionSource,
      SnapshotDataBundle marketData, ExternalId dataId, ZonedDateTime valuationTime) {
    this(holidaySource, regionSource, marketData, dataId, valuationTime);
  }

  public RollDateFRANodeConverter(HolidaySource holidaySource,
                                  RegionSource regionSource,
                                  SnapshotDataBundle marketData,
                                  ExternalId dataId,
                                  ZonedDateTime valuationTime) {
    _holidaySource = ArgumentChecker.notNull(holidaySource, "holidaySource");
    _regionSource = ArgumentChecker.notNull(regionSource, "regionSource");
    _marketData = ArgumentChecker.notNull(marketData, "marketData");
    _dataId = ArgumentChecker.notNull(dataId, "dataId");
    _valuationTime = ArgumentChecker.notNull(valuationTime, "valuationTime");
  }

  @Override
  public InstrumentDefinition<?> visitRollDateFRANode(RollDateFRANode rollDateFRANode) {
    Double rate = _marketData.getDataPoint(_dataId);
    if (rate == null) {
      throw new OpenGammaRuntimeException("Could not get market data for " + _dataId);
    }
    RollDateFRAConvention convention =
        ConventionLink.resolvable(rollDateFRANode.getRollDateFRAConvention(), RollDateFRAConvention.class).resolve();

    com.opengamma.financial.security.index.IborIndex indexSecurity =
        SecurityLink.resolvable(
            convention.getIndexConvention(),
            com.opengamma.financial.security.index.IborIndex.class)
            .resolve();

    IborIndexConvention indexConvention =
        ConventionLink.resolvable(indexSecurity.getConventionId(), IborIndexConvention.class).resolve();

    IborIndex index = ConverterUtils.indexIbor(indexSecurity.getName(), indexConvention, indexSecurity.getTenor());
    Calendar fixingCalendar =
        CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getFixingCalendar());
    RollDateAdjuster adjuster = RollDateAdjusterFactory.getAdjuster(convention.getRollDateConvention().getValue());
    ZonedDateTime adjustedStartDate = ScheduleCalculator.getAdjustedDate(
        _valuationTime.plus(rollDateFRANode.getStartTenor().getPeriod()), 0, fixingCalendar);

    // Implementation note: Date adjustment to following
    ZonedDateTime immStartDate =
        RollDateAdjusterUtils.nthDate(adjustedStartDate, adjuster, rollDateFRANode.getRollDateStartNumber());

    ZonedDateTime immEndDate =
        RollDateAdjusterUtils.nthDate(
            immStartDate.plusDays(1), adjuster,
            rollDateFRANode.getRollDateEndNumber() - rollDateFRANode.getRollDateStartNumber());

    immStartDate = ScheduleCalculator.getAdjustedDate(immStartDate, 0, fixingCalendar);
    immEndDate = ScheduleCalculator.getAdjustedDate(immEndDate, 0, fixingCalendar);
    Currency currency = indexConvention.getCurrency();
    DayCount dayCount = indexConvention.getDayCount();
    double accrualFactor = dayCount.getDayCountFraction(immStartDate, immEndDate);
    ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(immStartDate, -index.getSpotLag(), fixingCalendar);
    return new ForwardRateAgreementDefinition(
        currency, immStartDate, immStartDate, immEndDate, accrualFactor, 1, fixingDate,
        immStartDate, immEndDate, index, rate, fixingCalendar);
  }
  
}
