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

  public ConfigDBInterpolatedYieldCurveSpecificationBuilder(final ConfigSource configSource) {
    _configSource = configSource;
  }

  private void clearConfigCache() {
    _specBuilderCache.clear(); // specifically so if the config is changed, we don't cache stale values outside a single curve build.
  }

  // this is factored out into a method so it's easier to remove if we want to disable caching.
  private CurveSpecificationBuilderConfiguration getBuilderConfig(final String conventionName) {
    if (_specBuilderCache.containsKey(conventionName)) {
      return _specBuilderCache.get(conventionName);
    } else {
      final CurveSpecificationBuilderConfiguration builderSpecDoc = _configSource.getLatestByName(CurveSpecificationBuilderConfiguration.class, conventionName);
      if (builderSpecDoc != null) {
        _specBuilderCache.put(conventionName, builderSpecDoc);
        return builderSpecDoc;
      } else {
        return null;
      }
    }
  }

  @Override
  public InterpolatedYieldCurveSpecification buildCurve(final LocalDate curveDate, final YieldCurveDefinition curveDefinition) {
    clearConfigCache();
    final Collection<FixedIncomeStripWithIdentifier> securities = new ArrayList<FixedIncomeStripWithIdentifier>();
    for (final FixedIncomeStrip strip : curveDefinition.getStrips()) {
      final CurveSpecificationBuilderConfiguration builderConfig = getBuilderConfig(strip.getConventionName() + "_" + curveDefinition.getCurrency().getCode());
      if (builderConfig == null) {
        throw new OpenGammaRuntimeException("Could not get specification builder configuration for curve=" + 
            curveDefinition.getName() + ", currency=" + curveDefinition.getCurrency() + ", strip=" + strip);
      }
      ExternalId identifier;
      switch (strip.getInstrumentType()) {
        case CASH:
          identifier = builderConfig.getCashSecurity(curveDate, strip.getCurveNodePointTime());
          break;
        case FRA_3M:
          identifier = builderConfig.getFRA3MSecurity(curveDate, strip.getCurveNodePointTime());
          break;
        case FRA_6M:
          identifier = builderConfig.getFRA6MSecurity(curveDate, strip.getCurveNodePointTime());
          break;
        case FRA: 
          // assume that all old FRAs are 3m - shouldn't be used but just for consistency
          identifier = builderConfig.getFRA3MSecurity(curveDate, strip.getCurveNodePointTime());
          break;
        case FUTURE:
          identifier = builderConfig.getFutureSecurity(curveDate, strip.getCurveNodePointTime(), strip.getNumberOfFuturesAfterTenor());
          break;
        case LIBOR: //TODO is this right? It seems that we should have a generic IBOR strip. We will need to think about how we deal with *ibor providers 
          identifier = builderConfig.getLiborSecurity(curveDate, strip.getCurveNodePointTime());
          break;
        case EURIBOR:
          identifier = builderConfig.getEuriborSecurity(curveDate, strip.getCurveNodePointTime());
          break;
        case SWAP_3M:
          identifier = builderConfig.getSwap3MSecurity(curveDate, strip.getCurveNodePointTime());
          break;
        case SWAP_6M:
          identifier = builderConfig.getSwap6MSecurity(curveDate, strip.getCurveNodePointTime());
          break;
        case SWAP: 
          // assume that all old swaps are 3m - shouldn't be used but just for consistency
          identifier = builderConfig.getSwap3MSecurity(curveDate, strip.getCurveNodePointTime());
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
      securities.add(new FixedIncomeStripWithIdentifier(strip, identifier));
    }
    final Interpolator1D<?> interpolator = Interpolator1DFactory.getInterpolator(curveDefinition.getInterpolatorName());
    return new InterpolatedYieldCurveSpecification(curveDate, curveDefinition.getName(), curveDefinition.getCurrency(), interpolator, securities, curveDefinition.getRegionId());
  }
}
