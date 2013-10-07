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
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.analytics.ircurve.strips.IMMFRANode;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.convention.IMMFRAConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.RollDateAdjuster;
import com.opengamma.financial.convention.RollDateAdjusterFactory;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public class IMMFRANodeConverter extends CurveNodeVisitorAdapter<InstrumentDefinition<?>> {
  /** The convention source */
  private final ConventionSource _conventionSource;
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
   * @param conventionSource The convention source, not null
   * @param holidaySource The holiday source, not null
   * @param regionSource The region source, not null
   * @param marketData The market data, not null
   * @param dataId The id of the market data, not null
   * @param valuationTime The valuation time, not null
   */
  public IMMFRANodeConverter(final ConventionSource conventionSource, final HolidaySource holidaySource, final RegionSource regionSource,
      final SnapshotDataBundle marketData, final ExternalId dataId, final ZonedDateTime valuationTime) {
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(regionSource, "region source");
    ArgumentChecker.notNull(marketData, "market data");
    ArgumentChecker.notNull(dataId, "data id");
    ArgumentChecker.notNull(valuationTime, "valuation time");
    _conventionSource = conventionSource;
    _holidaySource = holidaySource;
    _regionSource = regionSource;
    _marketData = marketData;
    _dataId = dataId;
    _valuationTime = valuationTime;
  }

  @Override
  public InstrumentDefinition<?> visitIMMFRANode(final IMMFRANode immFRANode) {
    final Double rate = _marketData.getDataPoint(_dataId);
    if (rate == null) {
      throw new OpenGammaRuntimeException("Could not get market data for " + _dataId);
    }
    final IMMFRAConvention convention = _conventionSource.getConvention(IMMFRAConvention.class, immFRANode.getConvention());
    if (convention == null) {
      throw new OpenGammaRuntimeException("Convention with id " + immFRANode.getConvention() + " was null");
    }
    final IborIndexConvention indexConvention = _conventionSource.getConvention(IborIndexConvention.class, convention.getIndexConvention());
    if (indexConvention == null) {
      throw new OpenGammaRuntimeException("Underlying ibor convention with id " + convention.getIndexConvention() + " was null");
    }
    final RollDateAdjuster adjuster = RollDateAdjusterFactory.getAdjuster(convention.getImmDateConvention().getValue());
    final Tenor indexTenor = immFRANode.getImmTenor();
    final long monthsToAdjust = adjuster.getMonthsToAdjust();
    final ZonedDateTime unadjustedStartDate = _valuationTime.plus(immFRANode.getStartTenor().getPeriod());
    final ZonedDateTime immStartDate = unadjustedStartDate.plusMonths(monthsToAdjust * immFRANode.getImmDateStartNumber()).with(adjuster);
    final ZonedDateTime maturityDate = immStartDate.plusMonths(monthsToAdjust * immFRANode.getImmDateEndNumber());
    final Currency currency = indexConvention.getCurrency();
    final Calendar fixingCalendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getFixingCalendar());
    final int spotLag = indexConvention.getSettlementDays();
    final BusinessDayConvention businessDayConvention = indexConvention.getBusinessDayConvention();
    final DayCount dayCount = indexConvention.getDayCount();
    final boolean eom = indexConvention.isIsEOM();
    final IborIndex iborIndex = new IborIndex(currency, indexTenor.getPeriod(), spotLag, dayCount, businessDayConvention, eom, indexConvention.getName());
    return ForwardRateAgreementDefinition.from(immStartDate, maturityDate, 1, iborIndex, rate, fixingCalendar);
  }
}
