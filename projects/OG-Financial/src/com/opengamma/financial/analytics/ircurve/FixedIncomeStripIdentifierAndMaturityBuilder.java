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

import com.google.common.collect.Maps;
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
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Tenor;

/**
 * Converts specifications into fully resolved security definitions 
 */
public class FixedIncomeStripIdentifierAndMaturityBuilder {
  private static final LocalTime CASH_EXPIRY_TIME = LocalTime.of(11, 00);
  private RegionSource _regionSource;
  private ConventionBundleSource _conventionBundleSource;
  private SecuritySource _secSource;
  private static final Map<Currency, Identifier> s_currency2FRAUnderlyings = Maps.newHashMap();
  
  static {
    s_currency2FRAUnderlyings.put(Currency.USD, Identifier.of(SecurityUtils.BLOOMBERG_TICKER, "US0003M Index"));
    s_currency2FRAUnderlyings.put(Currency.JPY, Identifier.of(SecurityUtils.BLOOMBERG_TICKER, "JY0003M Index"));
  }

  public FixedIncomeStripIdentifierAndMaturityBuilder(RegionSource regionSource, ConventionBundleSource conventionBundleSource, SecuritySource secSource) {
    _regionSource = regionSource;
    _conventionBundleSource = conventionBundleSource;
    _secSource = secSource;
  }
  
  public InterpolatedYieldCurveSpecificationWithSecurities resolveToSecurity(InterpolatedYieldCurveSpecification curveSpecification, Map<Identifier, Double> marketValues) {
    //Currency currency = curveSpecification.getCurrency();
    LocalDate curveDate = curveSpecification.getCurveDate();
    Collection<FixedIncomeStripWithSecurity> securityStrips = new ArrayList<FixedIncomeStripWithSecurity>();
    for (FixedIncomeStripWithIdentifier strip : curveSpecification.getStrips()) {
      Security security;
      ZonedDateTime maturity;
      switch (strip.getInstrumentType()) {
        case CASH:
          CashSecurity cashSecurity = getCash(curveSpecification, strip, marketValues);
          if (cashSecurity == null) { 
            throw new OpenGammaRuntimeException("Could not resolve cash curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification); 
          }
          Region region = _regionSource.getHighestLevelRegion(cashSecurity.getRegion());
          TimeZone timeZone = region.getTimeZone();
          timeZone = ensureZone(timeZone);
          maturity = curveDate.plus(strip.getMaturity().getPeriod()).atTime(CASH_EXPIRY_TIME).atZone(timeZone);
          security = cashSecurity;
          break;
        case FRA:
          FRASecurity fraSecurity = getFRA(curveSpecification, strip, marketValues);
          if (fraSecurity == null) { 
            throw new OpenGammaRuntimeException("Could not resolve FRA curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification); 
          }
          maturity = fraSecurity.getEndDate();
          security = fraSecurity;
          break;
        case FUTURE:
          // TODO: jim 17-Aug-2010 -- we need to sort out the zoned date time related to the expiry.
          FutureSecurity futureSecurity = getFuture(curveSpecification, strip);
          if (futureSecurity == null) { 
            throw new OpenGammaRuntimeException("Could not resolve future curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification); 
          }
          maturity = futureSecurity.getExpiry().getExpiry();
          security = futureSecurity;
          break;
        case LIBOR:
          CashSecurity rateSecurity = getCash(curveSpecification, strip, marketValues);
          if (rateSecurity == null) { 
            throw new OpenGammaRuntimeException("Could not resolve future curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification); 
          }
          Region region2 = _regionSource.getHighestLevelRegion(rateSecurity.getRegion());
          TimeZone timeZone2 = region2.getTimeZone();
          timeZone2 = ensureZone(timeZone2);
          maturity = curveDate.plus(strip.getMaturity().getPeriod()).atTime(CASH_EXPIRY_TIME).atZone(timeZone2);
          security = rateSecurity;
          break;
        case SWAP:
          SwapSecurity swapSecurity = getSwap(curveSpecification, strip, marketValues);
          if (swapSecurity == null) { 
            throw new OpenGammaRuntimeException("Could not resolve swap curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification); 
          }
          maturity = swapSecurity.getMaturityDate();
          security = swapSecurity;
          break;
        case TENOR_SWAP:
          SwapSecurity tenorSwapSecurity = getTenorSwap(curveSpecification, strip, marketValues);
          if (tenorSwapSecurity == null) { 
            throw new OpenGammaRuntimeException("Could not resolve swap curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification); 
          }
          maturity = tenorSwapSecurity.getMaturityDate();
          security = tenorSwapSecurity;
          break;
        default:
          throw new OpenGammaRuntimeException("Unhandled type of instrument in curve definition " + strip.getInstrumentType());
      }
      Tenor resolvedTenor = new Tenor(Period.between(curveDate, maturity.toLocalDate()));
      if (strip.getInstrumentType() == StripInstrumentType.FUTURE) {
        securityStrips.add(new FixedIncomeStripWithSecurity(strip.getInstrumentType(), strip.getMaturity(), resolvedTenor, 
                                                            strip.getNumberOfFuturesAfterTenor(), maturity,  strip.getSecurity(), security));
      } else {
        securityStrips.add(new FixedIncomeStripWithSecurity(strip.getInstrumentType(), strip.getMaturity(), resolvedTenor,
                           maturity, strip.getSecurity(), security));
      }
    }
    return new InterpolatedYieldCurveSpecificationWithSecurities(curveDate, curveSpecification.getName(), curveSpecification.getCurrency(), curveSpecification.getInterpolator(), securityStrips);
  }
  
  private CashSecurity getCash(InterpolatedYieldCurveSpecification spec, FixedIncomeStripWithIdentifier strip, Map<Identifier, Double> marketValues) {
//    CashSecurity sec = new CashSecurity(spec.getCurrency(), RegionUtils.countryRegionId("US"), 
//                                        new DateTimeWithZone(spec.getCurveDate().plus(strip.getMaturity().getPeriod()).atTime(11, 00)));
    CashSecurity sec = new CashSecurity(spec.getCurrency(), spec.getRegion(), 
        spec.getCurveDate().plus(strip.getMaturity().getPeriod()).atTime(11, 00).atZone(TimeZone.UTC), marketValues.get(strip.getSecurity()), 1.0d);
    sec.setIdentifiers(IdentifierBundle.of(strip.getSecurity()));
    return sec;
  }
  
  private FRASecurity getFRA(InterpolatedYieldCurveSpecification spec, FixedIncomeStripWithIdentifier strip, Map<Identifier, Double> marketValues) {
    LocalDate curveDate = spec.getCurveDate(); // quick hack
    LocalDate startDate = curveDate.plus(strip.getMaturity().getPeriod()).minus(Period.ofMonths(3));
    LocalDate endDate = startDate.plusMonths(3); // quick hack, needs to be sorted.
//    return new FRASecurity(spec.getCurrency(), RegionUtils.countryRegionId("US"), 
//                           new DateTimeWithZone(startDate.atTime(11, 00)), new DateTimeWithZone(endDate.atTime(11, 00)));
    //REVIEW: yomi 16-jun-2011 How do we get the correct underlying?
    Identifier underlyingIdentifier = getFRAUnderlyingIdentifier(spec.getCurrency(), spec.getRegion());
    return new FRASecurity(spec.getCurrency(), spec.getRegion(), 
        startDate.atTime(11, 00).atZone(TimeZone.UTC), endDate.atTime(11, 00).atZone(TimeZone.UTC), marketValues.get(strip.getSecurity()), 1.0d, underlyingIdentifier);
  }
  
  private Identifier getFRAUnderlyingIdentifier(Currency currency, Identifier region) {
    Identifier identifier = s_currency2FRAUnderlyings.get(currency);
    if (identifier == null) {
      throw new OpenGammaRuntimeException("unable to workout underlying identifier for FRA with currency = " + currency);
    }
    return identifier;
  }

  private FutureSecurity getFuture(InterpolatedYieldCurveSpecification spec, FixedIncomeStripWithIdentifier strip) {
    return (FutureSecurity) _secSource.getSecurity(IdentifierBundle.of(strip.getSecurity()));
  }
  
  private SwapSecurity getSwap(InterpolatedYieldCurveSpecification spec, FixedIncomeStripWithIdentifier strip, Map<Identifier, Double> marketValues) {
    Identifier swapIdentifier = strip.getSecurity();
    Double rate = marketValues.get(swapIdentifier);
    LocalDate curveDate = spec.getCurveDate();
    InMemoryConventionBundleMaster refRateRepo = new InMemoryConventionBundleMaster();
    ConventionBundleSource source = new DefaultConventionBundleSource(refRateRepo);
    ZonedDateTime tradeDate = curveDate.atTime(11, 00).atZone(TimeZone.UTC);
    ZonedDateTime effectiveDate = DateUtil.previousWeekDay(curveDate.plusDays(3)).atTime(11, 00).atZone(TimeZone.UTC);
    ZonedDateTime maturityDate = curveDate.plus(strip.getMaturity().getPeriod()).atTime(11, 00).atZone(TimeZone.UTC);
    ConventionBundle convention = _conventionBundleSource.getConventionBundle(Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, spec.getCurrency().getCode() + "_SWAP"));
    String counterparty = "";
    ConventionBundle floatRateConvention = source.getConventionBundle(convention.getSwapFloatingLegInitialRate());
    Double initialRate = null; 
    for (Identifier identifier :  floatRateConvention.getIdentifiers()) {
      if (marketValues.containsKey(identifier)) {
        initialRate = marketValues.get(identifier); // get the initial rate.
        break;
      }
    }
    if (initialRate == null) {
      throw new OpenGammaRuntimeException("Could not get initial rate");
    }
    double spread = 0;
    if (rate == null) {
      throw new OpenGammaRuntimeException("rate was null on " + strip + " from " + spec);
    }
    double fixedRate = rate;
    // REVIEW: jim 25-Aug-2010 -- we need to change the swap to take settlement days.
    SwapSecurity swap =  new SwapSecurity(tradeDate, 
                                          effectiveDate, 
                                          maturityDate,
                                          counterparty, 
                                            new FloatingInterestRateLeg(
                                                convention.getSwapFloatingLegDayCount(),
                                                convention.getSwapFloatingLegFrequency(),
                                                convention.getSwapFloatingLegRegion(),
                                                convention.getSwapFloatingLegBusinessDayConvention(),
                                                new InterestRateNotional(spec.getCurrency(), 1),
                                                floatRateConvention.getUniqueId(), 
                                                initialRate, 
                                                spread,
                                                true),
                                            new FixedInterestRateLeg(
                                                convention.getSwapFixedLegDayCount(), 
                                                convention.getSwapFixedLegFrequency(),
                                                convention.getSwapFixedLegRegion(), 
                                                convention.getSwapFixedLegBusinessDayConvention(),
                                                new InterestRateNotional(spec.getCurrency(), 1),
                                                fixedRate)
                                          );
    swap.setIdentifiers(IdentifierBundle.of(swapIdentifier));
    return swap;
  }
  
  private SwapSecurity getTenorSwap(InterpolatedYieldCurveSpecification spec, FixedIncomeStripWithIdentifier strip, Map<Identifier, Double> marketValues) {
    Identifier swapIdentifier = strip.getSecurity();
    //Double rate = marketValues.get(swapIdentifier);
    LocalDate curveDate = spec.getCurveDate();
    InMemoryConventionBundleMaster refRateRepo = new InMemoryConventionBundleMaster();
    ConventionBundleSource source = new DefaultConventionBundleSource(refRateRepo);
    ZonedDateTime tradeDate = curveDate.atTime(11, 00).atZone(TimeZone.UTC);
    ZonedDateTime effectiveDate = DateUtil.previousWeekDay(curveDate.plusDays(3)).atTime(11, 00).atZone(TimeZone.UTC);
    ZonedDateTime maturityDate = curveDate.plus(strip.getMaturity().getPeriod()).atTime(11, 00).atZone(TimeZone.UTC);
    ConventionBundle convention = _conventionBundleSource.getConventionBundle(Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, spec.getCurrency().getCode() + "_TENOR_SWAP"));
    String counterparty = "";
    ConventionBundle floatRateConvention = source.getConventionBundle(convention.getBasisSwapPayFloatingLegInitialRate());
    Double initialRate = null; 
    for (Identifier identifier :  floatRateConvention.getIdentifiers()) {
      if (marketValues.containsKey(identifier)) {
        initialRate = marketValues.get(identifier); // get the initial rate.
        break;
      }
    }
    if (initialRate == null) {
      throw new OpenGammaRuntimeException("Could not get initial rate");
    }
    double spread = 0;
    //double fixedRate = rate;
    // REVIEW: jim 25-Aug-2010 -- we need to change the swap to take settlement days.
    SwapSecurity swap =  new SwapSecurity(tradeDate, 
                                          effectiveDate, 
                                          maturityDate,
                                          counterparty, 
                                            new FloatingInterestRateLeg(
                                                convention.getBasisSwapPayFloatingLegDayCount(),
                                                convention.getBasisSwapPayFloatingLegFrequency(),
                                                convention.getBasisSwapPayFloatingLegRegion(),
                                                convention.getBasisSwapPayFloatingLegBusinessDayConvention(),
                                                new InterestRateNotional(spec.getCurrency(), 1),
                                                floatRateConvention.getUniqueId(), 
                                                initialRate, 
                                                spread,
                                                true),
                                            new FloatingInterestRateLeg(
                                                convention.getBasisSwapReceiveFloatingLegDayCount(),
                                                convention.getBasisSwapReceiveFloatingLegFrequency(),
                                                convention.getBasisSwapReceiveFloatingLegRegion(),
                                                convention.getBasisSwapReceiveFloatingLegBusinessDayConvention(),
                                                new InterestRateNotional(spec.getCurrency(), 1),
                                                floatRateConvention.getUniqueId(), 
                                                initialRate, 
                                                spread,
                                                true)
                                          );
    swap.setIdentifiers(IdentifierBundle.of(swapIdentifier));
    return swap;
  }
  
  private TimeZone ensureZone(TimeZone zone) {
    if (zone != null) {
      return zone;
    } else {
      return TimeZone.UTC;
    }
  }
}
