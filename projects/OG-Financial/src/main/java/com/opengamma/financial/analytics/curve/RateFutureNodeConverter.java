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
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.convention.Convention;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.convention.ExchangeTradedInstrumentExpiryCalculator;
import com.opengamma.financial.convention.ExchangeTradedInstrumentExpiryCalculatorFactory;
import com.opengamma.financial.convention.FederalFundsFutureConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.InterestRateFutureConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Convert rate futures nodes into an {@link InstrumentDefinition}.
 * The dates of the futures are computed in the following way:
 * - The start date is the valuation date plus the "startTenor" without convention.
 * - The last trade date is computed from the expiry calculator from the start date, plus the number of futures.
 * - The delivery date is computed from the last trade date adding the "Settlement Days" (i.e. the number of business days) of the swap convention.
 * The futures notional is 1.
 */
public class RateFutureNodeConverter extends CurveNodeVisitorAdapter<InstrumentDefinition<?>> {
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
  public RateFutureNodeConverter(final ConventionSource conventionSource, final HolidaySource holidaySource, final RegionSource regionSource,
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
  public InstrumentDefinition<?> visitRateFutureNode(final RateFutureNode rateFuture) {
    final Convention futureConvention = _conventionSource.getConvention(rateFuture.getFutureConvention());
    final Double price = _marketData.getDataPoint(_dataId);
    if (price == null) {
      throw new OpenGammaRuntimeException("Could not get market data for " + _dataId);
    }
    if (futureConvention == null) {
      throw new OpenGammaRuntimeException("Future convention was null");
    }
    if (futureConvention instanceof InterestRateFutureConvention) {
      return getInterestRateFuture(rateFuture, (InterestRateFutureConvention) futureConvention, price);
    } else if (futureConvention instanceof FederalFundsFutureConvention) {
      return getFederalFundsFuture(rateFuture, (FederalFundsFutureConvention) futureConvention, price);
    }
    throw new OpenGammaRuntimeException("Could not handle future convention of type " + futureConvention.getClass());
  }

  /**
   * Creates an interest rate future from a rate future node.
   * @param rateFuture The rate future node
   * @param futureConvention The future convention
   * @param price The price
   * @return The interest rate future
   */
  private InstrumentDefinition<?> getInterestRateFuture(final RateFutureNode rateFuture, final InterestRateFutureConvention futureConvention,
      final Double price) {
    final String expiryCalculatorName = futureConvention.getExpiryConvention().getValue();
    final IborIndexConvention indexConvention = _conventionSource.getConvention(IborIndexConvention.class, rateFuture.getUnderlyingConvention());
    if (indexConvention == null) {
      throw new OpenGammaRuntimeException("Underlying convention was null");
    }
    final Period indexTenor = rateFuture.getUnderlyingTenor().getPeriod();
    final double paymentAccrualFactor = indexTenor.toTotalMonths() / 12.; //TODO don't use this method
    final Currency currency = indexConvention.getCurrency();
    final Calendar fixingCalendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getFixingCalendar());
    final Calendar regionCalendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getRegionCalendar());
    final BusinessDayConvention businessDayConvention = indexConvention.getBusinessDayConvention();
    final DayCount dayCount = indexConvention.getDayCount();
    final boolean eom = indexConvention.isIsEOM();
    final int spotLag = indexConvention.getSettlementDays();
    final IborIndex iborIndex = new IborIndex(currency, indexTenor, spotLag, dayCount, businessDayConvention, eom, indexConvention.getName());
    final ExchangeTradedInstrumentExpiryCalculator expiryCalculator = ExchangeTradedInstrumentExpiryCalculatorFactory.getCalculator(expiryCalculatorName);
    final ZonedDateTime startDate = _valuationTime.plus(rateFuture.getStartTenor().getPeriod());
    final LocalTime time = startDate.toLocalTime();
    final ZoneId timeZone = startDate.getZone();
    final ZonedDateTime expiryDate = ZonedDateTime.of(expiryCalculator.getExpiryDate(rateFuture.getFutureNumber(), startDate.toLocalDate(), regionCalendar), time, timeZone);
    final InterestRateFutureSecurityDefinition securityDefinition = new InterestRateFutureSecurityDefinition(expiryDate, iborIndex, 1, paymentAccrualFactor, "", fixingCalendar);
    final InterestRateFutureTransactionDefinition transactionDefinition = new InterestRateFutureTransactionDefinition(securityDefinition, _valuationTime, price, 1);
    return transactionDefinition;
  }

  /**
   * Creates a Federal fund future from a rate future node.
   * @param rateFuture The rate future node
   * @param futureConvention The future convention
   * @param price The price
   * @return The Fed fund future
   */
  private InstrumentDefinition<?> getFederalFundsFuture(final RateFutureNode rateFuture, final FederalFundsFutureConvention futureConvention,
      final Double price) {
    final String expiryCalculatorName = futureConvention.getExpiryConvention().getValue();
    final OvernightIndexConvention indexConvention = _conventionSource.getConvention(OvernightIndexConvention.class, rateFuture.getUnderlyingConvention());
    if (indexConvention == null) {
      throw new OpenGammaRuntimeException("Underlying convention was null");
    }
    final Currency currency = indexConvention.getCurrency();
    final DayCount dayCount = indexConvention.getDayCount();
    final int publicationLag = indexConvention.getPublicationLag();
    final IndexON index = new IndexON(indexConvention.getName(), currency, dayCount, publicationLag);
    final double paymentAccrualFactor = 1 / 12.;
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getRegionCalendar());
    final ExchangeTradedInstrumentExpiryCalculator expiryCalculator = ExchangeTradedInstrumentExpiryCalculatorFactory.getCalculator(expiryCalculatorName);
    final ZonedDateTime startDate = _valuationTime.plus(rateFuture.getStartTenor().getPeriod());
    final LocalTime time = startDate.toLocalTime();
    final ZoneId timeZone = startDate.getZone();
    final ZonedDateTime expiryDate = ZonedDateTime.of(expiryCalculator.getExpiryDate(rateFuture.getFutureNumber(), startDate.toLocalDate(), calendar), time, timeZone);
    final FederalFundsFutureSecurityDefinition securityDefinition = FederalFundsFutureSecurityDefinition.from(expiryDate,
        index, 1, paymentAccrualFactor, "", calendar);
    final FederalFundsFutureTransactionDefinition transactionDefinition = new FederalFundsFutureTransactionDefinition(securityDefinition, 1, _valuationTime, price);
    return transactionDefinition;
  }
}
