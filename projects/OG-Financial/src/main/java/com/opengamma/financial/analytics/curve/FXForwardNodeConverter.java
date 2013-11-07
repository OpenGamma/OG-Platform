/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.analytics.ircurve.strips.FXForwardNode;
import com.opengamma.financial.convention.FXForwardAndSwapConvention;
import com.opengamma.financial.convention.FXSpotConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Convert a Forex Forward node into an Instrument definition.
 * The dates of the forward are computed in the following way:
 * - The spot date is computed from the valuation date adding the "Settlement Days" (i.e. the number of business days) of the convention.
 * - The exchange date is computed from the spot date adding the "MaturityTenor" of the node and using the business-day-convention, calendar and EOM of the convention.
 * - The "startTenor" is not used.
 * The forward amount in the pay currency is 1 and in the receive currency -quote (e.g. - (spot+forward points)).
 */
public class FXForwardNodeConverter extends CurveNodeVisitorAdapter<InstrumentDefinition<?>> {
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
  public FXForwardNodeConverter(final ConventionSource conventionSource, final HolidaySource holidaySource, final RegionSource regionSource,
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
  public InstrumentDefinition<?> visitFXForwardNode(final FXForwardNode fxForward) {
    final ExternalId conventionId = fxForward.getFxForwardConvention();
    final Double forward = _marketData.getDataPoint(_dataId);
    if (forward == null) {
      throw new OpenGammaRuntimeException("Could not get market data for " + _dataId);
    }
    final FXForwardAndSwapConvention convention = _conventionSource.getSingle(conventionId, FXForwardAndSwapConvention.class);
    final FXForwardAndSwapConvention forwardConvention = convention;
    final ExternalId underlyingConventionId = forwardConvention.getSpotConvention();
    final FXSpotConvention underlyingConvention = _conventionSource.getSingle(underlyingConventionId, FXSpotConvention.class);
    final FXSpotConvention spotConvention = underlyingConvention;
    final Currency payCurrency = fxForward.getPayCurrency();
    final Currency receiveCurrency = fxForward.getReceiveCurrency();
    final Tenor forwardTenor = fxForward.getMaturityTenor();
    final double payAmount = 1;
    final double receiveAmount = forward;
    final int settlementDays = spotConvention.getSettlementDays();
    final ExternalId settlementRegion = forwardConvention.getSettlementRegion();
    final Calendar settlementCalendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, settlementRegion);
    final ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(_valuationTime, settlementDays, settlementCalendar);
    final ZonedDateTime exchangeDate = ScheduleCalculator.getAdjustedDate(spotDate, forwardTenor.getPeriod(), forwardConvention.getBusinessDayConvention(), settlementCalendar,
        forwardConvention.isIsEOM());
    return ForexDefinition.fromAmounts(payCurrency, receiveCurrency, exchangeDate, payAmount, -receiveAmount);
  }

}
