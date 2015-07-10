/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositIborDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.link.ConventionLink;
import com.opengamma.core.link.SecurityLink;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Convert a cash node into an Instrument definition.
 * The dates of the deposits are computed in the following way: 
 * - The spot date is computed from the valuation date adding the "Settlement Days"
 *   (i.e. the number of business days) of the convention.
 * - The start date is computed from the spot date adding the "StartTenor" of the node
 *   and using the business-day-convention, calendar and EOM of the convention.
 * - The end date is computed from the start date adding the "MaturityTenor" of the
 *   node and using the business-day-convention, calendar and EOM of the convention.
 * The deposit notional is 1.
 */
public class CashNodeConverter extends CurveNodeVisitorAdapter<InstrumentDefinition<?>> {

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
  public CashNodeConverter(
      SecuritySource securitySource,
      ConventionSource conventionSource,
      HolidaySource holidaySource,
      RegionSource regionSource,
      SnapshotDataBundle marketData,
      ExternalId dataId,
      ZonedDateTime valuationTime) {
    this(holidaySource, regionSource, marketData, dataId, valuationTime);
  }

  public CashNodeConverter(
      HolidaySource holidaySource,
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
  public InstrumentDefinition<?> visitCashNode(CashNode cashNode) {
    Tenor startTenor = cashNode.getStartTenor();
    Tenor maturityTenor = cashNode.getMaturityTenor();
    Double rate = _marketData.getDataPoint(_dataId);
    if (rate == null) {
      throw new OpenGammaRuntimeException("Could not get market data for " + _dataId);
    }
    // Deposit
    try {
      DepositConvention depositConvention =
          ConventionLink.resolvable(cashNode.getConvention(), DepositConvention.class).resolve();

      Currency currency = depositConvention.getCurrency();
      Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, depositConvention.getRegionCalendar());
      BusinessDayConvention businessDayConvention = depositConvention.getBusinessDayConvention();
      boolean isEOM = depositConvention.isIsEOM();
      DayCount dayCount = depositConvention.getDayCount();
      int settlementDays = depositConvention.getSettlementDays();
      ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(_valuationTime, settlementDays, calendar);
      ZonedDateTime startDate =
          ScheduleCalculator.getAdjustedDate(spotDate, startTenor, businessDayConvention, calendar, isEOM);
      ZonedDateTime endDate =
          ScheduleCalculator.getAdjustedDate(startDate, maturityTenor, businessDayConvention, calendar, isEOM);
      double accrualFactor = dayCount.getDayCountFraction(startDate, endDate);
      return new CashDefinition(currency, startDate, endDate, 1, rate, accrualFactor);

    } catch (DataNotFoundException e) {
      // If the convention is not found in the convention source
      // then try with the security source.
      IborIndex indexSecurity = SecurityLink.resolvable(cashNode.getConvention().toBundle(), IborIndex.class).resolve();

      IborIndexConvention indexConvention =
          ConventionLink.resolvable(indexSecurity.getConventionId(), IborIndexConvention.class).resolve();
      Currency currency = indexConvention.getCurrency();
      Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getRegionCalendar());
      BusinessDayConvention businessDayConvention = indexConvention.getBusinessDayConvention();
      boolean isEOM = indexConvention.isIsEOM();
      DayCount dayCount = indexConvention.getDayCount();
      int settlementDays = indexConvention.getSettlementDays();
      ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(_valuationTime, settlementDays, calendar);
      ZonedDateTime startDate =
          ScheduleCalculator.getAdjustedDate(spotDate, startTenor, businessDayConvention, calendar, isEOM);
      ZonedDateTime endDate =
          ScheduleCalculator.getAdjustedDate(startDate, maturityTenor, businessDayConvention, calendar, isEOM);
      double accrualFactor = dayCount.getDayCountFraction(startDate, endDate);
      com.opengamma.analytics.financial.instrument.index.IborIndex index =
          ConverterUtils.indexIbor(indexSecurity.getName(), indexConvention, indexSecurity.getTenor());
      return new DepositIborDefinition(currency, startDate, endDate, 1, rate, accrualFactor, index);
    }
  }

}
