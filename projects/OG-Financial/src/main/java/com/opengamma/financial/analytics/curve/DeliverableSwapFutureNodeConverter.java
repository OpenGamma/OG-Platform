/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.future.SwapFuturesPriceDeliverableSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.SwapFuturesPriceDeliverableTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.analytics.ircurve.strips.DeliverableSwapFutureNode;
import com.opengamma.financial.convention.DeliverablePriceQuotedSwapFutureConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.SwapConvention;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.expirycalc.ExchangeTradedInstrumentExpiryCalculator;
import com.opengamma.financial.convention.expirycalc.ExchangeTradedInstrumentExpiryCalculatorFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Convert a swap futures node into an Instrument definition.
 * The dates of the futures are computed in the following way:
 * - The start date is the valuation date plus the "StartTenor" without convention.
 * - The last trade date is computed from the expiry calculator from the start date, plus the number of futures.
 * - The delivery date is computed from the last trade date adding the "Settlement Days" (i.e. the number of business days) of the swap convention.
 * The futures notional is 1. The futures PVBP is 1. The PBVP is not used in the par spread on which the curve calibration is based.
 */
public class DeliverableSwapFutureNodeConverter extends CurveNodeVisitorAdapter<InstrumentDefinition<?>> {
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
  public DeliverableSwapFutureNodeConverter(final ConventionSource conventionSource, final HolidaySource holidaySource, final RegionSource regionSource,
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
  public InstrumentDefinition<?> visitDeliverableSwapFutureNode(final DeliverableSwapFutureNode swapFuture) {
    Double price = _marketData.getDataPoint(_dataId);
    if (price == null) {
      price = 0.99;
//      throw new OpenGammaRuntimeException("Could not get market data for " + _dataId);
    }
    final DeliverablePriceQuotedSwapFutureConvention futureConvention =
        _conventionSource.getSingle(swapFuture.getFutureConvention(), DeliverablePriceQuotedSwapFutureConvention.class);
    final SwapConvention underlyingSwapConvention = _conventionSource.getSingle(swapFuture.getSwapConvention(), SwapConvention.class);
    final Tenor maturityTenor = swapFuture.getUnderlyingTenor();
    final SwapFixedLegConvention fixedLegConvention = _conventionSource.getSingle(underlyingSwapConvention.getPayLegConvention(), SwapFixedLegConvention.class);
    final VanillaIborLegConvention iborLegConvention = _conventionSource.getSingle(underlyingSwapConvention.getReceiveLegConvention(), VanillaIborLegConvention.class);
    final String expiryCalculatorName = futureConvention.getExpiryConvention().getValue();
    final ZonedDateTime startDate = _valuationTime.plus(swapFuture.getStartTenor().getPeriod());
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, futureConvention.getExchangeCalendar());
    final ExchangeTradedInstrumentExpiryCalculator expiryCalculator = ExchangeTradedInstrumentExpiryCalculatorFactory.getCalculator(expiryCalculatorName);
    final LocalTime time = startDate.toLocalTime();
    final ZoneId timeZone = startDate.getZone();
    final double notional = 1.0;
    final int spotLagSwap = fixedLegConvention.getSettlementDays();
    final ZonedDateTime lastTradeDate = ZonedDateTime.of(expiryCalculator.getExpiryDate(swapFuture.getFutureNumber(), startDate.toLocalDate(), calendar), time, timeZone);
    final ZonedDateTime deliveryDate = ScheduleCalculator.getAdjustedDate(lastTradeDate, spotLagSwap, calendar);
    final IborIndexConvention indexConvention = _conventionSource.getSingle(iborLegConvention.getIborIndexConvention(), IborIndexConvention.class);
    final Currency currency = indexConvention.getCurrency();
    final DayCount dayCount = indexConvention.getDayCount();
    final BusinessDayConvention businessDayConvention = indexConvention.getBusinessDayConvention();
    final boolean eom = indexConvention.isIsEOM();
    final Period indexTenor = iborLegConvention.getResetTenor().getPeriod();
    final int spotLagIndex = indexConvention.getSettlementDays();
    final IborIndex iborIndex = new IborIndex(currency, indexTenor, spotLagIndex, dayCount, businessDayConvention, eom, indexConvention.getName());
    final GeneratorSwapFixedIbor generator = new GeneratorSwapFixedIbor("", fixedLegConvention.getPaymentTenor().getPeriod(), fixedLegConvention.getDayCount(), iborIndex, calendar);
    final SwapFixedIborDefinition underlying = SwapFixedIborDefinition.from(deliveryDate, maturityTenor.getPeriod(), generator, notional, 0.0, false); //FIXME: rate of underlying?
    final SwapFuturesPriceDeliverableSecurityDefinition securityDefinition = new SwapFuturesPriceDeliverableSecurityDefinition(lastTradeDate, underlying, notional);
    return new SwapFuturesPriceDeliverableTransactionDefinition(securityDefinition, 1, _valuationTime, price);
  }

}
