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
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.DefaultConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.security.cash.CashSecurity;
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

  private static final ConventionBundleSource s_conventionBundleSource = new DefaultConventionBundleSource(new InMemoryConventionBundleMaster());

  private final RegionSource _regionSource;
  private final ConventionBundleSource _conventionBundleSource;
  private final SecuritySource _secSource;

  public FixedIncomeStripIdentifierAndMaturityBuilder(final RegionSource regionSource, final ConventionBundleSource conventionBundleSource, final SecuritySource secSource) {
    _regionSource = regionSource;
    _conventionBundleSource = conventionBundleSource;
    _secSource = secSource;
  }

  public InterpolatedYieldCurveSpecificationWithSecurities resolveToSecurity(final InterpolatedYieldCurveSpecification curveSpecification, final Map<ExternalId, Double> marketValues) {
    final LocalDate curveDate = curveSpecification.getCurveDate();
    final Collection<FixedIncomeStripWithSecurity> securityStrips = new ArrayList<FixedIncomeStripWithSecurity>();
    for (final FixedIncomeStripWithIdentifier strip : curveSpecification.getStrips()) {
      Security security;
      ZonedDateTime maturity;
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
          final Tenor tenor = strip.getMaturity();
          if (tenor.getPeriod().getYears() != 0) {
            security = getOISSwap(curveSpecification, strip, marketValues);
          } else if ((tenor.getPeriod().getMonths() != 0 && tenor.getPeriod().getMonths() < 12) || tenor.getPeriod().getDays() != 0) {
            security = getOISCash(curveSpecification, strip, marketValues);
          } else {
            throw new OpenGammaRuntimeException("Cannot handle OIS swaps of tenor " + tenor);
          }
          maturity = curveDate.plus(strip.getMaturity().getPeriod()).atTime(11, 00).atZone(TimeZone.UTC);
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
    final Double rate = marketValues.get(strip.getSecurity());
    if (rate == null) {
      throw new OpenGammaRuntimeException("No market data for " + strip.getSecurity());
    }
    final CashSecurity sec = new CashSecurity(spec.getCurrency(), spec.getRegion(), spec.getCurveDate().plus(strip.getMaturity().getPeriod()).atTime(11, 00).atZone(TimeZone.UTC), rate, 1.0d);
    sec.setExternalIdBundle(ExternalIdBundle.of(strip.getSecurity()));
    return sec;
  }

  private FRASecurity getFRA(final InterpolatedYieldCurveSpecification spec, final FixedIncomeStripWithIdentifier strip, final Map<ExternalId, Double> marketValues, final Tenor tenor) {
    final LocalDate curveDate = spec.getCurveDate(); // quick hack
    // TODO emcleod this offset and start and end date calculations should not be done like this - it's consistent with what was there before but I'm not why it is that way.
    // It seems that getting the date right should use holidays and business day conventions
    int offset;
    if (tenor.equals(Tenor.ofMonths(3))) {
      offset = 3;
    } else if (tenor.equals(Tenor.ofMonths(6))) {
      offset = 6;
    } else {
      throw new OpenGammaRuntimeException("Can only handle 3m or 6m FRAs");
    }
    final LocalDate startDate = curveDate.plus(strip.getMaturity().getPeriod()).minus(Period.ofMonths(offset));
    final LocalDate endDate = startDate.plus(Period.ofMonths(offset));
    final ExternalId underlyingIdentifier = strip.getSecurity();
    //TODO this normalization should not be done here
    return new FRASecurity(spec.getCurrency(), spec.getRegion(), startDate.atTime(11, 00).atZone(TimeZone.UTC), endDate.atTime(11, 00).atZone(TimeZone.UTC),
        marketValues.get(strip.getSecurity()), 1.0d, underlyingIdentifier);
  }

  private FutureSecurity getFuture(final FixedIncomeStripWithIdentifier strip) {
    return (FutureSecurity) _secSource.getSecurity(ExternalIdBundle.of(strip.getSecurity()));
  }

  private SwapSecurity getSwap(final InterpolatedYieldCurveSpecification spec, final FixedIncomeStripWithIdentifier strip, final Map<ExternalId, Double> marketValues, final Tenor resetTenor) {
    final ExternalId swapIdentifier = strip.getSecurity();
    final int months = resetTenor.getPeriod().getMonths(); //TODO this isn't right - what if someone's used 1Y?
    final Double rate = marketValues.get(swapIdentifier);
    final LocalDate curveDate = spec.getCurveDate();
    final ConventionBundleSource source = s_conventionBundleSource;
    final ZonedDateTime tradeDate = curveDate.atTime(11, 00).atZone(TimeZone.UTC);
    final ZonedDateTime effectiveDate = DateUtils.previousWeekDay(curveDate.plusDays(3)).atTime(11, 00).atZone(TimeZone.UTC);
    final ZonedDateTime maturityDate = curveDate.plus(strip.getMaturity().getPeriod()).atTime(11, 00).atZone(TimeZone.UTC);
    ConventionBundle convention = _conventionBundleSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, spec.getCurrency().getCode() + "_" + months + "M_SWAP"));
    if (convention == null) {
      convention = _conventionBundleSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, spec.getCurrency().getCode() + "_SWAP"));
    }
    if (convention == null) {
      throw new OpenGammaRuntimeException("Could not get convention for " + swapIdentifier + ": tried "
          + ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, spec.getCurrency().getCode() + "_" + months + "M_SWAP") + " and "
          + ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, spec.getCurrency().getCode() + "_SWAP"));
    }
    final String counterparty = "";
    final ExternalId floatingRateId = convention.getSwapFloatingLegInitialRate();
    if (floatingRateId == null) {
      throw new OpenGammaRuntimeException("Could not get + " + floatingRateId + " from convention");
    }
    final ConventionBundle floatRateConvention = source.getConventionBundle(floatingRateId);
    final ExternalId floatRateBloombergTicker = floatRateConvention.getIdentifiers().getExternalId(SecurityUtils.BLOOMBERG_TICKER);
    if (rate == null) {
      throw new OpenGammaRuntimeException("rate was null on " + strip + " from " + spec);
    }
    final double fixedRate = rate;
    // REVIEW: jim 25-Aug-2010 -- we need to change the swap to take settlement days.
    final SwapSecurity swap = new SwapSecurity(tradeDate, effectiveDate, maturityDate, counterparty, new FloatingInterestRateLeg(convention.getSwapFloatingLegDayCount(), 
        convention.getSwapFloatingLegFrequency(), convention.getSwapFloatingLegRegion(), convention.getSwapFloatingLegBusinessDayConvention(), new InterestRateNotional(spec.getCurrency(), 1), 
        false, floatRateBloombergTicker, FloatingRateType.IBOR), new FixedInterestRateLeg(convention.getSwapFixedLegDayCount(), convention.getSwapFixedLegFrequency(), 
            convention.getSwapFixedLegRegion(), convention.getSwapFixedLegBusinessDayConvention(), new InterestRateNotional(spec.getCurrency(), 1), false, fixedRate));
    swap.setExternalIdBundle(ExternalIdBundle.of(swapIdentifier));
    return swap;
  }

  private SwapSecurity getTenorSwap(final InterpolatedYieldCurveSpecification spec, final FixedIncomeStripWithIdentifier strip, final Map<ExternalId, Double> marketValues) {
    final ExternalId swapIdentifier = strip.getSecurity();
    final Double rate = marketValues.get(swapIdentifier);
    final LocalDate curveDate = spec.getCurveDate();
    final ConventionBundleSource source = s_conventionBundleSource;
    final ZonedDateTime tradeDate = curveDate.atTime(11, 00).atZone(TimeZone.UTC);
    final ZonedDateTime effectiveDate = DateUtils.previousWeekDay(curveDate.plusDays(3)).atTime(11, 00).atZone(TimeZone.UTC);
    final ZonedDateTime maturityDate = curveDate.plus(strip.getMaturity().getPeriod()).atTime(11, 00).atZone(TimeZone.UTC);
    final ConventionBundle convention = _conventionBundleSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, spec.getCurrency().getCode() + "_TENOR_SWAP"));
    final String counterparty = "";
    final ConventionBundle payLegFloatRateConvention = source.getConventionBundle(convention.getBasisSwapPayFloatingLegInitialRate());
    final ConventionBundle receiveLegFloatRateConvention = source.getConventionBundle(convention.getBasisSwapReceiveFloatingLegInitialRate());
    final ExternalId payLegFloatRateBloombergTicker = payLegFloatRateConvention.getIdentifiers().getExternalId(SecurityUtils.BLOOMBERG_TICKER);
    final ExternalId receiveLegFloatRateBloombergTicker = receiveLegFloatRateConvention.getIdentifiers().getExternalId(SecurityUtils.BLOOMBERG_TICKER);
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

  private CashSecurity getOISCash(final InterpolatedYieldCurveSpecification spec, final FixedIncomeStripWithIdentifier strip, final Map<ExternalId, Double> marketValues) {
    final ExternalId identifier = strip.getSecurity();
    final double rate = marketValues.get(identifier);
    final ZonedDateTime maturity = spec.getCurveDate().plus(strip.getMaturity().getPeriod()).atTime(11, 00).atZone(TimeZone.UTC);
    final Currency currency = spec.getCurrency();
    final ExternalId region = spec.getRegion();
    final CashSecurity cash = new CashSecurity(currency, region, maturity, rate, 1);
    cash.setExternalIdBundle(ExternalIdBundle.of(identifier));
    return cash;
  }

  private SwapSecurity getOISSwap(final InterpolatedYieldCurveSpecification spec, final FixedIncomeStripWithIdentifier strip, final Map<ExternalId, Double> marketValues) {
    final ExternalId swapIdentifier = strip.getSecurity();
    final Double rate = marketValues.get(swapIdentifier);
    final LocalDate curveDate = spec.getCurveDate();
    final ConventionBundleSource source = s_conventionBundleSource;

    final ZonedDateTime tradeDate = curveDate.atTime(11, 00).atZone(TimeZone.UTC);
    final ZonedDateTime effectiveDate = DateUtils.previousWeekDay(curveDate.plusDays(3)).atTime(11, 00).atZone(TimeZone.UTC);
    final ZonedDateTime maturityDate = curveDate.plus(strip.getMaturity().getPeriod()).atTime(11, 00).atZone(TimeZone.UTC);
    final ConventionBundle convention = _conventionBundleSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, spec.getCurrency().getCode() + "_OIS_SWAP"));
    if (convention == null) {
      throw new OpenGammaRuntimeException("Could not get convention for id " + ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, spec.getCurrency().getCode() + "_OIS_SWAP"));
    }
    final String counterparty = "";
    final ConventionBundle floatRateConvention = source.getConventionBundle(convention.getSwapFloatingLegInitialRate());
    if (floatRateConvention == null) {
      throw new OpenGammaRuntimeException("Could not get convention for id " + convention.getSwapFloatingLegInitialRate());
    }
    final ExternalId floatRateBloombergTicker = floatRateConvention.getIdentifiers().getExternalId(SecurityUtils.BLOOMBERG_TICKER);
    if (rate == null) {
      throw new OpenGammaRuntimeException("rate was null on " + strip + " from " + spec);
    }
    final double fixedRate = rate;
    // REVIEW: jim 25-Aug-2010 -- we need to change the swap to take settlement days.

    final SwapSecurity swap = new SwapSecurity(tradeDate, effectiveDate, maturityDate, counterparty, new FloatingInterestRateLeg(convention.getSwapFloatingLegDayCount(), 
        convention.getSwapFloatingLegFrequency(), convention.getSwapFloatingLegRegion(), convention.getSwapFloatingLegBusinessDayConvention(), new InterestRateNotional(spec.getCurrency(), 1), 
        false, floatRateBloombergTicker, FloatingRateType.IBOR), new FixedInterestRateLeg(convention.getSwapFixedLegDayCount(), convention.getSwapFixedLegFrequency(), 
            convention.getSwapFixedLegRegion(), convention.getSwapFixedLegBusinessDayConvention(), new InterestRateNotional(spec.getCurrency(), 1), false, fixedRate));
    swap.setExternalIdBundle(ExternalIdBundle.of(swapIdentifier));

    return swap;
  }

  private TimeZone ensureZone(final TimeZone zone) {
    if (zone != null) {
      return zone;
    } 
    return TimeZone.UTC;
  }
}
