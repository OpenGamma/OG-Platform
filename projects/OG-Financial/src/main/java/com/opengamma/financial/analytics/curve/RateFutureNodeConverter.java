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

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.convention.Convention;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.convention.ExchangeTradedInstrumentExpiryCalculator;
import com.opengamma.financial.convention.ExchangeTradedInstrumentExpiryCalculatorFactory;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.InterestRateFutureConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class RateFutureNodeConverter extends CurveNodeVisitorAdapter<InstrumentDefinition<?>> {
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
  public RateFutureNodeConverter(final ConventionSource conventionSource, final HolidaySource holidaySource, final RegionSource regionSource,
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
  public InstrumentDefinition<?> visitRateFutureNode(final RateFutureNode rateFuture) {
    final Convention futureConvention = _conventionSource.getConvention(rateFuture.getFutureConvention());
    final String expiryCalculatorName;
    if (futureConvention instanceof InterestRateFutureConvention) {
      expiryCalculatorName = ((InterestRateFutureConvention) futureConvention).getExpiryConvention().getValue();
    } else {
      if (futureConvention == null) {
        throw new OpenGammaRuntimeException("Future convention was null");
      }
      throw new OpenGammaRuntimeException("Could not handle future convention of type " + futureConvention.getClass());
    }
    final Convention underlyingConvention = _conventionSource.getConvention(rateFuture.getUnderlyingConvention());
    final IborIndexConvention indexConvention;
    final Period indexTenor = rateFuture.getUnderlyingTenor().getPeriod();
    if (underlyingConvention instanceof IborIndexConvention) {
      indexConvention = (IborIndexConvention) underlyingConvention;
    } else {
      if (underlyingConvention == null) {
        throw new OpenGammaRuntimeException("Underlying convention was null");
      }
      throw new OpenGammaRuntimeException("Could not handle underlying convention of type " + underlyingConvention.getClass());
    }
    final double paymentAccrualFactor = indexTenor.toTotalMonths() / 12.; //TODO don't use this method
    final Currency currency = indexConvention.getCurrency();
    final Calendar fixingCalendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getFixingCalendar());
    final Calendar regionCalendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getRegionCalendar());
    final BusinessDayConvention businessDayConvention = indexConvention.getBusinessDayConvention();
    final DayCount dayCount = indexConvention.getDayCount();
    final boolean eom = indexConvention.isIsEOM();
    final IborIndex iborIndex = new IborIndex(currency, indexTenor, 0, dayCount, businessDayConvention, eom, indexConvention.getName());
    final ExchangeTradedInstrumentExpiryCalculator expiryCalculator = ExchangeTradedInstrumentExpiryCalculatorFactory.getCalculator(expiryCalculatorName);
    final ZonedDateTime startDate = _now.plus(rateFuture.getStartTenor().getPeriod());
    final LocalTime time = startDate.toLocalTime();
    final ZoneId timeZone = startDate.getZone();
    final ZonedDateTime lastTradeDate = ZonedDateTime.of(expiryCalculator.getExpiryDate(rateFuture.getFutureNumber(), startDate.toLocalDate(), regionCalendar), time, timeZone);
    final InterestRateFutureSecurityDefinition securityDefinition = new InterestRateFutureSecurityDefinition(lastTradeDate, iborIndex, 1, paymentAccrualFactor, "", fixingCalendar);
    final InterestRateFutureTransactionDefinition transactionDefinition = new InterestRateFutureTransactionDefinition(securityDefinition, _now, _marketData, 1);
    return transactionDefinition.withNewNotionalAndTransactionPrice(1, _marketData);
  }
}
