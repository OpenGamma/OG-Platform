/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalTime;
import javax.time.calendar.Period;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.deposit.PeriodicZeroDepositSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;

/**
 * Converts specifications into fully resolved security definitions
 */
public class FixedIncomeStripIdentifierAndMaturityBuilder {

  private static final LocalTime CASH_EXPIRY_TIME = LocalTime.of(11, 00);

  private final RegionSource _regionSource;
  private final ConventionBundleSource _conventionBundleSource;
  private final SecuritySource _secSource;
  private final HolidaySource _holidaySource;

  public FixedIncomeStripIdentifierAndMaturityBuilder(final RegionSource regionSource, final ConventionBundleSource conventionBundleSource, final SecuritySource secSource,
      final HolidaySource holidaySource) {
    _regionSource = regionSource;
    _conventionBundleSource = conventionBundleSource;
    _secSource = secSource;
    _holidaySource = holidaySource;
  }

  public InterpolatedYieldCurveSpecificationWithSecurities resolveToSecurity(final InterpolatedYieldCurveSpecification curveSpecification, final Map<ExternalId, Double> marketValues) {
    final LocalDate curveDate = curveSpecification.getCurveDate();
    final Collection<FixedIncomeStripWithSecurity> securityStrips = new ArrayList<FixedIncomeStripWithSecurity>();
    for (final FixedIncomeStripWithIdentifier strip : curveSpecification.getStrips()) {
      Security security;
      ZonedDateTime maturity; // Should the maturity be computed from the "security" (with a visitor)?
      switch (strip.getInstrumentType()) {
        case CASH:
          final CashSecurity cashSecurity = getCash(curveSpecification, strip, marketValues);
          if (cashSecurity == null) {
            throw new OpenGammaRuntimeException("Could not resolve cash curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification);
          }
          final Region region = _regionSource.getHighestLevelRegion(cashSecurity.getRegionId());
          TimeZone timeZone = region.getTimeZone();
          timeZone = ensureZone(timeZone);
          maturity = curveDate.plus(strip.getMaturity().getPeriod()).atTime(CASH_EXPIRY_TIME).atZone(timeZone);
          //        maturity = cashSecurity.getMaturity();
          security = cashSecurity;
          break;
        case FRA_3M: {
          final FRASecurity fraSecurity = getFRA(curveSpecification, strip, marketValues, Tenor.THREE_MONTHS);
          if (fraSecurity == null) {
            throw new OpenGammaRuntimeException("Could not resolve FRA curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification);
          }
          maturity = fraSecurity.getEndDate();
          security = fraSecurity;
          break;
        }
        case FRA_6M: {
          final FRASecurity fraSecurity = getFRA(curveSpecification, strip, marketValues, Tenor.SIX_MONTHS);
          if (fraSecurity == null) {
            throw new OpenGammaRuntimeException("Could not resolve FRA curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification);
          }
          maturity = fraSecurity.getEndDate();
          security = fraSecurity;
          break;
        }
        case FRA: {
          // In case there's any old curve definitions hanging around - assume that all FRAs are 3m
          // TODO get defaults from convention? (e.g. USD = 3m, EUR = 6M)
          final FRASecurity fraSecurity = getFRA(curveSpecification, strip, marketValues, Tenor.THREE_MONTHS);
          if (fraSecurity == null) {
            throw new OpenGammaRuntimeException("Could not resolve FRA curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification);
          }
          maturity = fraSecurity.getEndDate();
          security = fraSecurity;
          break;
        }
        case FUTURE:
          // TODO: jim 17-Aug-2010 -- we need to sort out the zoned date time related to the expiry.
          final FutureSecurity futureSecurity = getFuture(strip);
          if (futureSecurity == null) {
            throw new OpenGammaRuntimeException("Could not resolve future curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification);
          }
          maturity = futureSecurity.getExpiry().getExpiry();
          security = futureSecurity;
          break;
        case LIBOR: {
          final CashSecurity rateSecurity = getCash(curveSpecification, strip, marketValues);
          if (rateSecurity == null) {
            throw new OpenGammaRuntimeException("Could not resolve Libor curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification);
          }
          final Region region2 = _regionSource.getHighestLevelRegion(rateSecurity.getRegionId());
          TimeZone timeZone2 = region2.getTimeZone();
          timeZone2 = ensureZone(timeZone2);
          maturity = curveDate.plus(strip.getMaturity().getPeriod()).atTime(CASH_EXPIRY_TIME).atZone(timeZone2);
          security = rateSecurity;
          break;
        }
        case EURIBOR: {
          final CashSecurity rateSecurity = getCash(curveSpecification, strip, marketValues);
          if (rateSecurity == null) {
            throw new OpenGammaRuntimeException("Could not resolve Euribor curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification);
          }
          final Region region2 = _regionSource.getHighestLevelRegion(rateSecurity.getRegionId());
          TimeZone timeZone2 = region2.getTimeZone();
          timeZone2 = ensureZone(timeZone2);
          maturity = curveDate.plus(strip.getMaturity().getPeriod()).atTime(CASH_EXPIRY_TIME).atZone(timeZone2);
          security = rateSecurity;
          break;
        }
        case CDOR: {
          final CashSecurity rateSecurity = getCash(curveSpecification, strip, marketValues);
          if (rateSecurity == null) {
            throw new OpenGammaRuntimeException("Could not resolve CDOR curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification);
          }
          final Region region2 = _regionSource.getHighestLevelRegion(rateSecurity.getRegionId());
          TimeZone timeZone2 = region2.getTimeZone();
          timeZone2 = ensureZone(timeZone2);
          maturity = curveDate.plus(strip.getMaturity().getPeriod()).atTime(CASH_EXPIRY_TIME).atZone(timeZone2);
          security = rateSecurity;
          break;
        }
        case CIBOR: {
          final CashSecurity rateSecurity = getCash(curveSpecification, strip, marketValues);
          if (rateSecurity == null) {
            throw new OpenGammaRuntimeException("Could not resolve CIBOR curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification);
          }
          final Region region2 = _regionSource.getHighestLevelRegion(rateSecurity.getRegionId());
          TimeZone timeZone2 = region2.getTimeZone();
          timeZone2 = ensureZone(timeZone2);
          maturity = curveDate.plus(strip.getMaturity().getPeriod()).atTime(CASH_EXPIRY_TIME).atZone(timeZone2);
          security = rateSecurity;
          break;
        }
        case STIBOR: {
          final CashSecurity rateSecurity = getCash(curveSpecification, strip, marketValues);
          if (rateSecurity == null) {
            throw new OpenGammaRuntimeException("Could not resolve STIBOR curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification);
          }
          final Region region2 = _regionSource.getHighestLevelRegion(rateSecurity.getRegionId());
          TimeZone timeZone2 = region2.getTimeZone();
          timeZone2 = ensureZone(timeZone2);
          maturity = curveDate.plus(strip.getMaturity().getPeriod()).atTime(CASH_EXPIRY_TIME).atZone(timeZone2);
          security = rateSecurity;
          break;
        }
        case SWAP: {
          // In case there's any old curve definitions hanging around - assume that all swaps are 3m
          // TODO get defaults from convention? (e.g. USD = 3m, EUR = 6M)
          final SwapSecurity swapSecurity = getSwap(curveSpecification, strip, marketValues, Tenor.THREE_MONTHS);
          if (swapSecurity == null) {
            throw new OpenGammaRuntimeException("Could not resolve swap curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification);
          }
          maturity = swapSecurity.getMaturityDate();
          security = swapSecurity;
          break;
        }
        case SWAP_3M: {
          final SwapSecurity swapSecurity = getSwap(curveSpecification, strip, marketValues, Tenor.THREE_MONTHS);
          if (swapSecurity == null) {
            throw new OpenGammaRuntimeException("Could not resolve swap curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification);
          }
          maturity = swapSecurity.getMaturityDate();
          security = swapSecurity;
          break;
        }
        case SWAP_6M: {
          final SwapSecurity swapSecurity = getSwap(curveSpecification, strip, marketValues, Tenor.SIX_MONTHS);
          if (swapSecurity == null) {
            throw new OpenGammaRuntimeException("Could not resolve swap curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification);
          }
          maturity = swapSecurity.getMaturityDate();
          security = swapSecurity;
          break;
        }
        case TENOR_SWAP:
          final SwapSecurity tenorSwapSecurity = getTenorSwap(curveSpecification, strip, marketValues);
          if (tenorSwapSecurity == null) {
            throw new OpenGammaRuntimeException("Could not resolve swap curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification);
          }
          maturity = tenorSwapSecurity.getMaturityDate();
          security = tenorSwapSecurity;
          break;
        case OIS_SWAP:
          security = getOISSwap(curveSpecification, strip, marketValues);
          maturity = curveDate.plus(strip.getMaturity().getPeriod()).atTime(11, 00).atZone(TimeZone.UTC);
          break;
        case PERIODIC_ZERO_DEPOSIT:
          final PeriodicZeroDepositSecurity depositSecurity = getPeriodicZeroDeposit(curveSpecification, strip, marketValues);
          maturity = depositSecurity.getMaturityDate();
          security = depositSecurity;
          break;
        case BASIS_SWAP:
          final SwapSecurity basisSwapSecurity = getBasisSwap(curveSpecification, strip, marketValues);
          if (basisSwapSecurity == null) {
            throw new OpenGammaRuntimeException("Could not resolve basis swap curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification);
          }
          security = basisSwapSecurity;
          maturity = basisSwapSecurity.getMaturityDate();
          break;
        default:
          throw new OpenGammaRuntimeException("Unhandled type of instrument in curve definition " + strip.getInstrumentType());
      }
      final Tenor resolvedTenor = new Tenor(Period.between(curveDate, maturity.toLocalDate()));
      securityStrips.add(new FixedIncomeStripWithSecurity(strip.getStrip(), resolvedTenor, maturity, strip.getSecurity(), security));
    }
    return new InterpolatedYieldCurveSpecificationWithSecurities(curveDate, curveSpecification.getName(), curveSpecification.getCurrency(), curveSpecification.getInterpolator(), securityStrips);
  }

  private CashSecurity getCash(final InterpolatedYieldCurveSpecification spec, final FixedIncomeStripWithIdentifier strip, final Map<ExternalId, Double> marketValues) {
    final ConventionBundle cashConvention = _conventionBundleSource.getConventionBundle(strip.getSecurity());
    if (cashConvention == null) {
      throw new OpenGammaRuntimeException("No convention for cash " + strip.getSecurity() + " so can't establish business day convention");
    }
    if (cashConvention.getRegion() == null) {
      throw new OpenGammaRuntimeException("Region for strip " + strip + " was null");
    }
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, cashConvention.getRegion());
    if (calendar == null) {
      throw new OpenGammaRuntimeException("Calendar was null");
    }
    final ZonedDateTime curveDate = spec.getCurveDate().atStartOfDayInZone(TimeZone.UTC);
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(curveDate, cashConvention.getSettlementDays(), calendar);
    final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, cashConvention.getPeriod(), cashConvention.getBusinessDayConvention(), calendar, cashConvention.isEOMConvention());
    final Double rate = marketValues.get(strip.getSecurity());
    if (rate == null) {
      throw new OpenGammaRuntimeException("No market data for " + strip.getSecurity());
    }
    final CashSecurity sec = new CashSecurity(spec.getCurrency(), spec.getRegion(), startDate, endDate, cashConvention.getDayCount(), rate, 1.0d);
    sec.setExternalIdBundle(ExternalIdBundle.of(strip.getSecurity()));
    return sec;
  }

  private FRASecurity getFRA(final InterpolatedYieldCurveSpecification spec, final FixedIncomeStripWithIdentifier strip, final Map<ExternalId, Double> marketValues, final Tenor tenor) {
    final ExternalId fraIdentifier = strip.getSecurity();
    final int months = tenor.getPeriod().getMonths();
    final ConventionBundle fraConvention = _conventionBundleSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, spec.getCurrency().getCode() + "_" + months
        + "M_FRA"));
    if (fraConvention == null) {
      throw new OpenGammaRuntimeException("Could not get convention for " + fraIdentifier + ": tried "
          + ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, spec.getCurrency().getCode() + "_" + months + "M_FRA"));
    }
    final ExternalId underlyingIdentifier = fraConvention.getSwapFloatingLegInitialRate();
    final ConventionBundle iborConvention = _conventionBundleSource.getConventionBundle(underlyingIdentifier);
    final Period fraPeriod = iborConvention.getPeriod();
    final Currency ccy = spec.getCurrency();
    final BusinessDayConvention businessDayConvention = iborConvention.getBusinessDayConvention();
    final boolean eom = iborConvention.isEOMConvention();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, fraConvention.getSwapFloatingLegRegion());
    final ZonedDateTime curveDate = spec.getCurveDate().atStartOfDayInZone(TimeZone.UTC); // TODO: review?
    final ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(curveDate, iborConvention.getSettlementDays(), calendar);
    final Period endPeriod = strip.getMaturity().getPeriod();
    final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(spotDate, endPeriod, businessDayConvention, calendar, eom);
    final Period startPeriod = endPeriod.minus(fraPeriod).normalized(); // TODO: check period >0?
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spotDate, startPeriod, businessDayConvention, calendar, eom);
    final ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(startDate, -iborConvention.getSettlementDays(), calendar);
    return new FRASecurity(ccy, spec.getRegion(), startDate, endDate, marketValues.get(strip.getSecurity()), 1.0d, underlyingIdentifier, fixingDate);
  }

  private FutureSecurity getFuture(final FixedIncomeStripWithIdentifier strip) {
    return (FutureSecurity) _secSource.getSecurity(ExternalIdBundle.of(strip.getSecurity()));
  }

  private SwapSecurity getSwap(final InterpolatedYieldCurveSpecification spec, final FixedIncomeStripWithIdentifier strip, final Map<ExternalId, Double> marketValues, final Tenor resetTenor) {
    final ExternalId swapIdentifier = strip.getSecurity();
    final int months = resetTenor.getPeriod().getMonths();
    ConventionBundle swapConvention = _conventionBundleSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, spec.getCurrency().getCode() + "_" + months
        + "M_SWAP"));
    if (swapConvention == null) {
      swapConvention = _conventionBundleSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, spec.getCurrency().getCode() + "_SWAP"));
    }
    if (swapConvention == null) {
      throw new OpenGammaRuntimeException("Could not get convention for " + swapIdentifier + ": tried "
          + ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, spec.getCurrency().getCode() + "_" + months + "M_SWAP") + " and "
          + ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, spec.getCurrency().getCode() + "_SWAP"));
    }
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, swapConvention.getSwapFloatingLegRegion());
    final ZonedDateTime curveDate = spec.getCurveDate().atStartOfDayInZone(TimeZone.UTC);
    final ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(curveDate, swapConvention.getSwapFixedLegSettlementDays(), calendar);
    final ZonedDateTime maturityDate = spotDate.plus(strip.getMaturity().getPeriod());
    final String counterparty = "";
    final ExternalId floatingRateId = swapConvention.getSwapFloatingLegInitialRate();
    if (floatingRateId == null) {
      throw new OpenGammaRuntimeException("Could not get + " + floatingRateId + " from convention");
    }
    final Double rate = marketValues.get(swapIdentifier);
    if (rate == null) {
      throw new OpenGammaRuntimeException("rate was null on " + strip + " from " + spec);
    }
    final double fixedRate = rate;
    final FloatingInterestRateLeg iborLeg = new FloatingInterestRateLeg(swapConvention.getSwapFloatingLegDayCount(), swapConvention.getSwapFloatingLegFrequency(),
        swapConvention.getSwapFloatingLegRegion(), swapConvention.getSwapFloatingLegBusinessDayConvention(), new InterestRateNotional(spec.getCurrency(), 1), false, floatingRateId,
        FloatingRateType.IBOR);
    final FixedInterestRateLeg fixedLeg = new FixedInterestRateLeg(swapConvention.getSwapFixedLegDayCount(), swapConvention.getSwapFixedLegFrequency(), swapConvention.getSwapFixedLegRegion(),
        swapConvention.getSwapFixedLegBusinessDayConvention(), new InterestRateNotional(spec.getCurrency(), 1), false, fixedRate);
    final SwapSecurity swap = new SwapSecurity(curveDate, spotDate, maturityDate, counterparty, iborLeg, fixedLeg);
    swap.setExternalIdBundle(ExternalIdBundle.of(swapIdentifier));
    return swap;
  }

  private SwapSecurity getBasisSwap(final InterpolatedYieldCurveSpecification spec, final FixedIncomeStripWithIdentifier strip, final Map<ExternalId, Double> marketValues) {
    final ExternalId swapIdentifier = strip.getSecurity();
    final ZonedDateTime curveDate = spec.getCurveDate().atStartOfDayInZone(TimeZone.UTC);
    final FixedIncomeStrip fixedIncomeStrip = strip.getStrip();
    final IndexType payIndexType = fixedIncomeStrip.getPayIndexType();
    final Tenor payTenor = fixedIncomeStrip.getPayTenor();
    final IndexType receiveIndexType = fixedIncomeStrip.getReceiveIndexType();
    final Tenor receiveTenor = fixedIncomeStrip.getReceiveTenor();
    final String ccy = spec.getCurrency().getCode();
    final ExternalId payFloatingReferenceRateId = ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, payIndexType + "_" + ccy + "_" + payTenor.getPeriod().toString());
    final ExternalId receiveFloatingReferenceRateId = ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, receiveIndexType + "_" + ccy + "_" + receiveTenor.getPeriod().toString());
    final ConventionBundle payConvention = _conventionBundleSource.getConventionBundle(payFloatingReferenceRateId);
    final ConventionBundle receiveConvention = _conventionBundleSource.getConventionBundle(receiveFloatingReferenceRateId);
    final Calendar payCalendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, payConvention.getRegion());
    final ZonedDateTime paySpotDate = ScheduleCalculator.getAdjustedDate(curveDate, payConvention.getSettlementDays(), payCalendar);
    final ZonedDateTime payMaturityDate = paySpotDate.plus(strip.getMaturity().getPeriod());
    final String counterparty = "";
    final InterestRateNotional notional = new InterestRateNotional(spec.getCurrency(), 1);
    final ExternalId payRegionIdentifier = payConvention.getRegion();
    final DayCount payDayCount = payConvention.getDayCount();
    final Frequency payFrequency = PeriodFrequency.of(fixedIncomeStrip.getPayTenor().getPeriod());
    final BusinessDayConvention payBusinessDayConvention = payConvention.getBusinessDayConvention();
    final FloatingRateType payFloatingRateType = getFloatingTypeFromIndexType(fixedIncomeStrip.getPayIndexType());
    final ExternalId receiveRegionIdentifier = receiveConvention.getRegion();
    final DayCount receiveDayCount = receiveConvention.getDayCount();
    final Frequency receiveFrequency = PeriodFrequency.of(fixedIncomeStrip.getReceiveTenor().getPeriod());
    final BusinessDayConvention receiveBusinessDayConvention = receiveConvention.getBusinessDayConvention();
    final FloatingRateType receiveFloatingRateType = getFloatingTypeFromIndexType(fixedIncomeStrip.getReceiveIndexType());
    final FloatingInterestRateLeg payLeg = new FloatingInterestRateLeg(payDayCount, payFrequency, payRegionIdentifier, payBusinessDayConvention, notional, false, payFloatingReferenceRateId,
        payFloatingRateType);
    final double spread = marketValues.get(swapIdentifier);
    final FloatingSpreadIRLeg receiveLeg = new FloatingSpreadIRLeg(receiveDayCount, receiveFrequency, receiveRegionIdentifier, receiveBusinessDayConvention, notional, false,
        receiveFloatingReferenceRateId, receiveFloatingRateType, spread);
    //TODO don't use pay spot date and maturity date automatically
    final SwapSecurity swap = new SwapSecurity(curveDate, paySpotDate, payMaturityDate, counterparty, payLeg, receiveLeg);
    swap.setExternalIdBundle(ExternalIdBundle.of(swapIdentifier));
    return swap;
  }

  private SwapSecurity getTenorSwap(final InterpolatedYieldCurveSpecification spec, final FixedIncomeStripWithIdentifier strip, final Map<ExternalId, Double> marketValues) {
    final ExternalId swapIdentifier = strip.getSecurity();
    final Double rate = marketValues.get(swapIdentifier);
    final LocalDate curveDate = spec.getCurveDate();
    final ZonedDateTime tradeDate = curveDate.atTime(11, 00).atZone(TimeZone.UTC);
    final ZonedDateTime effectiveDate = DateUtils.previousWeekDay(curveDate.plusDays(3)).atTime(11, 00).atZone(TimeZone.UTC);
    final ZonedDateTime maturityDate = curveDate.plus(strip.getMaturity().getPeriod()).atTime(11, 00).atZone(TimeZone.UTC);
    final ConventionBundle convention = _conventionBundleSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, spec.getCurrency().getCode() + "_TENOR_SWAP"));
    final String counterparty = "";
    final ConventionBundle payLegFloatRateConvention = _conventionBundleSource.getConventionBundle(convention.getBasisSwapPayFloatingLegInitialRate());
    final ConventionBundle receiveLegFloatRateConvention = _conventionBundleSource.getConventionBundle(convention.getBasisSwapReceiveFloatingLegInitialRate());
    final ExternalId payLegFloatRateBloombergTicker = payLegFloatRateConvention.getIdentifiers().getExternalId(ExternalSchemes.BLOOMBERG_TICKER);
    final ExternalId receiveLegFloatRateBloombergTicker = receiveLegFloatRateConvention.getIdentifiers().getExternalId(ExternalSchemes.BLOOMBERG_TICKER);
    if (rate == null) {
      throw new OpenGammaRuntimeException("Could not get spread; was trying " + swapIdentifier);
    }
    final double spread = rate;
    // REVIEW: jim 25-Aug-2010 -- we need to change the swap to take settlement days.

    final SwapSecurity swap = new SwapSecurity(tradeDate, effectiveDate, maturityDate, counterparty, new FloatingInterestRateLeg(convention.getBasisSwapPayFloatingLegDayCount(),
        convention.getBasisSwapPayFloatingLegFrequency(), convention.getBasisSwapPayFloatingLegRegion(), convention.getBasisSwapPayFloatingLegBusinessDayConvention(), new InterestRateNotional(
            spec.getCurrency(), 1), false, payLegFloatRateBloombergTicker, FloatingRateType.IBOR), new FloatingSpreadIRLeg(convention.getBasisSwapReceiveFloatingLegDayCount(),
        convention.getBasisSwapReceiveFloatingLegFrequency(), convention.getBasisSwapReceiveFloatingLegRegion(), convention.getBasisSwapReceiveFloatingLegBusinessDayConvention(),
        new InterestRateNotional(spec.getCurrency(), 1), false, receiveLegFloatRateBloombergTicker, FloatingRateType.IBOR, spread));
    swap.setExternalIdBundle(ExternalIdBundle.of(swapIdentifier));
    return swap;
  }

  private SwapSecurity getOISSwap(final InterpolatedYieldCurveSpecification spec, final FixedIncomeStripWithIdentifier strip, final Map<ExternalId, Double> marketValues) {
    final FixedIncomeStrip underlyingStrip = strip.getStrip();
    final ExternalId swapIdentifier = strip.getSecurity();
    final ConventionBundle swapConvention = _conventionBundleSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, spec.getCurrency().getCode() + "_OIS_SWAP"));
    if (swapConvention == null) {
      throw new OpenGammaRuntimeException("Could not get convention for id " + ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, spec.getCurrency().getCode() + "_OIS_SWAP"));
    }
    if (!swapConvention.getSwapFloatingLegFrequency().equals(swapConvention.getSwapFixedLegFrequency())) {
      throw new OpenGammaRuntimeException("Payment frequencies for the fixed and floating legs did not match");
    }
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, swapConvention.getSwapFloatingLegRegion());
    final ZonedDateTime curveDate = spec.getCurveDate().atStartOfDayInZone(TimeZone.UTC);
    final ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(curveDate, swapConvention.getSwapFixedLegSettlementDays(), calendar);
    final ZonedDateTime maturityDate = spotDate.plus(strip.getMaturity().getPeriod());
    final String counterparty = "";
    final Double rate = marketValues.get(swapIdentifier);
    if (rate == null) {
      throw new OpenGammaRuntimeException("rate was null on " + strip + " from " + spec);
    }
    final double fixedRate = rate;
    Frequency floatingFrequency;
    final ExternalId floatingReferenceRateId;
    if (underlyingStrip.getResetTenor() != null) {
      final Period resetTenor = underlyingStrip.getResetTenor().getPeriod();
      floatingFrequency = PeriodFrequency.of(resetTenor);
      final IndexType indexType = underlyingStrip.getIndexType();
      floatingReferenceRateId = ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, indexType + "_" + resetTenor.toString());
    } else {
      floatingFrequency = swapConvention.getSwapFloatingLegFrequency();
      if (floatingFrequency == null) {
        throw new OpenGammaRuntimeException("Could not get floating leg frequency " + floatingFrequency + " from convention");
      }
      floatingReferenceRateId = swapConvention.getSwapFloatingLegInitialRate();
      if (floatingReferenceRateId == null) {
        throw new OpenGammaRuntimeException("Could not get floating reference rate " + floatingReferenceRateId + " from convention");
      }
    }
    final FloatingInterestRateLeg oisLeg = new FloatingInterestRateLeg(swapConvention.getSwapFloatingLegDayCount(), floatingFrequency,
        swapConvention.getSwapFloatingLegRegion(), swapConvention.getSwapFloatingLegBusinessDayConvention(), new InterestRateNotional(spec.getCurrency(), 1), false, floatingReferenceRateId,
        FloatingRateType.OIS);
    final FixedInterestRateLeg fixedLeg = new FixedInterestRateLeg(swapConvention.getSwapFixedLegDayCount(), swapConvention.getSwapFixedLegFrequency(), swapConvention.getSwapFixedLegRegion(),
        swapConvention.getSwapFixedLegBusinessDayConvention(), new InterestRateNotional(spec.getCurrency(), 1), false, fixedRate);
    final SwapSecurity swap = new SwapSecurity(curveDate, spotDate, maturityDate, counterparty, oisLeg, fixedLeg);
    swap.setExternalIdBundle(ExternalIdBundle.of(swapIdentifier));
    return swap;
  }

  private PeriodicZeroDepositSecurity getPeriodicZeroDeposit(final InterpolatedYieldCurveSpecification spec, final FixedIncomeStripWithIdentifier strip, final Map<ExternalId, Double> marketValues) {
    final ExternalId id = strip.getSecurity();
    final Currency currency = spec.getCurrency();
    final ZonedDateTime startDate = spec.getCurveDate().atStartOfDayInZone(TimeZone.UTC);
    final ZonedDateTime maturityDate = startDate.plus(strip.getMaturity().getPeriod());
    final double rate = marketValues.get(id);
    final int compoundingPeriodsPerYear = strip.getStrip().getPeriodsPerYear();
    final ExternalId region = spec.getRegion();
    final PeriodicZeroDepositSecurity deposit = new PeriodicZeroDepositSecurity(currency, startDate, maturityDate, rate, compoundingPeriodsPerYear, region);
    deposit.setExternalIdBundle(ExternalIdBundle.of(id));
    return deposit;
  }

  private TimeZone ensureZone(final TimeZone zone) {
    if (zone != null) {
      return zone;
    }
    return TimeZone.UTC;
  }

  private FloatingRateType getFloatingTypeFromIndexType(final IndexType indexType) {
    switch (indexType) {
      case Libor:
        return FloatingRateType.IBOR;
      case Euribor:
        return FloatingRateType.IBOR;
    }
    throw new OpenGammaRuntimeException("Cannot handle index type " + indexType);
  }
}
