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
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.analytics.curve.CurveNodeVisitorAdapter;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.FRANode;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.financial.convention.Convention;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.ExchangeTradedInstrumentExpiryCalculator;
import com.opengamma.financial.convention.ExchangeTradedInstrumentExpiryCalculatorFactory;
import com.opengamma.financial.convention.FederalFundsFutureConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.InterestRateFutureConvention;
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.FederalFundsFutureSecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.InterestRateLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/**
 * Convert a FRA node into an Instrument definition.
 * The dates of the FRA are computed in the following way:
 * - The spot date is computed from the valuation date adding the "Settlement Days" (i.e. the number of business days)
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
  /** The valuation time */
  private final ZonedDateTime _valuationTime;

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
   * @param valuationTime The valuation time, not null
   */
  public SecurityFromNodeConverter(final ConventionSource conventionSource,
                                   final HolidaySource holidaySource,
                                   final RegionSource regionSource,
                                   final ZonedDateTime valuationTime,
                                   final Double rate,
                                   final Double amount,
                                   final ExternalId identifier) {
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(regionSource, "region source");
    ArgumentChecker.notNull(valuationTime, "valuation time");
    _conventionSource = conventionSource;
    _holidaySource = holidaySource;
    _regionSource = regionSource;
    _valuationTime = valuationTime;
    _rate = rate;
    _amount = amount;
    _identifier = identifier;
  }


  @Override
  public FRASecurity visitFRANode(final FRANode fraNode) {
    final Convention convention = _conventionSource.getConvention(fraNode.getConvention());
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
    final ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(_valuationTime, spotLag, regionCalendar);
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
    return new FRASecurity(currency,
                           indexConvention.getRegionCalendar(),
                           accrualStartDate,
                           accrualEndDate,
                           _rate,
                           1,
                           _identifier,
                           fixingDate);
  }


  @Override
  public FinancialSecurity visitSwapNode(final SwapNode swapNode) {
    final Convention payLegConvention = _conventionSource.getConvention(swapNode.getPayLegConvention());
    if (payLegConvention == null) {
      throw new OpenGammaRuntimeException("Convention with id " + swapNode.getPayLegConvention() + " was null");
    }
    final Convention receiveLegConvention = _conventionSource.getConvention(swapNode.getReceiveLegConvention());
    if (receiveLegConvention == null) {
      throw new OpenGammaRuntimeException("Convention with id " + swapNode.getPayLegConvention() + " was null");
    }
    final Pair<? extends InterestRateLeg, Triple<ZonedDateTime, ZonedDateTime, ZonedDateTime>> payLeg;
    final Pair<? extends InterestRateLeg, Triple<ZonedDateTime, ZonedDateTime, ZonedDateTime>> receiveLeg;
    final boolean isFloatFloat = ((payLegConvention instanceof VanillaIborLegConvention) || (payLegConvention instanceof OISLegConvention))
        && ((receiveLegConvention instanceof VanillaIborLegConvention) || (receiveLegConvention instanceof OISLegConvention));
    if (payLegConvention instanceof SwapFixedLegConvention) {
      payLeg = getFixedLeg((SwapFixedLegConvention) payLegConvention, swapNode, true);
    } else if (payLegConvention instanceof VanillaIborLegConvention) {
      payLeg = getIborLeg(_identifier, (VanillaIborLegConvention) payLegConvention, swapNode, true, false);
    } else if (payLegConvention instanceof OISLegConvention) {
      payLeg = getOISLeg(_identifier, (OISLegConvention) payLegConvention, swapNode, true, false);
    } else {
      throw new OpenGammaRuntimeException("Cannot handle convention type " + payLegConvention.getClass());
    }
    if (receiveLegConvention instanceof SwapFixedLegConvention) {
      receiveLeg = getFixedLeg((SwapFixedLegConvention) receiveLegConvention, swapNode, false);
    } else if (receiveLegConvention instanceof VanillaIborLegConvention) {
      receiveLeg = getIborLeg(_identifier,
                              (VanillaIborLegConvention) receiveLegConvention,
                              swapNode,
                              false,
                              isFloatFloat);
    } else if (receiveLegConvention instanceof OISLegConvention) {
      receiveLeg = getOISLeg(_identifier, (OISLegConvention) receiveLegConvention, swapNode, false, isFloatFloat);
    } else {
      throw new OpenGammaRuntimeException("Cannot handle convention type " + receiveLegConvention.getClass());
    }

    if (!payLeg.getSecond().getFirst().equals(receiveLeg.getSecond().getFirst())) {
      throw new OpenGammaRuntimeException(
          "Both, pay and receive legs should resolve equal start dates, but instead there were: payleg(" + payLeg.getSecond().getFirst() + "), receiveLeg(" + receiveLeg.getSecond().getFirst() + ")");
    }
    if (!payLeg.getSecond().getSecond().equals(receiveLeg.getSecond().getSecond())) {
      throw new OpenGammaRuntimeException(
          "Both, pay and receive legs should resolve equal effective dates, but instead there were: payleg(" + payLeg.getSecond().getSecond() + "), " +
              "receiveLeg(" + receiveLeg.getSecond().getSecond() + ")");
    }
    if (!payLeg.getSecond().getThird().equals(receiveLeg.getSecond().getThird())) {
      throw new OpenGammaRuntimeException(
          "Both, pay and receive legs should resolve equal maturity dates, but instead there were: payleg(" + payLeg.getSecond().getThird() + "), " +
              "receiveLeg(" + receiveLeg.getSecond().getThird() + ")");
    }
    return new SwapSecurity(payLeg.getSecond().getFirst(),
                            payLeg.getSecond().getSecond(),
                            payLeg.getSecond().getThird(),
                            "counterparty",
                            payLeg.getFirst(),
                            receiveLeg.getFirst());
  }


  private Pair<FixedInterestRateLeg, Triple<ZonedDateTime, ZonedDateTime, ZonedDateTime>> getFixedLeg(final SwapFixedLegConvention convention,
                                                                                                      final SwapNode swapNode,
                                                                                                      final boolean isPayer) {
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource,
                                                        _holidaySource,
                                                        convention.getRegionCalendar());

    final Currency currency = convention.getCurrency();
    final DayCount dayCount = convention.getDayCount();
    final BusinessDayConvention businessDayConvention = convention.getBusinessDayConvention();
    final boolean eomLeg = convention.isIsEOM();
    final int spotLagLeg = convention.getSettlementDays();
    final ZonedDateTime spotDateLeg = ScheduleCalculator.getAdjustedDate(_valuationTime, spotLagLeg, calendar);
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spotDateLeg,
                                                                       swapNode.getStartTenor().getPeriod(),
                                                                       businessDayConvention,
                                                                       calendar,
                                                                       eomLeg);
    final Period paymentPeriod = convention.getPaymentTenor().getPeriod();
    final Period maturityTenor = swapNode.getMaturityTenor().getPeriod();

    return Pair.of(new FixedInterestRateLeg(dayCount,
                                            PeriodFrequency.of(paymentPeriod),
                                            convention.getRegionCalendar(),
                                            businessDayConvention,
                                            new InterestRateNotional(currency, _amount),
                                            eomLeg,
                                            _rate),
                   Triple.of(startDate, spotDateLeg, _valuationTime.plus(maturityTenor)));
  }

  //TODO do we actually need the settlement days for the swap, not the index?
  private Pair<? extends FloatingInterestRateLeg, Triple<ZonedDateTime, ZonedDateTime, ZonedDateTime>> getIborLeg(final ExternalId floatingReferenceRateId,
                                                                                                                  final VanillaIborLegConvention convention,
                                                                                                                  final SwapNode swapNode,
                                                                                                                  final boolean isPayer,
                                                                                                                  final boolean isFloatFloat) {
    final Convention underlyingConvention = _conventionSource.getConvention(convention.getIborIndexConvention());
    if (!(underlyingConvention instanceof IborIndexConvention)) {
      if (underlyingConvention == null) {
        throw new OpenGammaRuntimeException("Could not get convention with id " + convention.getIborIndexConvention());
      }
      throw new OpenGammaRuntimeException("Convention of the underlying was not an ibor index convention; have " + underlyingConvention.getClass());
    }
    final IborIndexConvention indexConvention = (IborIndexConvention) underlyingConvention;
    final Currency currency = indexConvention.getCurrency();
    final DayCount dayCount = indexConvention.getDayCount();
    final BusinessDayConvention businessDayConvention = indexConvention.getBusinessDayConvention();
    final boolean eomIndex = indexConvention.isIsEOM();
    final boolean eomLeg = convention.isIsEOM();
    final Period indexTenor = convention.getResetTenor().getPeriod();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource,
                                                        _holidaySource,
                                                        indexConvention.getFixingCalendar());
    final int spotLag = indexConvention.getSettlementDays();
    final IborIndex iborIndex = new IborIndex(currency,
                                              indexTenor,
                                              spotLag,
                                              dayCount,
                                              businessDayConvention,
                                              eomIndex,
                                              indexConvention.getName());
    final Period maturityTenor = swapNode.getMaturityTenor().getPeriod();
    final int spotLagLeg = convention.getSettlementDays();
    final ZonedDateTime spotDateLeg = ScheduleCalculator.getAdjustedDate(_valuationTime, spotLagLeg, calendar);
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spotDateLeg,
                                                                       swapNode.getStartTenor().getPeriod(),
                                                                       businessDayConvention,
                                                                       calendar,
                                                                       eomLeg);
    if (isFloatFloat) {
      //return AnnuityCouponIborSpreadDefinition.from(startDate, maturityTenor, 1, iborIndex, spread, isPayer, calendar);
      return Pair.of(new FloatingSpreadIRLeg(dayCount,
                                             PeriodFrequency.of(convention.getResetTenor().getPeriod()),
                                             indexConvention.getRegionCalendar(),
                                             businessDayConvention,
                                             new InterestRateNotional(currency, _amount),
                                             eomLeg,
                                             floatingReferenceRateId,
                                             FloatingRateType.IBOR,
                                             _rate),
                     Triple.of(startDate, spotDateLeg, _valuationTime.plus(maturityTenor)));
    }
    //return AnnuityCouponIborDefinition.from(startDate, maturityTenor, 1, iborIndex, isPayer, calendar);
    return Pair.of(new FloatingInterestRateLeg(dayCount,
                                               PeriodFrequency.of(maturityTenor),
                                               indexConvention.getRegionCalendar(),
                                               businessDayConvention,
                                               new InterestRateNotional(currency, _amount),
                                               eomLeg,
                                               floatingReferenceRateId, FloatingRateType.IBOR),
                   Triple.of(startDate, spotDateLeg, _valuationTime.plus(maturityTenor)));

  }

  private Pair<? extends FloatingInterestRateLeg, Triple<ZonedDateTime, ZonedDateTime, ZonedDateTime>> getOISLeg(final ExternalId floatingReferenceRateId,
                                                                                                                 final OISLegConvention convention,
                                                                                                                 final SwapNode swapNode,
                                                                                                                 final boolean isPayer,
                                                                                                                 final boolean isFloatFloat) {
    final OvernightIndexConvention indexConvention = (OvernightIndexConvention) _conventionSource.getConvention(
        convention.getOvernightIndexConvention());
    final Currency currency = indexConvention.getCurrency();
    final DayCount dayCount = indexConvention.getDayCount();
    final int publicationLag = indexConvention.getPublicationLag();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource,
                                                        _holidaySource,
                                                        indexConvention.getRegionCalendar());
    final int spotLagLeg = convention.getSettlementDays();
    final ZonedDateTime spotDateLeg = ScheduleCalculator.getAdjustedDate(_valuationTime, spotLagLeg, calendar);
    final Period maturityTenor = swapNode.getMaturityTenor().getPeriod();
    final IndexON indexON = new IndexON(indexConvention.getName(), currency, dayCount, publicationLag);
    final Period paymentPeriod = convention.getPaymentTenor().getPeriod();
    final boolean eomLeg = convention.isIsEOM();
    final BusinessDayConvention businessDayConvention = convention.getBusinessDayConvention();
    final int paymentLag = convention.getPaymentLag();
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spotDateLeg,
                                                                       swapNode.getStartTenor().getPeriod(),
                                                                       businessDayConvention,
                                                                       calendar,
                                                                       eomLeg);
    if (isFloatFloat) {
      //return AnnuityCouponONSpreadSimplifiedDefinition.from(startDate, maturityTenor, 1, spread, isPayer, indexON, paymentLag, calendar, businessDayConvention, paymentPeriod, eomLeg);
      return Pair.of(new FloatingSpreadIRLeg(dayCount,
                                             PeriodFrequency.of(paymentPeriod),
                                             indexConvention.getRegionCalendar(),
                                             businessDayConvention,
                                             new InterestRateNotional(currency, _amount),
                                             eomLeg,
                                             floatingReferenceRateId,
                                             FloatingRateType.OIS,
                                             _rate),
                     Triple.of(startDate, spotDateLeg, _valuationTime.plus(maturityTenor)));
    }
    //return AnnuityCouponONSimplifiedDefinition.from(startDate, maturityTenor, 1, isPayer, indexON, paymentLag, calendar, businessDayConvention, paymentPeriod, eomLeg);
    return Pair.of(new FloatingInterestRateLeg(dayCount,
                                               PeriodFrequency.of(maturityTenor),
                                               indexConvention.getRegionCalendar(),
                                               businessDayConvention,
                                               new InterestRateNotional(currency, _amount),
                                               eomLeg,
                                               floatingReferenceRateId, FloatingRateType.OIS),
                   Triple.of(startDate, spotDateLeg, _valuationTime.plus(maturityTenor)));

  }


  @Override
  public CashSecurity visitCashNode(final CashNode cashNode) {
    final Convention convention = _conventionSource.getConvention(cashNode.getConvention());
    if (convention == null) {
      throw new OpenGammaRuntimeException("Convention with id " + cashNode.getConvention() + " was null");
    }
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
      final ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(_valuationTime, settlementDays, calendar);
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
      final ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(_valuationTime, settlementDays, calendar);
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
      return new CashSecurity(currency,
                              iborConvention.getRegionCalendar(),
                              startDate,
                              endDate,
                              dayCount,
                              _rate,
                              _amount);
    } else {
      throw new OpenGammaRuntimeException("Could not handle convention of type " + convention.getClass());
    }
  }

  @Override
  public FutureSecurity visitRateFutureNode(final RateFutureNode rateFuture) {
    final Convention futureConvention = _conventionSource.getConvention(rateFuture.getFutureConvention());
    if (futureConvention == null) {
      throw new OpenGammaRuntimeException("Future convention was null");
    }
    if (futureConvention instanceof InterestRateFutureConvention) {
      return getInterestRateFuture(rateFuture, (InterestRateFutureConvention) futureConvention, _rate);
    } else if (futureConvention instanceof FederalFundsFutureConvention) {
      return getFederalFundsFuture(rateFuture, (FederalFundsFutureConvention) futureConvention, _rate);
    }
    throw new OpenGammaRuntimeException("Could not handle future convention of type " + futureConvention.getClass());
  }


  /**
   * Creates an interest rate future security from a rate future node.
   *
   * @param rateFuture The rate future node
   * @param futureConvention The future convention
   * @param price The price
   * @return The interest rate future
   */
  private InterestRateFutureSecurity getInterestRateFuture(final RateFutureNode rateFuture, final InterestRateFutureConvention futureConvention,
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
    //return transactionDefinition;

    final Expiry expiry = new Expiry(expiryDate);
    return new InterestRateFutureSecurity(expiry, "TRADING_EXCHANGE", "SETTLEMENT_EXCHANGE", currency, _amount, _identifier, "CATEGORY");

  }

  /**
   * Creates a Federal fund future security from a rate future node.
   *
   * @param rateFuture The rate future node
   * @param futureConvention The future convention
   * @param price The price
   * @return The Fed fund future
   */
  private FederalFundsFutureSecurity getFederalFundsFuture(final RateFutureNode rateFuture, final FederalFundsFutureConvention futureConvention,
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
    //return transactionDefinition;

    final Expiry expiry = new Expiry(expiryDate);
    return new FederalFundsFutureSecurity(expiry, "TRADING_EXCHANGE", "SETTLEMENT_EXCHANGE", currency, _amount, _identifier, "CATEGORY");
  }
}
