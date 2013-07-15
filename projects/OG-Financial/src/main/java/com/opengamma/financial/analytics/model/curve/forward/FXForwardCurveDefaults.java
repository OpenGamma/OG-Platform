/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.forward;

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
public abstract class FXForwardCurveDefaults extends DefaultPropertyFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(FXForwardCurveDefaults.class);
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.FORWARD_CURVE,
    ValueRequirementNames.BLACK_VOLATILITY_SURFACE,
    ValueRequirementNames.LOCAL_VOLATILITY_SURFACE,
    ValueRequirementNames.PURE_VOLATILITY_SURFACE,
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
  private final Map<String, String> _currencyPairToCurveName;
  private final Map<String, String> _currencyPairToCurveCalculationMethodName;

  public FXForwardCurveDefaults(final ComputationTargetType target, final String... defaultsPerCurrencyPair) {
    super(target, true);
    ArgumentChecker.notNull(defaultsPerCurrencyPair, "defaults per currency");
    final int n = defaultsPerCurrencyPair.length;
    ArgumentChecker.isTrue(n % 3 == 0, "Need one forward curve name, forward curve calculation method and surface name per currency pair");
    _currencyPairToCurveName = Maps.newLinkedHashMap();
    _currencyPairToCurveCalculationMethodName = Maps.newLinkedHashMap();
    for (int i = 0; i < n; i += 3) {
      final String currencyPair = defaultsPerCurrencyPair[i];
      _currencyPairToCurveName.put(currencyPair, defaultsPerCurrencyPair[i + 1]);
      _currencyPairToCurveCalculationMethodName.put(currencyPair, defaultsPerCurrencyPair[i + 2]);
    }

  }

  @Override
  public abstract boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target);

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CURVE);
      defaults.addValuePropertyName(valueRequirement, ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final String currencyPair = getCurrencyPair(target);
    final String curveName = _currencyPairToCurveName.get(currencyPair);
    if (curveName == null) {
      s_logger.error("Could not get curve name for {}; should never happen", target.getValue());
      return null;
    }
    if (ValuePropertyNames.CURVE.equals(propertyName)) {
      return Collections.singleton(curveName);
    }
    if (ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD.equals(propertyName)) {
      return Collections.singleton(_currencyPairToCurveCalculationMethodName.get(currencyPair));
    }
    s_logger.error("Could not find default value for {} in this function", propertyName);
    return null;
  }

  protected Collection<String> getAllCurrencyPairs() {
    return _currencyPairToCurveName.keySet();
  }

  protected abstract String getCurrencyPair(ComputationTarget target);

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.CURVE_DEFAULTS;
  }

}
