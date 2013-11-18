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
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public abstract class FXBlackVolatilitySurfaceDefaults extends DefaultPropertyFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(FXBlackVolatilitySurfaceDefaults.class);
  private static final String[] VALUE_REQUIREMENTS = new String[] {
      ValueRequirementNames.BLACK_VOLATILITY_SURFACE,
      ValueRequirementNames.LOCAL_VOLATILITY_SURFACE,
      ValueRequirementNames.FORWARD_DELTA,
      ValueRequirementNames.DUAL_DELTA,
      ValueRequirementNames.DUAL_GAMMA,
      ValueRequirementNames.FORWARD_GAMMA,
      ValueRequirementNames.FOREX_DOMESTIC_PRICE,
      ValueRequirementNames.FOREX_PV_QUOTES,
      ValueRequirementNames.FORWARD_VEGA,
      ValueRequirementNames.FORWARD_VOMMA,
      ValueRequirementNames.FORWARD_VANNA,
      ValueRequirementNames.PRESENT_VALUE,
      ValueRequirementNames.FX_PRESENT_VALUE,
      ValueRequirementNames.IMPLIED_VOLATILITY,
      ValueRequirementNames.GRID_DUAL_DELTA,
      ValueRequirementNames.GRID_DUAL_GAMMA,
      ValueRequirementNames.GRID_FORWARD_DELTA,
      ValueRequirementNames.GRID_FORWARD_GAMMA,
      ValueRequirementNames.GRID_FORWARD_VEGA,
      ValueRequirementNames.GRID_FORWARD_VANNA,
      ValueRequirementNames.GRID_FORWARD_VOMMA,
      ValueRequirementNames.GRID_IMPLIED_VOLATILITY,
      ValueRequirementNames.GRID_PRESENT_VALUE
  };
  private final Map<String, Set<String>> _currencyPairToCurveName; //TODO duplicated in FXForwardCurveDefaults
  private final Map<String, Set<String>> _currencyPairToCurveCalculationMethodName; //TODO duplicated in FXForwardCurveDefaults
  private final Map<String, Set<String>> _currencyPairToSurfaceName;

  public FXBlackVolatilitySurfaceDefaults(final ComputationTargetType target, final String... defaultsPerCurrencyPair) {
    super(target, true);
    ArgumentChecker.notNull(defaultsPerCurrencyPair, "defaults per currency");
    final int n = defaultsPerCurrencyPair.length;
    ArgumentChecker.isTrue(n % 4 == 0, "Need one forward curve name, forward curve calculation method and surface name per currency pair");
    _currencyPairToCurveName = Maps.newLinkedHashMap();
    _currencyPairToCurveCalculationMethodName = Maps.newLinkedHashMap();
    _currencyPairToSurfaceName = Maps.newLinkedHashMap();
    for (int i = 0; i < n; i += 4) {
      final String currencyPair = defaultsPerCurrencyPair[i];
      _currencyPairToCurveName.put(currencyPair, Collections.singleton(defaultsPerCurrencyPair[i + 1]));
      _currencyPairToCurveCalculationMethodName.put(currencyPair, Collections.singleton(defaultsPerCurrencyPair[i + 2]));
      _currencyPairToSurfaceName.put(currencyPair, Collections.singleton(defaultsPerCurrencyPair[i + 3]));
    }
  }

  @Override
  public abstract boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target);

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CURVE);
      defaults.addValuePropertyName(valueRequirement, ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.SURFACE);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final String currencyPair = getCurrencyPair(target);
    switch (propertyName) {
      case ValuePropertyNames.CURVE:
        return _currencyPairToCurveName.get(currencyPair);
      case ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD:
        return _currencyPairToCurveCalculationMethodName.get(currencyPair);
      case ValuePropertyNames.SURFACE:
        return _currencyPairToSurfaceName.get(currencyPair);
      default:
        s_logger.error("Could not find default value for {} in this function", propertyName);
        return null;
    }
  }

  protected Collection<String> getAllCurrencyPairs() {
    return _currencyPairToCurveName.keySet();
  }

  protected abstract String getCurrencyPair(ComputationTarget target);

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.BLACK_VOLATILITY_SURFACE_DEFAULTS;
  }

}
