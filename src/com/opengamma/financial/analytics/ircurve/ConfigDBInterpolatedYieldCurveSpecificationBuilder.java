/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;
import javax.time.i18n.Territory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.config.ConfigSearchRequest;
import com.opengamma.engine.config.ConfigSource;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecuritySource;
import com.opengamma.engine.world.Region;
import com.opengamma.engine.world.RegionSource;
import com.opengamma.financial.InMemoryRegionRepository;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class ConfigDBInterpolatedYieldCurveSpecificationBuilder implements InterpolatedYieldCurveSpecificationBuilder {
  private static final LocalTime CASH_EXPIRY_TIME = LocalTime.of(11, 00); // 11am
  private ConfigSource _configSource;
  private SecuritySource _secSource;
  private RegionSource _regionSource;
  // REVIEW: maybe we shouldn't cache these and rely on the repo doing that, but this prevents changes flowing through while we're running.
  private Map<String, CurveSpecificationBuilderConfiguration> _specBuilderCache = new HashMap<String, CurveSpecificationBuilderConfiguration>();
  
  public ConfigDBInterpolatedYieldCurveSpecificationBuilder(RegionSource regionSource, ConfigSource configSource, SecuritySource secSource) {
    _regionSource = regionSource;
    _configSource = configSource;
    _secSource = secSource;
  }
  
  private void clearConfigCache() {
    _specBuilderCache.clear(); // specifically so if the config is changed, we don't cache stale values outside a single curve build.
  }
  
  // this is factored out into a method so it's easier to remove if we want to disable caching.
  private CurveSpecificationBuilderConfiguration getBuilderConfig(String conventionName) {
    if (_specBuilderCache.containsKey(conventionName)) {
      return _specBuilderCache.get(conventionName);
    } else {
      ConfigSearchRequest search = new ConfigSearchRequest();
      search.setName(conventionName);
      List<CurveSpecificationBuilderConfiguration> builderSpecDoc = _configSource.search(CurveSpecificationBuilderConfiguration.class, search);
      if (builderSpecDoc.size() > 0) {
        _specBuilderCache.put(conventionName, builderSpecDoc.get(0));
        return builderSpecDoc.get(0);
      } else {
        return null;
      }
    }
  }
  
  private TimeZone ensureZone(TimeZone zone) {
    if (zone != null) {
      return zone;
    } else {
      return TimeZone.UTC;
    }
  }
  
  @Override
  public InterpolatedYieldCurveSpecification buildCurve(LocalDate curveDate, YieldCurveDefinition curveDefinition) {
    curveDefinition.getCurrency();
    clearConfigCache();
    Collection<ResolvedFixedIncomeStrip> securities = new ArrayList<ResolvedFixedIncomeStrip>();
    for (FixedIncomeStrip strip : curveDefinition.getStrips()) {
      CurveSpecificationBuilderConfiguration builderConfig = getBuilderConfig(strip.getConventionName() + "_" + curveDefinition.getCurrency().getISOCode());
      Security security;
      ZonedDateTime maturity;
      switch (strip.getInstrumentType()) {
        case CASH:
          Identifier cashIdentifier = builderConfig.getCashSecurity(curveDate, strip.getCurveNodePointTime());
          CashSecurity cashSecurity = (CashSecurity) _secSource.getSecurity(IdentifierBundle.of(cashIdentifier));
          if (cashSecurity == null) { throw new OpenGammaRuntimeException("Could not resolve cash curve instrument " + cashIdentifier + " from strip " + strip + " in " + curveDefinition); }
          Region region = _regionSource.getHighestLevelRegion(cashSecurity.getRegion());
          TimeZone timeZone = region.getTimeZone();
          timeZone = ensureZone(timeZone);
          maturity = curveDate.plus(strip.getCurveNodePointTime().getPeriod()).atTime(CASH_EXPIRY_TIME).atZone(timeZone);
          security = cashSecurity;
          break;
        case FRA:
          // TODO: jim 17-Aug-2010 -- we need to sort out the zoned date time related to the expiry.
          Identifier fraIdentifier = builderConfig.getFRASecurity(curveDate, strip.getCurveNodePointTime());
          FRASecurity fraSecurity = (FRASecurity) _secSource.getSecurity(IdentifierBundle.of(fraIdentifier));
          if (fraSecurity == null) { throw new OpenGammaRuntimeException("Could not resolve FRA curve instrument " + fraIdentifier + " from strip " + strip + " in " + curveDefinition); }
          maturity = fraSecurity.getEndDate().toZonedDateTime();
          security = fraSecurity;
          break;
        case FUTURE:
          // TODO: jim 17-Aug-2010 -- we need to sort out the zoned date time related to the expiry.
          Identifier futureIdentifier = builderConfig.getFutureSecurity(curveDate, strip.getCurveNodePointTime(), strip.getNumberOfFuturesAfterTenor());
          FutureSecurity futureSecurity = (FutureSecurity) _secSource.getSecurity(IdentifierBundle.of(futureIdentifier));
          if (futureSecurity == null) { throw new OpenGammaRuntimeException("Could not resolve future curve instrument " + futureIdentifier + " from strip " + strip + " in " + curveDefinition); }
          maturity = futureSecurity.getExpiry().getExpiry();
          security = futureSecurity;
          break;
        case LIBOR:
          Identifier rateIdentifier = builderConfig.getRateSecurity(curveDate, strip.getCurveNodePointTime());
          CashSecurity rateSecurity = (CashSecurity) _secSource.getSecurity(IdentifierBundle.of(rateIdentifier));
          if (rateSecurity == null) { throw new OpenGammaRuntimeException("Could not resolve future curve instrument " + rateIdentifier + " from strip " + strip + " in " + curveDefinition); }
          Region region2 = _regionSource.getHighestLevelRegion(rateSecurity.getRegion());
          TimeZone timeZone2 = region2.getTimeZone();
          timeZone2 = ensureZone(timeZone2);
          maturity = curveDate.plus(strip.getCurveNodePointTime().getPeriod()).atTime(CASH_EXPIRY_TIME).atZone(timeZone2);
          security = rateSecurity;
          break;
        case SWAP:
          // TODO: jim 17-Aug-2010 -- we need to sort out the zoned date time related to the expiry.
          Identifier swapIdentifier = builderConfig.getSwapSecurity(curveDate, strip.getCurveNodePointTime());
          SwapSecurity swapSecurity = (SwapSecurity) _secSource.getSecurity(IdentifierBundle.of(swapIdentifier));
          if (swapSecurity == null) { throw new OpenGammaRuntimeException("Could not resolve swap curve instrument " + swapIdentifier + " from strip " + strip + " in " + curveDefinition); }
          maturity = swapSecurity.getMaturityDate().toZonedDateTime();
          security = swapSecurity;
          break;
        default:
          throw new OpenGammaRuntimeException("Unhandled type of instrument in curve definition " + strip.getInstrumentType());
      }
      securities.add(new ResolvedFixedIncomeStrip(strip.getInstrumentType(), maturity, security));
    }
    Interpolator1D<?> interpolator = Interpolator1DFactory.getInterpolator(curveDefinition.getInterpolatorName());  
    return new InterpolatedYieldCurveSpecification(curveDate, curveDefinition.getName(), curveDefinition.getCurrency(), interpolator, securities);
  }

}
