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
import com.opengamma.core.link.ConventionLink;
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
 * - The last trade date is computed from the expiry calculator from the start date,
 *   plus the number of futures.
 * - The delivery date is computed from the last trade date adding the "Settlement Days"
 *   (i.e. the number of business days) of the swap convention.
 * The futures notional is 1. The futures PVBP is 1. The PBVP is not used in the par
 * spread on which the curve calibration is based.
 */
public class DeliverableSwapFutureNodeConverter extends CurveNodeVisitorAdapter<InstrumentDefinition<?>> {
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
   * @param holidaySource The holiday source, not null
   * @param regionSource The region source, not null
   * @param marketData The market data, not null
   * @param dataId The id of the market data, not null
   * @param valuationTime The valuation time, not null
   */
  public DeliverableSwapFutureNodeConverter(HolidaySource holidaySource,
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

  /**
   * @param conventionSource The convention source, not required
   * @param holidaySource The holiday source, not null
   * @param regionSource The region source, not null
   * @param marketData The market data, not null
   * @param dataId The id of the market data, not null
   * @param valuationTime The valuation time, not null
   * @deprecated use constructor without conventionSource
   */
  @Deprecated
  public DeliverableSwapFutureNodeConverter(ConventionSource conventionSource, HolidaySource holidaySource,
                                            RegionSource regionSource, SnapshotDataBundle marketData,
                                            ExternalId dataId, ZonedDateTime valuationTime) {
    this(holidaySource, regionSource, marketData, dataId, valuationTime);
  }

  @Override
  public InstrumentDefinition<?> visitDeliverableSwapFutureNode(DeliverableSwapFutureNode swapFuture) {
    Double price = _marketData.getDataPoint(_dataId);
    if (price == null) {
      price = 0.99;
//      throw new OpenGammaRuntimeException("Could not get market data for " + _dataId);
    }
    DeliverablePriceQuotedSwapFutureConvention futureConvention =
        ConventionLink.resolvable(swapFuture.getFutureConvention(), DeliverablePriceQuotedSwapFutureConvention.class)
            .resolve();
    SwapConvention underlyingSwapConvention =
        ConventionLink.resolvable(swapFuture.getSwapConvention(), SwapConvention.class).resolve();
    Tenor maturityTenor = swapFuture.getUnderlyingTenor();
    SwapFixedLegConvention fixedLegConvention =
        ConventionLink.resolvable(underlyingSwapConvention.getPayLegConvention(), SwapFixedLegConvention.class)
            .resolve();
    VanillaIborLegConvention iborLegConvention =
        ConventionLink.resolvable(underlyingSwapConvention.getReceiveLegConvention(), VanillaIborLegConvention.class)
            .resolve();

    String expiryCalculatorName = futureConvention.getExpiryConvention().getValue();
    ZonedDateTime startDate = _valuationTime.plus(swapFuture.getStartTenor().getPeriod());
    Calendar calendar =
        CalendarUtils.getCalendar(_regionSource, _holidaySource, futureConvention.getExchangeCalendar());
    ExchangeTradedInstrumentExpiryCalculator expiryCalculator =
        ExchangeTradedInstrumentExpiryCalculatorFactory.getCalculator(expiryCalculatorName);
    LocalTime time = startDate.toLocalTime();
    ZoneId timeZone = startDate.getZone();
    double notional = 1.0;
    int spotLagSwap = fixedLegConvention.getSettlementDays();
    ZonedDateTime lastTradeDate = ZonedDateTime.of(expiryCalculator.getExpiryDate(
        swapFuture.getFutureNumber(), startDate.toLocalDate(), calendar), time, timeZone);
    ZonedDateTime deliveryDate = ScheduleCalculator.getAdjustedDate(lastTradeDate, spotLagSwap, calendar);
    IborIndexConvention indexConvention =
        ConventionLink.resolvable(iborLegConvention.getIborIndexConvention(), IborIndexConvention.class).resolve();
    Currency currency = indexConvention.getCurrency();
    DayCount dayCount = indexConvention.getDayCount();
    BusinessDayConvention businessDayConvention = indexConvention.getBusinessDayConvention();
    boolean eom = indexConvention.isIsEOM();
    Period indexTenor = iborLegConvention.getResetTenor().getPeriod();
    int spotLagIndex = indexConvention.getSettlementDays();
    IborIndex iborIndex =
        new IborIndex(currency, indexTenor, spotLagIndex, dayCount,
                      businessDayConvention, eom, indexConvention.getName());
    GeneratorSwapFixedIbor generator = new GeneratorSwapFixedIbor(
        "", fixedLegConvention.getPaymentTenor().getPeriod(), fixedLegConvention.getDayCount(), iborIndex, calendar);
    SwapFixedIborDefinition underlying = SwapFixedIborDefinition.from(
        deliveryDate, maturityTenor.getPeriod(), generator, notional, 0.0, false); //FIXME: rate of underlying?
    SwapFuturesPriceDeliverableSecurityDefinition securityDefinition =
        new SwapFuturesPriceDeliverableSecurityDefinition(lastTradeDate, underlying, notional);
    return new SwapFuturesPriceDeliverableTransactionDefinition(securityDefinition, 1, _valuationTime, price);
  }

}
