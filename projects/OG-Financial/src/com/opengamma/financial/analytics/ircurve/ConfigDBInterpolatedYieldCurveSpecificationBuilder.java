/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.time.calendar.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.ExternalId;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;

/**
 * 
 */
public class ConfigDBInterpolatedYieldCurveSpecificationBuilder implements InterpolatedYieldCurveSpecificationBuilder {
  private final ConfigSource _configSource;
  // REVIEW: maybe we shouldn't cache these and rely on the repo doing that, but this prevents changes flowing through while we're running.
  private final Map<String, CurveSpecificationBuilderConfiguration> _specBuilderCache = new HashMap<String, CurveSpecificationBuilderConfiguration>();

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
      CurveSpecificationBuilderConfiguration builderSpecDoc = _configSource.getLatestByName(CurveSpecificationBuilderConfiguration.class, conventionName);
      if (builderSpecDoc != null) {
        _specBuilderCache.put(conventionName, builderSpecDoc);
        return builderSpecDoc;
      } else {
        return null;
      }
    }
  }

  @Override
  public InterpolatedYieldCurveSpecification buildCurve(LocalDate curveDate, YieldCurveDefinition curveDefinition) {
    clearConfigCache();
    Collection<FixedIncomeStripWithIdentifier> securities = new ArrayList<FixedIncomeStripWithIdentifier>();
    for (FixedIncomeStrip strip : curveDefinition.getStrips()) {
      CurveSpecificationBuilderConfiguration builderConfig = getBuilderConfig(strip.getConventionName() + "_" + curveDefinition.getCurrency().getCode());
      ExternalId identifier;
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
        case BASIS_SWAP:
          identifier = builderConfig.getBasisSwapSecurity(curveDate, strip.getCurveNodePointTime());
          break;
        case TENOR_SWAP:
          identifier = builderConfig.getTenorSwapSecurity(curveDate, strip.getCurveNodePointTime());
          break;
        case OIS_SWAP:
          identifier = builderConfig.getOISSwapSecurity(curveDate, strip.getCurveNodePointTime());
          break;
        default:
          throw new OpenGammaRuntimeException("Unhandled type of instrument in curve definition " + strip.getInstrumentType());
      }
      if (strip.getInstrumentType() == StripInstrumentType.FUTURE) {
        securities.add(new FixedIncomeStripWithIdentifier(strip.getInstrumentType(), strip.getCurveNodePointTime(), strip.getNumberOfFuturesAfterTenor(), identifier));
      } else {
        securities.add(new FixedIncomeStripWithIdentifier(strip.getInstrumentType(), strip.getCurveNodePointTime(), identifier));
      }
    }
    Interpolator1D<?> interpolator = Interpolator1DFactory.getInterpolator(curveDefinition.getInterpolatorName());
    return new InterpolatedYieldCurveSpecification(curveDate, curveDefinition.getName(), curveDefinition.getCurrency(), interpolator, securities, curveDefinition.getRegionId());
  }
}
