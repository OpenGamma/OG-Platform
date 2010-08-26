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

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.config.ConfigSearchRequest;
import com.opengamma.engine.config.ConfigSource;
import com.opengamma.engine.security.SecuritySource;
import com.opengamma.engine.world.RegionSource;
import com.opengamma.id.Identifier;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;

/**
 * 
 */
public class ConfigDBInterpolatedYieldCurveSpecificationBuilder implements InterpolatedYieldCurveSpecificationBuilder {
  private ConfigSource _configSource;
  // REVIEW: maybe we shouldn't cache these and rely on the repo doing that, but this prevents changes flowing through while we're running.
  private Map<String, CurveSpecificationBuilderConfiguration> _specBuilderCache = new HashMap<String, CurveSpecificationBuilderConfiguration>();
  
  public ConfigDBInterpolatedYieldCurveSpecificationBuilder(ConfigSource configSource) {
    _configSource = configSource;
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
  

  
  @Override
  public InterpolatedYieldCurveSpecification buildCurve(LocalDate curveDate, YieldCurveDefinition curveDefinition) {
    clearConfigCache();
    Collection<ResolvedFixedIncomeStrip> securities = new ArrayList<ResolvedFixedIncomeStrip>();
    for (FixedIncomeStrip strip : curveDefinition.getStrips()) {
      CurveSpecificationBuilderConfiguration builderConfig = getBuilderConfig(strip.getConventionName() + "_" + curveDefinition.getCurrency().getISOCode());
      Identifier identifier;
      switch (strip.getInstrumentType()) {
        case CASH:
          identifier = builderConfig.getCashSecurity(curveDate, strip.getCurveNodePointTime());
          break;
        case FRA:
          identifier = builderConfig.getFRASecurity(curveDate, strip.getCurveNodePointTime());
          break;
        case FUTURE:
          identifier = builderConfig.getFutureSecurity(curveDate, strip.getCurveNodePointTime(), strip.getNumberOfFuturesAfterTenor());
          break;
        case LIBOR:
          identifier = builderConfig.getRateSecurity(curveDate, strip.getCurveNodePointTime());
          break;
        case SWAP:
          identifier = builderConfig.getSwapSecurity(curveDate, strip.getCurveNodePointTime());
          break;
        default:
          throw new OpenGammaRuntimeException("Unhandled type of instrument in curve definition " + strip.getInstrumentType());
      }
      securities.add(new ResolvedFixedIncomeStrip(strip.getInstrumentType(), strip.getCurveNodePointTime(), identifier));
    }
    Interpolator1D<?> interpolator = Interpolator1DFactory.getInterpolator(curveDefinition.getInterpolatorName());  
    return new InterpolatedYieldCurveSpecification(curveDate, curveDefinition.getName(), curveDefinition.getCurrency(), interpolator, securities);
  }  
}
