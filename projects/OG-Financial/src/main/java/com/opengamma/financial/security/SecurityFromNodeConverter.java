/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.analytics.curve.CurveNodeVisitorAdapter;
import com.opengamma.financial.analytics.curve.NodeConverterUtils;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.FRANode;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.FederalFundsFutureConvention;
import com.opengamma.financial.convention.FinancialConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.InterestRateFutureConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.expirycalc.ExchangeTradedInstrumentExpiryCalculator;
import com.opengamma.financial.convention.expirycalc.ExchangeTradedInstrumentExpiryCalculatorFactory;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.FederalFundsFutureSecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.tuple.Triple;

/**
 * Convert a FRA node into an Instrument definition.
 * The dates of the FRA are computed in the following way:
 * - The spot date is computed from the trade date time adding the "Settlement Days" (i.e. the number of business days)
 * of the convention.
 * - The accrual start date is computed from the spot date adding the "FixingStart" of the node and using the
 * business-day-convention, calendar and EOM of the convention.
 * - The accrual end date is computed from the spot date adding the "FixingEnd" of the node and using the
 * business-day-convention, calendar and EOM of the convention.
 * The FRA notional is 1.
 */
public class SecurityFromNodeConverter extends CurveNodeVisitorAdapter<FinancialSecurity> {

  /** The convention source */
  private final ConventionSource _conventionSource;
  /** The holiday source */
  private final HolidaySource _holidaySource;
  /** The region source */
  private final RegionSource _regionSource;
  /** The trade date time */
  private final ZonedDateTime _tradeDateTime;

  /** the rate to fill in */
  private final Double _rate;
  /** the amount to fill in */
  private final Double _amount;
  /** the external identifier */
  private final ExternalId _identifier;

  /**
   * @param conventionSource The convention source, not null
   * @param holidaySource The holiday source, not null
   * @param regionSource The region source, not null
   * @param rate The fixed rate
   * @param amount The notional amounts
   * @param identifier The floating rate identifier
   * @param tradeDateTime The trade date time, not null
   */
  public SecurityFromNodeConverter(final ConventionSource conventionSource,
                                   final HolidaySource holidaySource,
                                   final RegionSource regionSource,
                                   final ZonedDateTime tradeDateTime,
                                   final Double rate,
                                   final Double amount,
                                   final ExternalId identifier) {
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(regionSource, "region source");
    ArgumentChecker.notNull(tradeDateTime, "trade date time");
    _conventionSource = conventionSource;
    _holidaySource = holidaySource;
    _regionSource = regionSource;
    _tradeDateTime = tradeDateTime;
    _rate = rate;
    _amount = amount;
    _identifier = identifier;
  }


  @Override
  public FRASecurity visitFRANode(final FRANode fraNode) {
    final Convention convention = _conventionSource.getSingle(fraNode.getConvention().toBundle());
    final Period startPeriod = fraNode.getFixingStart().getPeriod();
    final Period endPeriod = fraNode.getFixingEnd().getPeriod();
    //TODO probably need a specific FRA convention to hold the reset tenor
    final long months = endPeriod.toTotalMonths() - startPeriod.toTotalMonths();
    final Period indexTenor = Period.ofMonths((int) months);
    final IborIndexConvention indexConvention;
    if (convention instanceof IborIndexConvention) {
      indexConvention = (IborIndexConvention) convention;
    } else {
      if (convention == null) {
        throw new OpenGammaRuntimeException("Convention with id " + fraNode.getConvention() + " was null");
      }
      throw new OpenGammaRuntimeException("Could not handle underlying convention of type " + convention.getClass());
    }
    final Currency currency = indexConvention.getCurrency();
    final Calendar fixingCalendar = CalendarUtils.getCalendar(_regionSource,
                                                              _holidaySource,
                                                              indexConvention.getFixingCalendar());
    final Calendar regionCalendar = CalendarUtils.getCalendar(_regionSource,
                                                              _holidaySource,
                                                              indexConvention.getRegionCalendar());
    final int spotLag = indexConvention.getSettlementDays();
    final BusinessDayConvention businessDayConvention = indexConvention.getBusinessDayConvention();
    final DayCount dayCount = indexConvention.getDayCount();
    final boolean eom = indexConvention.isIsEOM();
    final IborIndex iborIndex = new IborIndex(currency,
                                              indexTenor,
                                              spotLag,
                                              dayCount,
                                              businessDayConvention,
                                              eom,
                                              convention.getName());
    final ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(_tradeDateTime, spotLag, regionCalendar);
    final ZonedDateTime accrualStartDate = ScheduleCalculator.getAdjustedDate(spotDate,
                                                                              startPeriod,
                                                                              businessDayConvention,
                                                                              regionCalendar,
                                                                              eom);
    final ZonedDateTime accrualEndDate = ScheduleCalculator.getAdjustedDate(spotDate,
                                                                            endPeriod,
                                                                            businessDayConvention,
                                                                            regionCalendar,
                                                                            eom);
    final ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(accrualStartDate,
                                                                        -iborIndex.getSpotLag(),
                                                                        fixingCalendar);
    FRASecurity security = new FRASecurity(currency,
                                           indexConvention.getRegionCalendar(),
                                           accrualStartDate,
                                           accrualEndDate,
                                           _rate,
                                           1,
                                           _identifier,
                                           fixingDate);

    security.setName(fraNode.getName() + ": " + _identifier);
    return security;
  }


  @Override
  public FinancialSecurity visitSwapNode(final SwapNode swapNode) {
    final FinancialConvention payLegConvention = _conventionSource.getSingle(swapNode.getPayLegConvention(), FinancialConvention.class);
    final FinancialConvention receiveLegConvention = _conventionSource.getSingle(swapNode.getReceiveLegConvention(), FinancialConvention.class);

    final boolean isFloatFloat = NodeConverterUtils.isFloatFloat(payLegConvention, receiveLegConvention);
    final SnapshotDataBundle snapshotDataBundle = new SnapshotDataBundle();
    snapshotDataBundle.setDataPoint(_identifier, _rate);
    final Triple<? extends SwapLeg, ZonedDateTime, ZonedDateTime> payLeg = NodeConverterUtils.createSwapLeg(
        payLegConvention,
        swapNode.getStartTenor().getPeriod(),
        swapNode.getMaturityTenor().getPeriod(),
        _regionSource,
        _holidaySource,
        _conventionSource,
        snapshotDataBundle,
        _identifier,
        _tradeDateTime,
        true,
        isFloatFloat);


    final Triple<? extends SwapLeg, ZonedDateTime, ZonedDateTime> receiveLeg = NodeConverterUtils.createSwapLeg(
        receiveLegConvention,
        swapNode.getStartTenor().getPeriod(),
        swapNode.getMaturityTenor().getPeriod(),
        _regionSource,
        _holidaySource,
        _conventionSource,
        snapshotDataBundle,
        _identifier,
        _tradeDateTime,
        false,
        isFloatFloat);


    SwapSecurity security = new SwapSecurity(_tradeDateTime,
                                             payLeg.getSecond(),
                                             payLeg.getThird(),
                                             "counterparty",
                                             payLeg.getFirst(),
                                             receiveLeg.getFirst());

    security.setName(swapNode.getName() + ": " + _identifier);
    return security;
  }


  @Override
  public CashSecurity visitCashNode(final CashNode cashNode) {
    final Convention convention = _conventionSource.getSingle(cashNode.getConvention());
    final Period startPeriod = cashNode.getStartTenor().getPeriod();
    final Period maturityPeriod = cashNode.getMaturityTenor().getPeriod();
    if (convention instanceof DepositConvention) {
      final DepositConvention depositConvention = (DepositConvention) convention;
      final Currency currency = depositConvention.getCurrency();
      final Calendar calendar = CalendarUtils.getCalendar(_regionSource,
                                                          _holidaySource,
                                                          depositConvention.getRegionCalendar());
      final BusinessDayConvention businessDayConvention = depositConvention.getBusinessDayConvention();
      final boolean isEOM = depositConvention.isIsEOM();
      final DayCount dayCount = depositConvention.getDayCount();
      final int settlementDays = depositConvention.getSettlementDays();
      final ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(_tradeDateTime, settlementDays, calendar);
      final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spotDate,
                                                                         startPeriod,
                                                                         businessDayConvention,
                                                                         calendar,
                                                                         isEOM);
      final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate,
                                                                       maturityPeriod,
                                                                       businessDayConvention,
                                                                       calendar,
                                                                       isEOM);
      final double accrualFactor = dayCount.getDayCountFraction(startDate, endDate);
      return new CashSecurity(currency,
                              depositConvention.getRegionCalendar(),
                              startDate,
                              endDate,
                              dayCount,
                              _rate,
                              _amount);
    } else if (convention instanceof IborIndexConvention) {
      final IborIndexConvention iborConvention = (IborIndexConvention) convention;
      final Currency currency = iborConvention.getCurrency();
      final Calendar calendar = CalendarUtils.getCalendar(_regionSource,
                                                          _holidaySource,
                                                          iborConvention.getRegionCalendar());
      final BusinessDayConvention businessDayConvention = iborConvention.getBusinessDayConvention();
      final boolean isEOM = iborConvention.isIsEOM();
      final DayCount dayCount = iborConvention.getDayCount();
      final int settlementDays = iborConvention.getSettlementDays();
      final ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(_tradeDateTime, settlementDays, calendar);
      final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spotDate,
                                                                         startPeriod,
                                                                         businessDayConvention,
                                                                         calendar,
                                                                         isEOM);
      final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate,
                                                                       maturityPeriod,
                                                                       businessDayConvention,
                                                                       calendar,
                                                                       isEOM);
      final double accrualFactor = dayCount.getDayCountFraction(startDate, endDate);
      final int spotLag = iborConvention.getSettlementDays();
      final boolean eom = iborConvention.isIsEOM();
      final long months = maturityPeriod.toTotalMonths() - startPeriod.toTotalMonths();
      final Period indexTenor = Period.ofMonths((int) months);
      final IborIndex iborIndex = new IborIndex(currency,
                                                indexTenor,
                                                spotLag,
                                                dayCount,
                                                businessDayConvention,
                                                eom,
                                                convention.getName());
      CashSecurity security = new CashSecurity(currency,
                                               iborConvention.getRegionCalendar(),
                                               startDate,
                                               endDate,
                                               dayCount,
                                               _rate,
                                               _amount);

      security.setName(cashNode.getName() + ": " + _identifier);
      return security;
    } else {
      throw new OpenGammaRuntimeException("Could not handle convention of type " + convention.getClass());
    }
  }

  @Override
  public FutureSecurity visitRateFutureNode(final RateFutureNode rateFuture) {
    final FutureSecurity security;
    final Convention futureConvention = _conventionSource.getSingle(rateFuture.getFutureConvention());
    if (futureConvention instanceof InterestRateFutureConvention) {
      security = getInterestRateFuture(rateFuture, (InterestRateFutureConvention) futureConvention, _rate);
    } else if (futureConvention instanceof FederalFundsFutureConvention) {
      security = getFederalFundsFuture(rateFuture, (FederalFundsFutureConvention) futureConvention, _rate);
    } else {
      throw new OpenGammaRuntimeException("Could not handle future convention of type " + futureConvention.getClass());
    }
    security.setName(rateFuture.getName() + ": " + _identifier);
    return security;
  }


  /**
   * Creates an interest rate future security from a rate future node.
   *
   * @param rateFuture The rate future node
   * @param futureConvention The future convention
   * @param price The price
   * @return The interest rate future
   */
  private InterestRateFutureSecurity getInterestRateFuture(final RateFutureNode rateFuture,
                                                           final InterestRateFutureConvention futureConvention,
                                                           final Double price) {
    final String expiryCalculatorName = futureConvention.getExpiryConvention().getValue();
    final IborIndexConvention indexConvention = _conventionSource.getSingle(futureConvention.getIndexConvention(),
                                                                                IborIndexConvention.class);
    final Period indexTenor = rateFuture.getUnderlyingTenor().getPeriod();
    final double paymentAccrualFactor = indexTenor.toTotalMonths() / 12.; //TODO don't use this method
    final Currency currency = indexConvention.getCurrency();
    final Calendar fixingCalendar = CalendarUtils.getCalendar(_regionSource,
                                                              _holidaySource,
                                                              indexConvention.getFixingCalendar());
    final Calendar regionCalendar = CalendarUtils.getCalendar(_regionSource,
                                                              _holidaySource,
                                                              indexConvention.getRegionCalendar());
    final BusinessDayConvention businessDayConvention = indexConvention.getBusinessDayConvention();
    final DayCount dayCount = indexConvention.getDayCount();
    final boolean eom = indexConvention.isIsEOM();
    final int spotLag = indexConvention.getSettlementDays();
    final IborIndex iborIndex = new IborIndex(currency,
                                              indexTenor,
                                              spotLag,
                                              dayCount,
                                              businessDayConvention,
                                              eom,
                                              indexConvention.getName());
    final ExchangeTradedInstrumentExpiryCalculator expiryCalculator = ExchangeTradedInstrumentExpiryCalculatorFactory.getCalculator(
        expiryCalculatorName);
    final ZonedDateTime startDate = _tradeDateTime.plus(rateFuture.getStartTenor().getPeriod());
    final LocalTime time = startDate.toLocalTime();
    final ZoneId timeZone = startDate.getZone();
    final ZonedDateTime expiryDate = ZonedDateTime.of(expiryCalculator.getExpiryDate(rateFuture.getFutureNumber(),
                                                                                     startDate.toLocalDate(),
                                                                                     regionCalendar), time, timeZone);
    final InterestRateFutureSecurityDefinition securityDefinition = new InterestRateFutureSecurityDefinition(expiryDate,
                                                                                                             iborIndex,
                                                                                                             1,
                                                                                                             paymentAccrualFactor,
                                                                                                             "",
                                                                                                             fixingCalendar);
    final InterestRateFutureTransactionDefinition transactionDefinition = new InterestRateFutureTransactionDefinition(
        securityDefinition,
        1,
        _tradeDateTime,
        price);
    //return transactionDefinition;

    final Expiry expiry = new Expiry(expiryDate);
    return new InterestRateFutureSecurity(expiry,
                                          "TRADING_EXCHANGE",
                                          "SETTLEMENT_EXCHANGE",
                                          currency,
                                          _amount,
                                          _identifier,
                                          "CATEGORY");

  }

  /**
   * Creates a Federal fund future security from a rate future node.
   *
   * @param rateFuture The rate future node
   * @param futureConvention The future convention
   * @param price The price
   * @return The Fed fund future
   */
  private FederalFundsFutureSecurity getFederalFundsFuture(final RateFutureNode rateFuture,
                                                           final FederalFundsFutureConvention futureConvention,
                                                           final Double price) {
    final String expiryCalculatorName = futureConvention.getExpiryConvention().getValue();
    final OvernightIndexConvention indexConvention = _conventionSource.getSingle(futureConvention.getIndexConvention(),
                                                                                     OvernightIndexConvention.class);
    final Currency currency = indexConvention.getCurrency();
    final DayCount dayCount = indexConvention.getDayCount();
    final int publicationLag = indexConvention.getPublicationLag();
    final IndexON index = new IndexON(indexConvention.getName(), currency, dayCount, publicationLag);
    final double paymentAccrualFactor = 1 / 12.;
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource,
                                                        _holidaySource,
                                                        indexConvention.getRegionCalendar());
    final ExchangeTradedInstrumentExpiryCalculator expiryCalculator = ExchangeTradedInstrumentExpiryCalculatorFactory.getCalculator(
        expiryCalculatorName);
    final ZonedDateTime startDate = _tradeDateTime.plus(rateFuture.getStartTenor().getPeriod());
    final LocalTime time = startDate.toLocalTime();
    final ZoneId timeZone = startDate.getZone();
    final ZonedDateTime expiryDate = ZonedDateTime.of(expiryCalculator.getExpiryDate(rateFuture.getFutureNumber(),
                                                                                     startDate.toLocalDate(),
                                                                                     calendar), time, timeZone);
    final FederalFundsFutureSecurityDefinition securityDefinition = FederalFundsFutureSecurityDefinition.from(expiryDate,
                                                                                                              index,
                                                                                                              1,
                                                                                                              paymentAccrualFactor,
                                                                                                              "",
                                                                                                              calendar);
    final FederalFundsFutureTransactionDefinition transactionDefinition = new FederalFundsFutureTransactionDefinition(
        securityDefinition,
        1,
        _tradeDateTime,
        price);
    //return transactionDefinition;

    final Expiry expiry = new Expiry(expiryDate);
    return new FederalFundsFutureSecurity(expiry,
                                          "TRADING_EXCHANGE",
                                          "SETTLEMENT_EXCHANGE",
                                          currency,
                                          _amount,
                                          _identifier,
                                          "CATEGORY");
  }
}
