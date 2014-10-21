/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import org.threeten.bp.Period;
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
import com.opengamma.financial.analytics.ircurve.strips.FRANode;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * Convert a FRA node into an Instrument definition.
 * The dates of the FRA are computed in the following way:
 * - The spot date is computed from the valuation date adding the "Settlement Days"
 *   (i.e. the number of business days) of the convention.
 * - The accrual start date is computed from the spot date adding the "FixingStart"
 *   of the node and using the business-day-convention, calendar and EOM of the
 *   convention.
 * - The accrual end date is computed from the spot date adding the "FixingEnd" of the
 *   node and using the business-day-convention, calendar and EOM of the convention.
 * The FRA notional is 1.
 */
public class FRANodeConverter extends CurveNodeVisitorAdapter<InstrumentDefinition<?>> {

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
   * @param securitySource The security source, not used
   * @param conventionSource The convention source, not used
   * @param holidaySource The holiday source, not null
   * @param regionSource The region source, not null
   * @param marketData The market data, not null
   * @param dataId The id of the market data, not null
   * @param valuationTime The valuation time, not null
   * @deprecated call constructor without securitySource and conventionSource
   */
  @Deprecated
  public FRANodeConverter(SecuritySource securitySource, ConventionSource conventionSource,
                          HolidaySource holidaySource, RegionSource regionSource,
                          SnapshotDataBundle marketData, ExternalId dataId, ZonedDateTime valuationTime) {
    this(holidaySource, regionSource, marketData, dataId, valuationTime);
  }

  public FRANodeConverter(HolidaySource holidaySource, RegionSource regionSource, SnapshotDataBundle marketData,
                          ExternalId dataId, ZonedDateTime valuationTime) {
    _holidaySource = ArgumentChecker.notNull(holidaySource, "holidaySource");
    _regionSource = ArgumentChecker.notNull(regionSource, "regionSource");
    _marketData = ArgumentChecker.notNull(marketData, "marketData");
    _dataId = ArgumentChecker.notNull(dataId, "dataId");
    _valuationTime = ArgumentChecker.notNull(valuationTime, "valuationTime");
  }

  //TODO check calendars
  @Override
  public InstrumentDefinition<?> visitFRANode(FRANode fraNode) {

    Double rate = _marketData.getDataPoint(_dataId);
    if (rate == null) {
      throw new OpenGammaRuntimeException("Could not get market data for " + _dataId);
    }
    com.opengamma.financial.security.index.IborIndex indexSecurity =
        SecurityLink.resolvable(
            fraNode.getConvention(),
            com.opengamma.financial.security.index.IborIndex.class)
            .resolve();

    IborIndexConvention indexConvention =
        ConventionLink.resolvable(indexSecurity.getConventionId(), IborIndexConvention.class).resolve();

    IborIndex index = ConverterUtils.indexIbor(indexSecurity.getName(), indexConvention, indexSecurity.getTenor());
    Period startPeriod = fraNode.getFixingStart().getPeriod();
    Period endPeriod = fraNode.getFixingEnd().getPeriod();
    Calendar fixingCalendar =
        CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getFixingCalendar());
    Calendar regionCalendar =
        CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getRegionCalendar());
    int spotLag = indexConvention.getSettlementDays();
    ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(_valuationTime, spotLag, regionCalendar);
    ZonedDateTime accrualStartDate = ScheduleCalculator.getAdjustedDate(spotDate, startPeriod, index, regionCalendar);
    ZonedDateTime accrualEndDate = ScheduleCalculator.getAdjustedDate(spotDate, endPeriod, index, regionCalendar);
    return ForwardRateAgreementDefinition.from(accrualStartDate, accrualEndDate, 1, index, rate, fixingCalendar);
  }

}
