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
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.analytics.ircurve.strips.FXForwardNode;
import com.opengamma.financial.convention.Convention;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.convention.FXForwardAndSwapConvention;
import com.opengamma.financial.convention.FXSpotConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public class FXForwardNodeConverter extends CurveNodeVisitorAdapter<InstrumentDefinition<?>> {
  /** The convention source */
  private final ConventionSource _conventionSource;
  /** The holiday source */
  private final HolidaySource _holidaySource;
  /** The region source */
  private final RegionSource _regionSource;
  /** The market data */
  private final Double _marketData;
  /** The valuation time */
  private final ZonedDateTime _now;

  /**
   * @param conventionSource The convention source, not null
   * @param holidaySource The holiday source, not null
   * @param regionSource The region source, not null
   * @param marketData The market data, may be null
   * @param now The valuation time, not null
   */
  public FXForwardNodeConverter(final ConventionSource conventionSource, final HolidaySource holidaySource, final RegionSource regionSource,
      final Double marketData, final ZonedDateTime now) {
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(regionSource, "region source");
    _conventionSource = conventionSource;
    _holidaySource = holidaySource;
    _regionSource = regionSource;
    _marketData = marketData;
    _now = now;
  }

  @SuppressWarnings("synthetic-access")
  @Override
  public InstrumentDefinition<?> visitFXForwardNode(final FXForwardNode fxForward) {
    final ExternalId conventionId = fxForward.getFxForwardConvention();
    final Convention convention = _conventionSource.getConvention(conventionId);
    if (convention == null) {
      throw new OpenGammaRuntimeException("Could not get convention with id " + conventionId);
    }
    if (!(convention instanceof FXForwardAndSwapConvention)) {
      throw new OpenGammaRuntimeException("Need a convention of type " + FXForwardAndSwapConvention.class + ", have " + convention.getClass());
    }
    final FXForwardAndSwapConvention forwardConvention = (FXForwardAndSwapConvention) convention;
    final ExternalId underlyingConventionId = forwardConvention.getSpotConvention();
    final Convention underlyingConvention = _conventionSource.getConvention(underlyingConventionId);
    if (underlyingConvention == null) {
      throw new OpenGammaRuntimeException("Could not get convention with id " + underlyingConventionId);
    }
    if (!(underlyingConvention instanceof FXSpotConvention)) {
      throw new OpenGammaRuntimeException("Need a convention of type " + FXSpotConvention.class + ", have " + convention.getClass());
    }
    final FXSpotConvention spotConvention = (FXSpotConvention) underlyingConvention;
    final Currency payCurrency = fxForward.getPayCurrency();
    final Currency receiveCurrency = fxForward.getReceiveCurrency();
    final Tenor forwardTenor = fxForward.getMaturityTenor();
    final double payAmount = 1;
    final double receiveAmount = _marketData;
    final int settlementDays = spotConvention.getSettlementDays();
    final ExternalId settlementRegion = forwardConvention.getSettlementRegion();
    final Calendar settlementCalendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, settlementRegion);
    final ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(_now, settlementDays, settlementCalendar);
    final ZonedDateTime exchangeDate = ScheduleCalculator.getAdjustedDate(spotDate, forwardTenor.getPeriod(), forwardConvention.getBusinessDayConvention(), settlementCalendar,
        forwardConvention.isIsEOM());
    return ForexDefinition.fromAmounts(payCurrency, receiveCurrency, exchangeDate, payAmount, -receiveAmount);
  }

}
