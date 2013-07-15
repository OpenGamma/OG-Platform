/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public abstract class PureBlackVolatilitySurfaceDefaults extends DefaultPropertyFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(PureBlackVolatilitySurfaceDefaults.class);
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.PURE_VOLATILITY_SURFACE,
  };
  private final Map<String, String> _tickerToCurveName;
  private final Map<String, String> _tickerToCurveCurrency;
  private final Map<String, String> _tickerToCurveCalculationConfig;
  private final Map<String, String> _tickerToSurfaceName;

  public PureBlackVolatilitySurfaceDefaults(final ComputationTargetType target, final String... defaultsPerTicker) {
    super(target, true);
    ArgumentChecker.notNull(defaultsPerTicker, "defaults per currency");
    final int n = defaultsPerTicker.length;
    ArgumentChecker.isTrue(n % 5 == 0, "Need one discounting curve name, curve currency, curve calculation config name and surface name per ticker value");
    _tickerToCurveName = Maps.newLinkedHashMap();
    _tickerToCurveCurrency = Maps.newLinkedHashMap();
    _tickerToCurveCalculationConfig = Maps.newLinkedHashMap();
    _tickerToSurfaceName = Maps.newLinkedHashMap();
    for (int i = 0; i < n; i += 5) {
      final String ticker = defaultsPerTicker[i];
      _tickerToCurveName.put(ticker, defaultsPerTicker[i + 1]);
      _tickerToCurveCurrency.put(ticker, defaultsPerTicker[i + 2]);
      _tickerToCurveCalculationConfig.put(ticker, defaultsPerTicker[i + 3]);
      _tickerToSurfaceName.put(ticker, defaultsPerTicker[i + 4]);
    }
  }

  @Override
  public abstract boolean canApplyTo(FunctionCompilationContext context, final ComputationTarget target);

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CURVE);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CURVE_CALCULATION_CONFIG);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CURVE_CURRENCY);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.SURFACE);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final String ticker = getTicker(target);
    final String curveName = _tickerToCurveName.get(ticker);
    if (curveName == null) {
      s_logger.error("Could not get curve name for {}; should never happen", target.getValue());
      return null;
    }
    if (ValuePropertyNames.CURVE.equals(propertyName)) {
      return Collections.singleton(curveName);
    }
    if (ValuePropertyNames.CURVE_CALCULATION_CONFIG.equals(propertyName)) {
      return Collections.singleton(_tickerToCurveCalculationConfig.get(ticker));
    }
    if (ValuePropertyNames.CURVE_CURRENCY.equals(propertyName)) {
      return Collections.singleton(_tickerToCurveCurrency.get(ticker));
    }
    if (ValuePropertyNames.SURFACE.equals(propertyName)) {
      return Collections.singleton(_tickerToSurfaceName.get(ticker));
    }
    s_logger.error("Could not find default value for {} in this function", propertyName);
    return null;
  }

  protected Collection<String> getAllTickers() {
    return _tickerToCurveName.keySet();
  }

  protected abstract String getTicker(ComputationTarget target);

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.EQUITY_PURE_VOLATILITY_SURFACE_DEFAULTS;
  }


}
